package com.project.likelion14thbe.global.security.handler;

import com.project.likelion14thbe.domain.auth.repository.TokenRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomLogoutHandlerTest {

    @Test
    void logout_recordsInvalidationCutoffForUser() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenRepository tokenRepository = mock(TokenRepository.class);
        TokenInvalidationService invalidationService = mock(TokenInvalidationService.class);

        when(jwtUtil.resolveAccessToken(org.mockito.ArgumentMatchers.any())).thenReturn("tok");
        when(jwtUtil.getEmail("tok")).thenReturn("kim@test.com");

        CustomLogoutHandler handler =
                new CustomLogoutHandler(jwtUtil, tokenRepository, invalidationService);

        handler.logout(new MockHttpServletRequest(), new MockHttpServletResponse(), null);

        verify(tokenRepository).deleteById("kim@test.com");
        verify(invalidationService).invalidateUser("kim@test.com");
    }
}
