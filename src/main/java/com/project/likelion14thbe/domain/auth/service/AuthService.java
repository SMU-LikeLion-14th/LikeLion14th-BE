
package com.project.likelion14thbe.domain.auth.service;


import com.project.likelion14thbe.domain.auth.exception.AuthErrorCode;
import com.project.likelion14thbe.domain.auth.exception.AuthException;
import com.project.likelion14thbe.global.security.jwt.JwtUtil;
import com.project.likelion14thbe.global.security.jwt.dto.JwtDTO;
import com.project.likelion14thbe.global.security.jwt.entity.Token;
import com.project.likelion14thbe.global.security.jwt.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SignatureException;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    public JwtDTO reissueToken(JwtDTO jwtDto) {

        log.info("[ Auth Service ] 토큰 재발급을 시작합니다.");
        String accessToken = jwtDto.accessToken();
        String refreshToken = jwtDto.refreshToken();

        //Access Token 으로부터 사용자 Email 추출
        String email = jwtUtil.getEmail(refreshToken);
        log.info("[ Auth Service ] Email ---> {}", email);

        //Access Token 에서의 Email 로 부터 DB 에 저장된 Refresh Token 가져오기
        Token refreshTokenByDB = tokenRepository.findByEmail(email).orElseThrow(
                () -> new AuthException(AuthErrorCode._INVALID_TOKEN)
        );

        //Refresh Token 이 유효한지 검사
        jwtUtil.validateToken(refreshToken);

        log.info("[ Auth Service ] Refresh Token 이 유효합니다.");

        //만약 DB 에서 찾은 Refresh Token 과 파라미터로 온 Refresh Token 이 일치하면 새로운 토큰 발급
        if (refreshTokenByDB.getToken().equals(refreshToken)) {
            log.info("[ Auth Service ] 토큰을 재발급합니다.");
            try {
                return jwtUtil.reissueToken(refreshToken);
            } catch (SignatureException e) {
                throw new AuthException(AuthErrorCode._INVALID_TOKEN);
            }
        } else {
            throw new AuthException(AuthErrorCode._INVALID_TOKEN);
        }
    }
}