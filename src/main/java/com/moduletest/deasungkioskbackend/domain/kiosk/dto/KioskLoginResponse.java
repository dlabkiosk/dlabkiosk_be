package com.moduletest.deasungkioskbackend.domain.kiosk.dto;

import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "키오스크 로그인 응답")
public record KioskLoginResponse(
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "지점 코드", example = "DS-001")
    String storeCode
) {

    public static KioskLoginResponse fromEntity(Store store) {
        return new KioskLoginResponse(
            store.getId(),
            store.getStoreName(),
            store.getStoreCode()
        );
    }
}
