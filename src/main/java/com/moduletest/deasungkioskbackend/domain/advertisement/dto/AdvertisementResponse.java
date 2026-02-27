package com.moduletest.deasungkioskbackend.domain.advertisement.dto;

import com.moduletest.deasungkioskbackend.domain.advertisement.entity.Advertisement;

public record AdvertisementResponse(
    Long id,
    Long storeId,
    String storeName,
    String imageUrl,
    String mediaType,
    int displayOrder,
    int displaySeconds,
    boolean active
) {
    public static AdvertisementResponse fromEntity(Advertisement advertisement) {
        return new AdvertisementResponse(
            advertisement.getId(),
            advertisement.getStore().getId(),
            advertisement.getStore().getStoreName(),
            advertisement.getImageUrl(),
            advertisement.getMediaType(),
            advertisement.getDisplayOrder(),
            advertisement.getDisplaySeconds(),
            advertisement.isActive()
        );
    }
}
