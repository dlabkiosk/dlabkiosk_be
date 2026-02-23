package com.moduletest.deasungkioskbackend.domain.store.dto;

import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "지점 응답")
public record StoreResponse(
    @Schema(description = "지점 ID", example = "1")
    Long id,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "지점 코드", example = "DS-001")
    String storeCode,
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    String address,
    @Schema(description = "전화번호", example = "02-1234-5678")
    String phone,
    @Schema(description = "활성화 여부", example = "true")
    boolean active,
    @Schema(description = "생성일시")
    LocalDateTime createdAt,
    @Schema(description = "수정일시")
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
