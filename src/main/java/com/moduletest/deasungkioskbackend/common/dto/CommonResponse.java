package com.moduletest.deasungkioskbackend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null);
    }

    public static <T> CommonResponse<T> error(ErrorCode errorCode) {
        return new CommonResponse<>(false, null,
                new ErrorDetail(errorCode.getCode(), errorCode.getMessage()));
    }

    public static <T> CommonResponse<T> error(String code, String message) {
        return new CommonResponse<>(false, null, new ErrorDetail(code, message));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorDetail {
        private final String code;
        private final String message;
    }
}
