package com.moduletest.deasungkioskbackend.common.dsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DsaTokenRequest(
    @JsonProperty("acad_cd") String acadCd,
    @JsonProperty("client_id") String clientId,
    @JsonProperty("secret_id") String secretId,
    String service
) {

    public static DsaTokenRequest of(String acadCd, String clientId, String secretId) {
        return new DsaTokenRequest(acadCd, clientId, secretId, "kiosk");
    }
}
