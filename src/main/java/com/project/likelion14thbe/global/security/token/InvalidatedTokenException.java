package com.project.likelion14thbe.global.security.token;

/**
 * 사용자별 컷오프에 의해 무효화된 토큰으로 접근 시 던진다.
 * JwtAuthorizationFilter가 잡아 AUTH401_6으로 응답한다.
 */
public class InvalidatedTokenException extends RuntimeException {

    public InvalidatedTokenException() {
        super("무효화된 토큰입니다.");
    }
}
