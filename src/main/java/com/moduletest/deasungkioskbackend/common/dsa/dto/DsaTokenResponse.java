package com.moduletest.deasungkioskbackend.common.dsa.dto;

public record DsaTokenResponse(
    int code,
    String message,
    String token,
    String refreshToken
) {

    public boolean isSuccess() {
        return code == 0;
    }
}
