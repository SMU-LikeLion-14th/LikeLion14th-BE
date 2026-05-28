package com.project.likelion14thbe.global.apiPayload.exception.handler;

import com.project.likelion14thbe.global.apiPayload.code.BaseErrorCode;
import com.project.likelion14thbe.global.apiPayload.code.GeneralErrorCode;
import com.project.likelion14thbe.global.apiPayload.exception.CustomException;
import com.project.likelion14thbe.global.apiPayload.exception.CustomResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //애플리케이션에서 발생하는 커스텀 예외를 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomResponse<Void>> handleCustomException(CustomException ex) {
        //예외가 발생하면 로그 기록
        log.warn("[ CustomException ]: {}", ex.getCode().getMessage());
        //커스텀 예외에 정의된 에러 코드와 메시지를 포함한 응답 제공
        return ResponseEntity.status(ex.getCode().getHttpStatus())
                .body(ex.getCode().getErrorResponse());
    }
    // @Valid 또는 @Validated유효성 검증 실패 시 발생(주로 DTO검증)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("[ MethodArgumentNotValidException ]: 유효성 검증 실패");

        // 에러가 발생한 필드와 메시지를 Map으로 수집 (예: {"email": "이메일 형식이 아닙니다."})
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "올바르지 않은 값입니다.",
                        (existingValue, newValue) -> existingValue // 중복 필드 에러 발생 시 첫 번째 에러 유지
                ));

        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                "COMMON_400",
                "입력값 검증에 실패했습니다.",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    // 제약요건 위반 시 발생
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("[ ConstraintViolationException ]: 제약 조건 위반");

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        });

        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                "COMMON_400",
                "요청 파라미터 제약 조건 위반입니다.",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler({Exception.class})
    public ResponseEntity<CustomResponse<String>> handleAllException(Exception ex) {
        log.error("[WARNING] Internal Server Error : {} ", ex.getMessage());
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR_500;
        CustomResponse<String> errorResponse = CustomResponse.onFailure(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}
