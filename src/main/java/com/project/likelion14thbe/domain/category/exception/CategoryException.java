package com.project.likelion14thbe.domain.category.exception;

import com.project.likelion14thbe.global.apiPayload.exception.CustomException;

public class CategoryException extends CustomException {
    public CategoryException(CategoryErrorCode errorCode) {
        super(errorCode);
    }
}