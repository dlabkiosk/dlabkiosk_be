package com.moduletest.deasungkioskbackend.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "지점 수정 요청")
public record StoreUpdateRequest(
    @Schema(description = "지점명", example = "대성학원 강남점")
    @NotBlank(message = "지점명은 필수 입력 항목입니다") String storeName,
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    String address,
    @Schema(description = "전화번호", example = "02-1234-5678")
    String phone,
    @Schema(description = "활성화 여부", example = "true")
    @NotNull(message = "활성화 여부는 필수 입력 항목입니다") Boolean active,
    @Schema(description = "키오스크 PIN", example = "1234")
    @Size(max = 10, message = "PIN은 10자를 초과할 수 없습니다.")
    String kioskPin
) { }
