package com.project.likelion14thbe.global.security.filter;

import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.service.TokenBlacklistService;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// ... existing code ...

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService; // ✅ 추가

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("[ JwtAuthorizationFilter ] 인가 필터 작동");

        try {
            // 1. Request에서 Access Token 추출
            String accessToken = jwtUtil.resolveAccessToken(request);


            // 2. Access Token이 없으면 다음 필터로 바로 진행
            if (accessToken == null) {
                log.debug("[ JwtAuthorizationFilter ] Access Token 없음, 다음 필터로 진행");
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Access Token을 이용한 인증 처리
            authenticateAccessToken(accessToken);
            log.debug("[ JwtAuthorizationFilter ] 인증 완료. 다음 필터로 넘어갑니다.");

            // ⭐ 정상 흐름에서도 반드시 다음 필터로 넘겨야 함
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 4. 토큰 만료 시 401 응답 처리
            logger.warn("[ JwtAuthorizationFilter ] accessToken 이 만료되었습니다.");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("Access Token 이 만료되었습니다.");
        } catch (IllegalStateException e) { // ✅ 블랙리스트 예외 처리
            logger.warn("[ JwtAuthorizationFilter ] 로그아웃된 Access Token 입니다.");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("로그아웃된 Access Token 입니다. 다시 로그인 해주세요.");
        }
    }

    private void authenticateAccessToken(String accessToken) {
        log.info("[ JwtAuthorizationFilter ] 토큰으로 인가 과정을 시작합니다. ");

        // 1. Access Token의 유효성 검증 (시그니처, 만료 등)
        jwtUtil.validateToken(accessToken);
        log.info("[ JwtAuthorizationFilter ] Access Token 유효성 검증 성공. ");

        // ✅ 1-1. 블랙리스트 검사 (로그아웃 처리된 토큰인지 확인)
        if (tokenBlacklistService.isBlacklisted(accessToken)) {
            log.warn("[ JwtAuthorizationFilter ] 블랙리스트에 등록된 Access Token 입니다.");
            throw new IllegalStateException("로그아웃된 Access Token 입니다.");
        }

        // 2. Access Token에서 사용자 정보 추출 후 CustomUserDetails 생성
        Long memberId = jwtUtil.getMemberId(accessToken);
        String email = jwtUtil.getEmail(accessToken);
        Role role = jwtUtil.getRoles(accessToken);

        CustomUserDetails customUserDetails = new CustomUserDetails(memberId, email, null, role);
        log.info("[ JwtAuthorizationFilter ] UserDetails 객체 생성 성공");

        // 3. 인증 객체 생성 및 저장
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[ JwtAuthorizationFilter ] 인증 객체 저장 완료");
    }
}