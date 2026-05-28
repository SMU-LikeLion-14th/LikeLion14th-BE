package com.project.likelion14thbe.domain.order.exception;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER404_1", "주문을 찾을 수 없습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER403_1", "해당 주문에 대한 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}