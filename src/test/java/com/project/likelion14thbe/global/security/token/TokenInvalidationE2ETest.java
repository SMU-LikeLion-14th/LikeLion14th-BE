package com.project.likelion14thbe.global.security.token;

import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import com.project.likelion14thbe.support.RedisTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
// 회귀 시뮬레이션 ABC: JwtAuthorizationFilter의 isInvalidated 분기를
// `if (false && ...)`로 무력화하면 아래 두 401 단언이 RED가 되고,
// byte-identical 복원 시 GREEN으로 돌아옴을 개발 및 리뷰에서 독립 재현 확인.
// (무력화 편집은 커밋하지 않는다.)
class TokenInvalidationE2ETest extends RedisTestContainer {

    private static final String EMAIL_1 = "e2e@test.com";
    private static final String EMAIL_2 = "e2e2@test.com";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TokenInvalidationService invalidationService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 두 테스트에서 사용할 멤버를 각각 미리 저장한다.
        // e2e@test.com: protectedResource 테스트에서 GET /api/v1/members/me 가 200을 반환하려면 DB에 존재해야 한다.
        // e2e2@test.com: logout 테스트에서 POST /api/v1/auth/logout 호출 후
        //   GET /api/v1/members/me 는 필터에서 401 거부되므로 DB 조회 전에 끊기지만,
        //   일관성을 위해 함께 저장한다.
        if (!memberRepository.existsByEmail(EMAIL_1)) {
            memberRepository.save(Member.builder()
                    .email(EMAIL_1)
                    .password(passwordEncoder.encode("passw0rd!!"))
                    .name("E2E테스트1")
                    .role(Role.ROLE_USER)
                    .build());
        }
        if (!memberRepository.existsByEmail(EMAIL_2)) {
            memberRepository.save(Member.builder()
                    .email(EMAIL_2)
                    .password(passwordEncoder.encode("passw0rd!!"))
                    .name("E2E테스트2")
                    .role(Role.ROLE_USER)
                    .build());
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트 격리를 위해 두 멤버를 삭제한다.
        memberRepository.findByEmail(EMAIL_1).ifPresent(memberRepository::delete);
        memberRepository.findByEmail(EMAIL_2).ifPresent(memberRepository::delete);
    }

    private String issueAccessToken(String email) {
        return jwtUtil.createJwtAccessToken(
                new CustomUserDetails(email, null, Role.ROLE_USER));
    }

    @Test
    void protectedResource_ok_then_afterInvalidation_rejected() throws Exception {
        String token = issueAccessToken(EMAIL_1);

        // 무효화 전: 멤버가 DB에 존재하므로 200을 기대한다.
        mockMvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Thread.sleep(5);
        invalidationService.invalidateUser(EMAIL_1);

        // 무효화 후: 인가 필터가 401을 반환한다.
        mockMvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_invalidatesAccessToken() throws Exception {
        String token = issueAccessToken(EMAIL_2);
        Thread.sleep(5);

        // 로그아웃: 토큰 무효화 컷오프가 기록된다.
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 로그아웃 이후: 인가 필터가 무효화된 토큰을 거부한다.
        mockMvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
