package com.project.likelion14thbe.domain.auth.service.command;

import com.project.likelion14thbe.domain.auth.converter.OAuthConverter;
import com.project.likelion14thbe.domain.auth.dto.oauth.OAuthUserInfo;
import com.project.likelion14thbe.domain.auth.dto.response.JwtDTO;
import com.project.likelion14thbe.domain.auth.entity.SocialAccount;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.auth.exception.AuthErrorCode;
import com.project.likelion14thbe.domain.auth.exception.AuthException;
import com.project.likelion14thbe.domain.auth.repository.SocialAccountRepository;
import com.project.likelion14thbe.domain.auth.strategy.OAuthStrategy;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class OAuthCommandServiceImpl implements OAuthCommandService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Map<Provider, OAuthStrategy> strategyMap = new EnumMap<>(Provider.class);
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public OAuthCommandServiceImpl(
            List<OAuthStrategy> strategies,
            SocialAccountRepository socialAccountRepository,
            MemberRepository memberRepository,
            JwtUtil jwtUtil,
            BCryptPasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager
    ) {
        for (OAuthStrategy s : strategies) {
            strategyMap.put(s.getProvider(), s);
        }
        this.socialAccountRepository = socialAccountRepository;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        // 자기호출 한계를 피하기 위해 프록시 어드바이스 대신 TransactionTemplate 으로 명시 트랜잭션을 연다
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void redirect(Provider provider, HttpServletResponse response, HttpSession session)
            throws IOException {
        OAuthStrategy strategy = getStrategy(provider);
        String state = new BigInteger(130, SECURE_RANDOM).toString(32);
        // CSRF 방지용 state를 HttpSession에 저장한다.
        // SecurityConfig는 SessionCreationPolicy.STATELESS이지만 충돌하지 않는다.
        // STATELESS는 스프링 시큐리티가 SecurityContext를 세션에 보존하지 않는다는 의미일 뿐,
        // 서블릿 컨테이너의 세션 메커니즘 자체를 끄지 않는다. 여기서 HttpSession에 직접
        // setAttribute하면 컨테이너가 실제 세션과 JSESSIONID 쿠키를 생성하고, 브라우저는
        // OAuth 왕복 동안 이를 보관하므로(콜백은 top-level GET, 쿠키 기본 SameSite=Lax는 전송)
        // 콜백에서 세션이 복원된다. 팀장 운영 레포 capstone-BackEnd가 동일 패턴으로 실서비스 동작.
        session.setAttribute(stateKey(provider), state);
        response.sendRedirect(strategy.buildAuthorizationUrl(state));
    }

    @Override
    public JwtDTO handleCallback(Provider provider, String code, String state, String error, HttpSession session) {
        // 인가 거부 또는 Provider 오류 처리 (state 검증 전)
        if (error != null && !error.isBlank()) {
            log.warn("[OAuth] provider={} 인가 거부/에러: {}", provider, error);
            throw new AuthException(AuthErrorCode.OAUTH_ACCESS_DENIED);
        }
        // state 검증 (외부 호출 전, 트랜잭션 밖)
        Object stored = session.getAttribute(stateKey(provider));
        if (stored == null || !Objects.equals(stored.toString(), state)) {
            throw new AuthException(invalidStateCode(provider));
        }
        session.removeAttribute(stateKey(provider));
        log.debug("[OAuth] handleCallback provider={}", provider);

        OAuthStrategy strategy = getStrategy(provider);

        // code 누락 방어
        if (code == null || code.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_OAUTH_REQUEST);
        }

        // 외부 Provider 호출은 트랜잭션 밖
        String accessToken = strategy.exchangeCodeForToken(code, state);
        OAuthUserInfo userInfo = strategy.fetchUserInfo(accessToken);
        if (userInfo.email() == null || userInfo.email().isBlank()) {
            throw new AuthException(AuthErrorCode.OAUTH_EMAIL_NOT_PROVIDED);
        }

        // DB 변경만 트랜잭션 안
        Member member = loginOrSignup(userInfo);

        CustomUserDetails userDetails =
                new CustomUserDetails(member.getEmail(), member.getPassword(), member.getRole());
        String access = jwtUtil.createJwtAccessToken(userDetails);
        String refresh = jwtUtil.createJwtRefreshToken(userDetails);
        return JwtDTO.builder().accessToken(access).refreshToken(refresh).build();
    }

    private Member loginOrSignup(OAuthUserInfo userInfo) {
        return transactionTemplate.execute(status ->
                socialAccountRepository
                        .findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                        .map(SocialAccount::getMember)
                        .orElseGet(() -> signup(userInfo))
        );
    }

    private Member signup(OAuthUserInfo userInfo) {
        // Member.email 은 전역 unique 라 soft-delete 회원도 충돌 대상이다(일반 가입과 통일)
        if (memberRepository.existsByEmail(userInfo.email())) {
            throw new AuthException(AuthErrorCode.OAUTH_EMAIL_CONFLICT);
        }
        // 스키마 호환용 비밀번호. 사용자가 아는 값이 아니며 일반 로그인 불가.
        String schemaCompatPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        Member member = memberRepository.save(OAuthConverter.toMember(userInfo, schemaCompatPassword));
        socialAccountRepository.save(OAuthConverter.toSocialAccount(userInfo, member));
        log.info("[OAuth] 신규 소셜 회원 가입 provider={} email={}", userInfo.provider(), userInfo.email());
        return member;
    }

    private OAuthStrategy getStrategy(Provider provider) {
        OAuthStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new AuthException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return strategy;
    }

    private AuthErrorCode invalidStateCode(Provider provider) {
        return provider == Provider.KAKAO
                ? AuthErrorCode.KAKAO_INVALID_STATE
                : AuthErrorCode.NAVER_INVALID_STATE;
    }

    private String stateKey(Provider provider) {
        return "OAUTH_STATE_" + provider.name();
    }
}
