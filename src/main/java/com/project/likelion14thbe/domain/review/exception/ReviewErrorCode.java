package com.project.likelion14thbe.domain.review.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW404_1", "리뷰를 찾을 수 없습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW403_1", "해당 리뷰에 대한 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}