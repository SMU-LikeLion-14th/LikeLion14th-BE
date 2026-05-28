package com.project.likelion14thbe.global.security.jwt;

import com.project.likelion14thbe.domain.member.enums.Role;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import com.project.likelion14thbe.global.security.jwt.entity.Token;
import com.project.likelion14thbe.global.security.jwt.repository.TokenRepository;
import com.project.likelion14thbe.global.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
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
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.token.access-expiration-time}") Long access,
            @Value("${spring.jwt.token.refresh-expiration-time}") Long refresh,
            TokenRepository tokenRepo
    ) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        accessExpMs = access;
        refreshExpMs = refresh;
        tokenRepository = tokenRepo;
    }

    // JWT 토큰을 입력으로 받아 토큰의 subject 로부터 사용자 Email 추출하는 메서드
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

    // JWT 토큰을 입력으로 받아 토큰의 claim 에서 사용자 권한을 추출하는 메서드
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

    // JWT 토큰에서 memberId 추출
    public Long getMemberId(String token) {
        log.info("[ JwtUtil ] 토큰에서 memberId 를 추출합니다.");
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("memberId", Long.class);
        } catch (Exception e) {
            throw new SecurityException("잘못된 토큰입니다.");
        }
    }

    // Token 발급하는 메서드
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
                .claim("memberId", customUserDetails.getMemberId())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // principalDetails 객체에 대해 새로운 JWT 액세스 토큰을 생성
    public String createJwtAccessToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(accessExpMs);
        return tokenProvider(customUserDetails, expiration);
    }

    // principalDetails 객체에 대해 새로운 JWT 리프레시 토큰을 생성
    public String createJwtRefreshToken(CustomUserDetails customUserDetails) {
        Instant expiration = Instant.now().plusMillis(refreshExpMs);
        String refreshToken = tokenProvider(customUserDetails, expiration);

        // DB에 Refresh Token 저장
        tokenRepository.save(Token.builder()
                .email(customUserDetails.getUsername())
                .token(refreshToken)
                .build()
        );


        return refreshToken;
    }

    // 제공된 리프레시 토큰을 기반으로 JwtDto 쌍을 다시 발급
    public JwtDTO reissueToken(String refreshToken) throws SignatureException {

        CustomUserDetails userDetails = new CustomUserDetails(
                getMemberId(refreshToken),
                getEmail(refreshToken),
                null,
                getRoles(refreshToken)
        );
        log.info("[ JwtUtil ] 새로운 토큰을 재발급 합니다.");

        return new JwtDTO(
                createJwtAccessToken(userDetails),
                createJwtRefreshToken(userDetails)
        );
    }

    // HTTP 요청의 'Authorization' 헤더에서 JWT 액세스 토큰을 검색
    public String resolveAccessToken(HttpServletRequest request) {
        String tokenFromHeader = request.getHeader("Authorization");

        if (tokenFromHeader == null || !tokenFromHeader.startsWith("Bearer ")) {
            log.debug("[ JwtUtil ] Request Header 에 토큰이 존재하지 않습니다.");
            return null;
        }

        log.debug("[ JwtUtil ] 헤더에 토큰이 존재합니다.");

        return tokenFromHeader.split(" ")[1]; //Bearer 와 분리
    }


    public void validateToken(String token) {
        log.info("[ JwtUtil ] 토큰의 유효성을 검증합니다.");
        try {
            // 구문 분석 시스템의 시계가 JWT를 생성한 시스템의 시계 오차 고려
            // 약 3분 허용.
            long seconds = 3 *60;
            boolean isExpired = Jwts
                    .parser()
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
            //원하는 Exception throw
            throw new SecurityException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            //원하는 Exception throw
            throw new ExpiredJwtException(null, null, "만료된 JWT 토큰입니다.");
        }
    }

    public long getRemainingExpiration(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
