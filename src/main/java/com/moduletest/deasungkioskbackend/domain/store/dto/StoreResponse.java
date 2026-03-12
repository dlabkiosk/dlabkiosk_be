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
    @Schema(description = "DSA 연동 여부", example = "true")
    boolean dsaConnected,
    @Schema(description = "생성일시")
    LocalDateTime createdAt,
    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

    public static StoreResponse fromEntity(Store store) {
        boolean dsaConnected = store.getDsaAcadCd() != null
            && store.getDsaClientId() != null
            && store.getDsaSecretId() != null;
        return new StoreResponse(
            store.getId(),
            store.getStoreName(),
            store.getStoreCode(),
            store.getAddress(),
            store.getPhone(),
            store.isActive(),
            dsaConnected,
            store.getCreatedAt(),
            store.getUpdatedAt()
        );
    }
}
