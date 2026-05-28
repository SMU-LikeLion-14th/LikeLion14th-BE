package com.project.likelion14thbe.global.apiPayload.code;

import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();

    default CustomResponse<Void> getErrorResponse() {
        return CustomResponse.onFailure(getCode(), getMessage());
    }
}
