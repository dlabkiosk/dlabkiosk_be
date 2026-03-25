package com.moduletest.deasungkioskbackend.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "지점 등록 요청")
public record StoreCreateRequest(
    @Schema(description = "지점명", example = "대성학원 강남점")
    @NotBlank(message = "지점명은 필수 입력 항목입니다.") String storeName,
    @Schema(description = "지점 코드 (중복 불가)", example = "DS-001")
    @NotBlank(message = "지점 코드는 필수 입력 항목입니다.") String storeCode,
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    String address,
    @Schema(description = "전화번호", example = "02-1234-5678")
    String phone,
    @Schema(description = "키오스크 PIN (미입력 시 0000)", example = "1234")
    @Size(max = 10, message = "PIN은 10자를 초과할 수 없습니다.")
    String kioskPin,
    @Schema(description = "DSA 학원 고유코드", example = "ACAD001")
    String dsaAcadCd,
    @Schema(description = "DSA 키오스크 ID", example = "kiosk_client_01")
    String dsaClientId,
    @Schema(description = "DSA 시크릿 키", example = "secret_key_01")
    String dsaSecretId
) { }
