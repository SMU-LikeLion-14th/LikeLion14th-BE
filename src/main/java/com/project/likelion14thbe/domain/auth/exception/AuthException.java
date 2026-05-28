package com.project.likelion14thbe.domain.auth.exception;

import com.project.likelion14thbe.global.apiPayload.exception.CustomException;

public class AuthException extends CustomException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}