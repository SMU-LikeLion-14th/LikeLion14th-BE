package com.project.likelion14thbe.global.security.handler;

import com.project.likelion14thbe.domain.auth.repository.TokenRepository;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final TokenInvalidationService tokenInvalidationService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        log.info("[ Logout Handler ] 로그아웃 처리 시작");

        String accessToken = jwtUtil.resolveAccessToken(request);
        if (accessToken == null) {
            log.warn("[ Logout Handler ] Access Token이 없어 처리를 종료합니다.");
            return;
        }

        try {
            String email = jwtUtil.getEmail(accessToken);
            tokenRepository.deleteById(email);
            tokenInvalidationService.invalidateUser(email);
            log.info("[ Logout Handler ] Refresh Token 삭제 및 무효화 컷오프 기록 완료 : {}", email);
        } catch (Exception e) {
            log.warn("[ Logout Handler ] Refresh Token 삭제 실패 : {}", e.getMessage());
        }
    }
}
