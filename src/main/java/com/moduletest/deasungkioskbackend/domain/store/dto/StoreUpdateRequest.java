package com.moduletest.deasungkioskbackend.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreUpdateRequest(
    @NotBlank(message = "지점명은 필수 입력 항목입니다") String storeName,
    String address,
    String phone,
    @NotNull(message = "활성화 여부는 필수 입력 항목입니다") Boolean active
) {}
