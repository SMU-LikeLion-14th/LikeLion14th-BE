package com.project.likelion14thbe.global.security.jwt;

import com.project.likelion14thbe.domain.auth.entity.Token;
import com.project.likelion14thbe.domain.auth.repository.TokenRepository;
import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long accessExpMs;
    private final Long refreshExpMs;
    private final TokenRepository tokenRepository;

    public JwtUtil(
            @Value("${spring.jwt.secret:dGhpc2lzYWxvbmdlbm91Z2hzZWNyZXRrZXlmb3JqdzI1NmFsZ29yaXRobTEyMzQ1Njc4OTA=}") String secret,
            @Value("${spring.jwt.token.access-expiration-time:1800000}") Long access,
            @Value("${spring.jwt.token.refresh-expiration-time:1209600000}") Long refresh,
            TokenRepository tokenRepository
    ) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.accessExpMs = access;
        this.refreshExpMs = refresh;
        this.tokenRepository = tokenRepository;
    }

    // JWT 토큰을 입력으로 받아 subject(email)을 추출
    public String getEmail(String token) {
        log.info("[ JwtUtil ] 토큰에서 이메일을 추출합니다.");
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new SecurityException("잘못된 토큰입니다.");
        }
    }

    // JWT 토큰의 발급 시각(iat)을 epoch millis로 추출
    public long getIssuedAt(String token) {
        log.info("[ JwtUtil ] 토큰에서 발급 시각을 추출합니다.");
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getIssuedAt()
                    .getTime();
        } catch (Exception e) {
            throw new SecurityException("잘못된 토큰입니다.");
        }
    }

    // JWT 토큰에서 사용자 권한(role) 추출
    public Role getRoles(String token) {
        log.info("[ JwtUtil ] 토큰에서 권한을 추출합니다.");
        try {
            String roleStr = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
            return Role.valueOf(roleStr);
        } catch (Exception e) {
            throw new SecurityException("잘못된 토큰입니다.");
        }
    }

    // 토큰 발급 메서드 (subject = email, claim "role" = ROLE_USER 등)
    public String tokenProvider(CustomUserDetails customUserDetails, Instant expiration) {
        log.info("[ JwtUtil ] 토큰을 새로 생성합니다.");
        Instant issuedAt = Instant.now();

        String authorities = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(customUserDetails.getUsername())
                .claim("role", authorities)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // Access Token 발급
    public String createJwtAccessToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(accessExpMs);
        return tokenProvider(customUserDetails, expiration);
    }

    // Refresh Token 발급 + DB 저장
    public String createJwtRefreshToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(refreshExpMs);
        String refreshToken = tokenProvider(customUserDetails, expiration);

        String email = customUserDetails.getUsername();
        tokenRepository.findByEmail(email)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken),
                        () -> tokenRepository.save(Token.builder()
                                .email(email)
                                .token(refreshToken)
                                .build())
                );

        return refreshToken;
    }

    // HTTP 요청의 Authorization 헤더에서 Bearer 토큰 추출
    public String resolveAccessToken(HttpServletRequest request) {
        log.info("[ JwtUtil ] 헤더에서 토큰을 추출합니다.");
        String tokenFromHeader = request.getHeader("Authorization");

        if (tokenFromHeader == null || !tokenFromHeader.startsWith("Bearer ")) {
            log.warn("[ JwtUtil ] Request Header 에 토큰이 존재하지 않습니다.");
            return null;
        }
        return tokenFromHeader.split(" ")[1];
    }

    // 토큰 유효성 검증 (서명 + 만료)
    public void validateToken(String token) {
        log.info("[ JwtUtil ] 토큰의 유효성을 검증합니다.");
        try {
            // 시스템 시계 오차 3분 허용
            long seconds = 3 * 60;
            boolean isExpired = Jwts.parser()
                    .clockSkewSeconds(seconds)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
            if (isExpired) {
                log.info("만료된 JWT 토큰입니다.");
            }
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new SecurityException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "만료된 JWT 토큰입니다.");
        }
    }
}
