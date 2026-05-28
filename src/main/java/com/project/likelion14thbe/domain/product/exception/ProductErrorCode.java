package com.project.likelion14thbe.domain.product.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT404_1", "상품을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}