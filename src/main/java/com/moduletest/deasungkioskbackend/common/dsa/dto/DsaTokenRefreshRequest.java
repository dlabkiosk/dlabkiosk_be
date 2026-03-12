package com.moduletest.deasungkioskbackend.common.dsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DsaTokenRefreshRequest(
    @JsonProperty("client_id") String clientId,
    String refreshToken
) {

}
