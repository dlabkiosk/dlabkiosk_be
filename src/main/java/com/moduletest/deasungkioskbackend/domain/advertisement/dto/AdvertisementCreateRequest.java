package com.moduletest.deasungkioskbackend.domain.advertisement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdvertisementCreateRequest(
    @NotNull Long storeId,
    @NotBlank String imageUrl,
    @NotBlank String mediaType,
    int displayOrder,
    int displaySeconds
) {
}
