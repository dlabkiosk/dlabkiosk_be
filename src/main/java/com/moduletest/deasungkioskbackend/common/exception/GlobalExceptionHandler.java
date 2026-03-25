package com.moduletest.deasungkioskbackend.common.exception;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.student.exception.MultipleStudentsException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(
                        "INVALID_INPUT_VALUE",
                        "입력값 검증에 실패했습니다: " + errors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        log.warn("Constraint violation: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(
                        "INVALID_INPUT_VALUE",
                        "제약조건 위반: " + errors
                ));
    }

    @ExceptionHandler(MultipleStudentsException.class)
    public ResponseEntity<CommonResponse<Object>> handleMultipleStudents(
            MultipleStudentsException ex) {

        log.warn("Multiple students found by phone last 4 digits: {} candidates",
                ex.getCandidates().size());

        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonResponse.error(errorCode, ex.getCandidates()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Object>> handleBusinessException(
            BusinessException ex) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Business exception - Code: {}, Message: {}",
                errorCode.getCode(), ex.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonResponse.error(errorCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(
                        "INVALID_INPUT_VALUE",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CommonResponse<Object>> handleIllegalState(
            IllegalStateException ex) {

        log.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(CommonResponse.error(
                        "INVALID_STATE",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        log.warn("JSON parsing error: {}", ex.getMessage());

        String message = "잘못된 요청 형식입니다.";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Unrecognized character escape")) {
                message = "JSON 형식 오류: 특수문자를 올바르게 escape 해주세요.";
            } else if (ex.getMessage().contains("Unrecognized field")) {
                message = "JSON 형식 오류: 올바르지 않은 필드입니다.";
            } else if (ex.getMessage().contains("Cannot deserialize")) {
                message = "JSON 형식 오류: 올바르지 않은 데이터 타입입니다.";
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(
                        "INVALID_INPUT_VALUE",
                        message
                ));
    }

    @ExceptionHandler(DsaApiException.class)
    public ResponseEntity<CommonResponse<Object>> handleDsaApiException(
            DsaApiException ex) {

        log.error("DSA API error - code: {}, message: {}", ex.getDsaCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(CommonResponse.error(
                        "DSA_API_ERROR",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Object>> handleException(Exception ex) {

        log.error("Unexpected exception occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(
                        "INTERNAL_SERVER_ERROR",
                        "내부 서버 오류가 발생했습니다."
                ));
    }
}
