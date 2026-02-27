package com.moduletest.deasungkioskbackend.domain.advertisement.dto;

public record AdvertisementUpdateRequest(
    String imageUrl,
    String mediaType,
    Integer displayOrder,
    Integer displaySeconds,
    Boolean active
) {
}
