package com.signaldecode.templatebackendapi.common.exception;

import com.signaldecode.templatebackendapi.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * <p>
 * 이 파일은 수정 없이 그대로 사용 가능합니다.
 * 모든 예외를 자동으로 catch하여 일관된 응답 형식으로 변환합니다.
 * <p>
 * 처리하는 예외 목록:
 * 1. MethodArgumentNotValidException - @Valid 검증 실패
 * 2. ConstraintViolationException - @Validated 제약조건 위반
 * 3. BusinessException - 비즈니스 로직 예외 (가장 중요)
 * 4. IllegalArgumentException - 잘못된 인자
 * 5. IllegalStateException - 잘못된 상태
 * 6. HttpMessageNotReadableException - JSON 파싱 오류
 * 7. Exception - 그 외 모든 예외
 * <p>
 * 주의사항:
 * - @RestControllerAdvice는 하나의 프로젝트에 여러 개 있을 수 있습니다
 * - 더 구체적인 예외 핸들러가 우선순위를 가집니다
 * - 이 클래스를 실제 프로젝트에 적용할 때는 클래스명을 변경하세요
 * (예: GlobalExceptionHandler -> SampleExceptionHandler)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * @Valid 검증 실패 처리
     * DTO의 @NotNull, @Size 등 검증 실패 시 호출됩니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Object>> handleValidationException(
        MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
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

    /**
     * @Validated 제약조건 위반 처리
     * 메서드 파라미터의 @Min, @Max 등 검증 실패 시 호출됩니다.
     */
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

    /**
     * BusinessException 처리 (핵심)
     * <p>
     * 모든 비즈니스 예외를 여기서 처리합니다.
     * ErrorCode의 HttpStatus와 메시지를 그대로 사용합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Object>> handleBusinessException(
        BusinessException ex) {

        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();

        log.warn("Business exception - Code: {}, Message: {}",
            errorCode.getCode(), ex.getMessage());

        return ResponseEntity
            .status(httpStatus)
            .body(CommonResponse.error(
                errorCode.getCode(),
                ex.getMessage()
            ));
    }

    /**
     * IllegalArgumentException 처리
     * 메서드 인자가 잘못되었을 때 발생합니다.
     */
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

    /**
     * IllegalStateException 처리
     * 객체의 상태가 메서드 호출에 적합하지 않을 때 발생합니다.
     */
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

    /**
     * JSON 파싱 오류 처리
     * 클라이언트가 잘못된 JSON을 보냈을 때 발생합니다.
     */
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

    /**
     * 그 외 모든 예외 처리
     * 위에서 처리하지 못한 모든 예외를 catch합니다.
     */
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
