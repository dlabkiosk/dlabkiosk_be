package com.moduletest.deasungkioskbackend.common.dsa.exception;

import lombok.Getter;

@Getter
public class DsaApiException extends RuntimeException {

    private final int dsaCode;

    public DsaApiException(int dsaCode, String message) {
        super("DSA API 오류 [" + dsaCode + "]: " + message);
        this.dsaCode = dsaCode;
    }
}
