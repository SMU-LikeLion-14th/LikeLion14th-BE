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
import org.springframework.transaction.PlatformTransactionManager;

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
 * <p><b>회귀 가드</b>: loginOrSignup 은 TransactionTemplate 으로 명시 트랜잭션을 연다.
 * signup() 의 두 번째 save(SocialAccount) 가 실패하면 첫 번째 save(Member) 도 함께
 * 롤백되어 반쪽 가입(orphan)이 남지 않아야 한다. 누군가 트랜잭션을 제거하거나 자기호출
 * 패턴으로 되돌리면 Member 만 커밋되어 본 테스트가 RED 로 뒤집힌다.
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

    @Autowired
    PlatformTransactionManager transactionManager;

    // social_account.provider_id 컬럼 한계(length=100) 초과 → 두 번째 save 가 결정적으로 실패
    private static final String OVERLONG_PROVIDER_ID = "k".repeat(150);
    private static final String ORPHAN_EMAIL = "orphan@kakao.com";

    @AfterEach
    void cleanUp() {
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("signup 의 SocialAccount save 가 실패하면 Member 도 함께 롤백되어 반쪽 가입이 남지 않는다 — 원자성 보장")
    void signup_isAtomic_rollsBackMemberWhenSocialAccountSaveFails() {
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

        // 실 레포 빈 + 실 JwtUtil/Encoder + 실 트랜잭션 매니저 + mock 전략으로 서비스 수동 구성
        OAuthCommandServiceImpl service = new OAuthCommandServiceImpl(
                List.of(kakaoStrategy),
                socialAccountRepository,
                memberRepository,
                jwtUtil,
                passwordEncoder,
                transactionManager);

        String state = "test-state";
        HttpSession session = new MockHttpSession();
        session.setAttribute("OAUTH_STATE_KAKAO", state);

        // when: 콜백 처리 → signup 진입 → SocialAccount save 가 컬럼 한계로 실패 → 트랜잭션 롤백
        assertThatThrownBy(() ->
                service.handleCallback(Provider.KAKAO, "auth-code", state, null, session))
                .isInstanceOf(DataIntegrityViolationException.class);

        // then: 트랜잭션 롤백으로 Member 와 SocialAccount 모두 없다 — 반쪽 가입 없음
        assertThat(memberRepository.findByEmailAndNotDeleted(ORPHAN_EMAIL))
                .as("원자성 보장으로 Member 도 롤백되어 남지 않는다")
                .isEmpty();
        assertThat(socialAccountRepository.findByProviderAndProviderId(Provider.KAKAO, OVERLONG_PROVIDER_ID))
                .as("SocialAccount 도 저장 실패 — 둘 다 부재")
                .isEmpty();
    }
}
