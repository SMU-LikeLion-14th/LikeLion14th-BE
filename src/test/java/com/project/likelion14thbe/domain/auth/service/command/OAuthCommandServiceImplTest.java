package com.project.likelion14thbe.domain.auth.service.command;

import com.project.likelion14thbe.domain.auth.dto.oauth.OAuthUserInfo;
import com.project.likelion14thbe.domain.auth.dto.response.JwtDTO;
import com.project.likelion14thbe.domain.auth.entity.SocialAccount;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.auth.exception.AuthErrorCode;
import com.project.likelion14thbe.domain.auth.exception.AuthException;
import com.project.likelion14thbe.domain.auth.repository.SocialAccountRepository;
import com.project.likelion14thbe.domain.auth.strategy.OAuthStrategy;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthCommandServiceImplTest {

    private SocialAccountRepository socialAccountRepository;
    private MemberRepository memberRepository;
    private JwtUtil jwtUtil;
    private OAuthStrategy kakaoStrategy;
    private OAuthCommandServiceImpl service;

    private final OAuthUserInfo userInfo = OAuthUserInfo.builder()
            .provider(Provider.KAKAO).providerId("123")
            .email("user@kakao.com").nickname("규언").profileImage("img").build();

    @BeforeEach
    void setUp() {
        socialAccountRepository = mock(SocialAccountRepository.class);
        memberRepository = mock(MemberRepository.class);
        jwtUtil = mock(JwtUtil.class);
        kakaoStrategy = mock(OAuthStrategy.class);

        when(kakaoStrategy.getProvider()).thenReturn(Provider.KAKAO);
        when(kakaoStrategy.exchangeCodeForToken(any(), any())).thenReturn("kakao-access-token");
        when(kakaoStrategy.fetchUserInfo(any())).thenReturn(userInfo);
        when(jwtUtil.createJwtAccessToken(any())).thenReturn("ACCESS");
        when(jwtUtil.createJwtRefreshToken(any())).thenReturn("REFRESH");

        PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
        when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new OAuthCommandServiceImpl(
                List.of(kakaoStrategy), socialAccountRepository, memberRepository,
                jwtUtil, new BCryptPasswordEncoder(), txManager);
    }

    private HttpSession sessionWithState(String state) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("OAUTH_STATE_KAKAO")).thenReturn(state);
        return session;
    }

    @Test
    void state가_불일치하면_예외() {
        HttpSession session = sessionWithState("expected");
        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, "code", "wrong", null, session))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.KAKAO_INVALID_STATE);
    }

    @Test
    void 기존_소셜계정이면_로그인하고_JwtDTO를_반환한다() {
        Member member = Member.builder().name("규언").email("user@kakao.com")
                .password("x").role(Role.ROLE_USER).build();
        SocialAccount sa = SocialAccount.builder()
                .provider(Provider.KAKAO).providerId("123").member(member).build();
        when(socialAccountRepository.findByProviderAndProviderId(Provider.KAKAO, "123"))
                .thenReturn(Optional.of(sa));

        JwtDTO result = service.handleCallback(Provider.KAKAO, "code", "s", null, sessionWithState("s"));

        assertThat(result.accessToken()).isEqualTo("ACCESS");
        assertThat(result.refreshToken()).isEqualTo("REFRESH");
    }

    @Test
    void 소셜계정도_email회원도_없으면_신규가입한다() {
        when(socialAccountRepository.findByProviderAndProviderId(Provider.KAKAO, "123"))
                .thenReturn(Optional.empty());
        when(memberRepository.existsByEmail("user@kakao.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        JwtDTO result = service.handleCallback(Provider.KAKAO, "code", "s", null, sessionWithState("s"));

        assertThat(result.accessToken()).isEqualTo("ACCESS");
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }

    @Test
    void 소셜계정은_없는데_같은email_로컬회원이_있으면_충돌예외() {
        when(socialAccountRepository.findByProviderAndProviderId(Provider.KAKAO, "123"))
                .thenReturn(Optional.empty());
        when(memberRepository.existsByEmail("user@kakao.com")).thenReturn(true);

        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, "code", "s", null, sessionWithState("s")))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void error_파라미터가_있으면_접근거부_예외() {
        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, null, "s", "access_denied", sessionWithState("s")))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.OAUTH_ACCESS_DENIED);
    }

    @Test
    void code도_없고_error도_없으면_잘못된요청_예외() {
        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, null, "s", null, sessionWithState("s")))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.INVALID_OAUTH_REQUEST);
    }
}
