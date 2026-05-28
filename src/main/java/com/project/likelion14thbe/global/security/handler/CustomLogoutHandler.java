package com.project.likelion14thbe.global.security.handler;

import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.repository.TokenRepository;
import com.project.likelion14thbe.global.security.jwt.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        log.info("[ CustomLogoutHandler ] 로그아웃 처리를 시작합니다.");

        String accessToken = jwtUtil.resolveAccessToken(request);

        if (accessToken == null) {
            log.warn("[ CustomLogoutHandler ] Access Token 이 존재하지 않습니다.");
            return;
        }

        try {
            jwtUtil.validateToken(accessToken);
            String email = jwtUtil.getEmail(accessToken);
            log.info("[ CustomLogoutHandler ] 로그아웃 대상 email = {}", email);

            // DB의 Refresh Token 삭제
            tokenRepository.findByEmail(email).ifPresent(token -> {
                tokenRepository.deleteByEmail(email);
                log.info("[ CustomLogoutHandler ] DB 에서 Refresh Token 삭제 완료");
            });

            //  Access Token 블랙리스트 등록 (남은 만료시간만큼 TTL 설정)
            long remainMillis = jwtUtil.getRemainingExpiration(accessToken);
            tokenBlacklistService.addToBlacklist(accessToken, remainMillis);
            log.info("[ CustomLogoutHandler ] Access Token 블랙리스트 등록 완료 (TTL={}ms)", remainMillis);

        } catch (Exception e) {
            log.warn("[ CustomLogoutHandler ] 로그아웃 중 토큰 처리 오류: {}", e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
            log.info("[ CustomLogoutHandler ] SecurityContext 클리어 완료");
        }
    }
}
