package com.project.likelion14thbe.global.security.filter;

import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthorizationFilterInvalidationTest {

    private static final String SECRET =
            "dGhpc2lzYWxvbmdlbm91Z2hzZWNyZXRrZXlmb3JqdzI1NmFsZ29yaXRobTEyMzQ1Njc4OTA=";

    private JwtUtil jwtUtil;
    private TokenInvalidationService invalidationService;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 1_800_000L, 1_209_600_000L, null);
        invalidationService = mock(TokenInvalidationService.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void rejectsWith401_whenTokenInvalidated() throws Exception {
        String token = jwtUtil.createJwtAccessToken(
                new com.project.likelion14thbe.global.security.userdetails.CustomUserDetails(
                        "kim@test.com", null, Role.ROLE_USER));
        when(invalidationService.isInvalidated(eq("kim@test.com"), anyLong())).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        new JwtAuthorizationFilter(jwtUtil, invalidationService).doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString()).contains("AUTH401_6");
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    void passes_whenTokenNotInvalidated() throws Exception {
        String token = jwtUtil.createJwtAccessToken(
                new com.project.likelion14thbe.global.security.userdetails.CustomUserDetails(
                        "kim@test.com", null, Role.ROLE_USER));
        when(invalidationService.isInvalidated(eq("kim@test.com"), anyLong())).thenReturn(false);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        new JwtAuthorizationFilter(jwtUtil, invalidationService).doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
    }
}
