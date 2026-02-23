package com.moduletest.deasungkioskbackend.domain.store.dto;

import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.time.LocalDateTime;

public record StoreResponse(
    Long id,
    String storeName,
    String storeCode,
    String address,
    String phone,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static StoreResponse fromEntity(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getStoreName(),
            store.getStoreCode(),
            store.getAddress(),
            store.getPhone(),
            store.isActive(),
            store.getCreatedAt(),
            store.getUpdatedAt()
        );
    }
}
