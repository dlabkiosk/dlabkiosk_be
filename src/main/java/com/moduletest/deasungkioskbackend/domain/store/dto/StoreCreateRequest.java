package com.moduletest.deasungkioskbackend.domain.store.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreCreateRequest(
    @NotBlank(message = "지점명은 필수 입력 항목입니다.") String storeName,
    @NotBlank(message = "지점 코드는 필수 입력 항목입니다.") String storeCode,
    String address,
    String phone
) {}
