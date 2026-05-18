package com.project.likelion14thbe.global.security.filter;

import com.project.likelion14thbe.domain.auth.exception.AuthErrorCode;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.token.InvalidatedTokenException;
import com.project.likelion14thbe.global.security.token.TokenInvalidationService;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import com.project.likelion14thbe.global.security.utils.HttpResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenInvalidationService tokenInvalidationService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("[ JwtAuthorizationFilter ] 인가 필터 작동");

        try {
            String accessToken = jwtUtil.resolveAccessToken(request);

            if (accessToken == null) {
                log.info("[ JwtAuthorizationFilter ] Access Token 없음, 다음 필터로 진행");
                filterChain.doFilter(request, response);
                return;
            }

            authenticateAccessToken(accessToken);
            log.info("[ JwtAuthorizationFilter ] 종료. 다음 필터로 넘어갑니다.");
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("[ JwtAuthorizationFilter ] accessToken 이 만료되었습니다.");
            HttpResponseUtil.setErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
        } catch (InvalidatedTokenException e) {
            log.warn("[ JwtAuthorizationFilter ] 무효화된 토큰입니다.");
            HttpResponseUtil.setErrorResponse(response, AuthErrorCode.INVALIDATED_TOKEN);
        } catch (SecurityException e) {
            log.warn("[ JwtAuthorizationFilter ] 잘못된 토큰입니다.");
            HttpResponseUtil.setErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
        }
    }

    private void authenticateAccessToken(String accessToken) {
        log.info("[ JwtAuthorizationFilter ] 토큰으로 인가 과정을 시작합니다.");

        jwtUtil.validateToken(accessToken);
        log.info("[ JwtAuthorizationFilter ] Access Token 유효성 검증 성공.");

        String email = jwtUtil.getEmail(accessToken);

        long issuedAt = jwtUtil.getIssuedAt(accessToken);
        if (tokenInvalidationService.isInvalidated(email, issuedAt)) {
            log.info("[ JwtAuthorizationFilter ] 무효화된 토큰 거부 : {}", email);
            throw new InvalidatedTokenException();
        }

        Role role = jwtUtil.getRoles(accessToken);

        CustomUserDetails customUserDetails = new CustomUserDetails(email, null, role);
        log.info("[ JwtAuthorizationFilter ] UserDetails 객체 생성 성공");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[ JwtAuthorizationFilter ] 인증 객체 저장 완료");
    }
}
