package com.project.likelion14thbe.domain.auth.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    WRONG_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH401_1", "이메일 또는 비밀번호가 잘못되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_2", "잘못된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_3", "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401_4", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH403_1", "권한이 없습니다."),
    MEMBER_NOT_FOUND_AUTH(HttpStatus.NOT_FOUND, "AUTH404_1", "계정을 찾을 수 없습니다."),
    BODY_PARSE_ERROR(HttpStatus.BAD_REQUEST, "AUTH400_1", "Request Body 파싱 중 오류가 발생했습니다."),

    // KAKAO (Provider별 코드)
    KAKAO_INVALID_STATE(HttpStatus.BAD_REQUEST, "KAKAO_001", "유효하지 않은 요청입니다."),
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_002", "카카오 소셜 로그인 토큰 발급에 실패했습니다."),
    KAKAO_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_003", "카카오 소셜 로그인 사용자 정보 조회에 실패했습니다."),

    // NAVER (Provider별 코드)
    NAVER_INVALID_STATE(HttpStatus.BAD_REQUEST, "NAVER_001", "유효하지 않은 요청입니다."),
    NAVER_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "NAVER_002", "네이버 소셜 로그인 토큰 발급에 실패했습니다."),
    NAVER_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "NAVER_003", "네이버 소셜 로그인 사용자 정보 조회에 실패했습니다."),

    // 공통 OAuth (이 레포 정본: 도메인PREFIX + HTTP + SEQ)
    INVALID_OAUTH_REQUEST(HttpStatus.BAD_REQUEST, "AUTH400_2", "잘못된 OAuth 요청입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH400_3", "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "AUTH400_4", "소셜 계정에서 이메일을 제공받지 못했습니다."),
    OAUTH_EMAIL_CONFLICT(HttpStatus.CONFLICT, "AUTH409_1", "이미 동일 이메일로 가입된 계정이 있습니다."),
    OAUTH_ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "AUTH401_5", "소셜 로그인 동의가 거부되었습니다."),
    INVALIDATED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_6", "로그아웃되었거나 무효화된 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
