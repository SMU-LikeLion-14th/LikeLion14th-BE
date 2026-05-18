package com.project.likelion14thbe.domain.auth.service.command;

import com.project.likelion14thbe.domain.auth.dto.oauth.OAuthUserInfo;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.auth.repository.SocialAccountRepository;
import com.project.likelion14thbe.domain.auth.strategy.OAuthStrategy;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.support.AbstractDbIntegrationTest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * goal #2 — Member 와 SocialAccount 가 함께 저장될 때 트랜잭션 경계가 어떻게 동작하는지
 * 실 MySQL 로 검증한다.
 *
 * <p><b>characterization 테스트(현재 사실 박제)</b>: OAuthCommandServiceImpl.loginOrSignup
 * 은 {@code @Transactional protected} 이지만 같은 빈의 handleCallback 에서 this. 로
 * 자기호출(self-invocation)되어 Spring AOP 프록시가 어드바이스를 적용하지 못한다.
 * 그 결과 signup() 의 memberRepository.save 와 socialAccountRepository.save 가
 * 각자 독립 auto-commit 되어 <b>두 번째 save 실패 시 Member 만 커밋된 반쪽 가입(orphan)</b>
 * 이 발생한다. 본 테스트는 이 현재 동작을 사실 그대로 GREEN 으로 박제한다(가짜 통과 아님).
 *
 * <p>self-invocation 으로 인한 비원자성은 결함으로 식별되어 DONE.md / 메모리에 별도
 * finding 으로 기록되며, 수정은 본 Phase 비목표이므로 후속 Phase 후보로만 남긴다.
 *
 * <p>회귀 시뮬레이션 ABC: signup 을 별 빈 + REQUIRES_NEW 등으로 실제 원자 트랜잭션으로
 * 감싸면 orphan 이 발생하지 않아 아래 isPresent 단언이 RED 가 된다. 즉 미래에 누군가
 * 원자성을 부여하면 본 테스트가 즉시 RED 로 뒤집혀 행위 변경을 신호한다(회귀 가드).
 * (무력화/수정 편집은 커밋하지 않는다.)
 *
 * <p>서비스 인스턴스는 수동 {@code new} 로 구성한다. {@code @Autowired} 프록시 빈으로
 * loginOrSignup 을 외부 호출하면 프록시가 @Transactional 을 적용해 결함이 사라지므로,
 * 프로덕션의 self-invocation 효과를 충실히 재현하려면 plain object 여야 한다.
 */
@SpringBootTest
class OAuthSignupTransactionBoundaryIntegrationTest extends AbstractDbIntegrationTest {

    @Autowired
    SocialAccountRepository socialAccountRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    // social_account.provider_id 컬럼 한계(length=100) 초과 → 두 번째 save 가 결정적으로 실패
    private static final String OVERLONG_PROVIDER_ID = "k".repeat(150);
    private static final String ORPHAN_EMAIL = "orphan@kakao.com";

    @AfterEach
    void cleanUp() {
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("signup 의 SocialAccount save 가 실패해도 Member 는 커밋되어 반쪽 가입(orphan)이 남는다 — 현재 비원자적")
    void signup_isNotAtomic_leavesOrphanMemberWhenSocialAccountSaveFails() {
        // given: KAKAO 전략 mock — 정상 토큰/사용자정보 반환하되 providerId 가 컬럼 한계를 초과
        OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .provider(Provider.KAKAO)
                .providerId(OVERLONG_PROVIDER_ID)
                .email(ORPHAN_EMAIL)
                .nickname("orphan")
                .profileImage("img")
                .build();

        OAuthStrategy kakaoStrategy = mock(OAuthStrategy.class);
        when(kakaoStrategy.getProvider()).thenReturn(Provider.KAKAO);
        when(kakaoStrategy.exchangeCodeForToken(any(), any())).thenReturn("kakao-access-token");
        when(kakaoStrategy.fetchUserInfo(any())).thenReturn(userInfo);

        // 실 레포 빈 + 실 JwtUtil/Encoder + mock 전략으로 서비스 수동 구성 (plain object)
        OAuthCommandServiceImpl service = new OAuthCommandServiceImpl(
                List.of(kakaoStrategy),
                socialAccountRepository,
                memberRepository,
                jwtUtil,
                passwordEncoder);

        String state = "test-state";
        HttpSession session = new MockHttpSession();
        session.setAttribute("OAUTH_STATE_KAKAO", state);

        // when: 콜백 처리 → signup 진입 → Member save 커밋 후 SocialAccount save 가 컬럼 한계로 실패
        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, "auth-code", state, null, session))
                .isInstanceOf(DataIntegrityViolationException.class);

        // then(characterization): Member 는 커밋되어 남아 있고(orphan), 그 SocialAccount 는 없다
        assertThat(memberRepository.findByEmailAndNotDeleted(ORPHAN_EMAIL))
                .as("self-invocation 으로 트랜잭션이 무효라 Member 만 커밋된 반쪽 가입이 남는다")
                .isPresent();
        assertThat(socialAccountRepository.findByProviderAndProviderId(Provider.KAKAO, OVERLONG_PROVIDER_ID))
                .as("SocialAccount 는 저장 실패하여 부재 — 가입이 원자적이지 않음")
                .isEmpty();
    }
}
