package com.project.likelion14thbe.domain.category.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY404_1", "카테고리를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}