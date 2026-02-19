package com.moduletest.deasungkioskbackend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("V001", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
