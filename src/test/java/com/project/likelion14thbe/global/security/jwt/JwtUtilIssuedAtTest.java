package com.project.likelion14thbe.global.security.jwt;

import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilIssuedAtTest {

    private static final String SECRET =
            "dGhpc2lzYWxvbmdlbm91Z2hzZWNyZXRrZXlmb3JqdzI1NmFsZ29yaXRobTEyMzQ1Njc4OTA=";

    @Test
    void getIssuedAt_returnsEpochMillisCloseToNow() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 1_800_000L, 1_209_600_000L, null);
        CustomUserDetails user = new CustomUserDetails("kim@test.com", null, Role.ROLE_USER);

        long before = System.currentTimeMillis();
        String token = jwtUtil.createJwtAccessToken(user);
        long after = System.currentTimeMillis();

        long iat = jwtUtil.getIssuedAt(token);

        // JWT iat는 초 단위로 절삭되므로 1초 여유로 경계 비교
        assertThat(iat).isBetween(before - 1000, after + 1000);
    }
}
