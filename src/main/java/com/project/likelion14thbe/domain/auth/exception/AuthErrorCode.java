package com.project.likelion14thbe.domain.auth.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
    public enum AuthErrorCode implements BaseErrorCode {
        _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다"),
        _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403",  "접근이 금지되었습니다"),
        _NOT_FOUND(HttpStatus.NOT_FOUND,  "COMMON404", "요청한 자원을 찾을 수 없습니다"),
        _TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"COMMON_404_1","토큰을 찾을 수 없습니다"),
        _INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"COMMON_404_1","이상한 토큰입니다"),

        // 공통
        INVALID_OAUTH_REQUEST(HttpStatus.BAD_REQUEST, "AUTH400", "잘못된 OAuth 요청"),

        // KAKAO
        KAKAO_INVALID_STATE(HttpStatus.BAD_REQUEST, "KAKAO_001", "유효하지 않은 요청입니다."),
        KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_002", "카카오 소셜 로그인 토큰 발급에 실패했습니다."),
        KAKAO_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_003", "카카오 소셜 로그인 사용자 정보 조회에 실패했습니다.");

        private final HttpStatus httpStatus;
        private final String code;
        private final String message;
}
