package com.project.likelion14thbe.domain.order.exception;

import com.project.likelion14thbe.global.apiPayload.exception.CustomException;

public class OrderException extends CustomException {
    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}