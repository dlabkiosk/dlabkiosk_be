package com.moduletest.deasungkioskbackend.domain.kiosk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "키오스크 로그인 요청")
public record KioskLoginRequest(
    @Schema(description = "지점 코드", example = "DS-001")
    @NotBlank(message = "지점 코드는 필수 입력 항목입니다.")
    String storeCode,

    @Schema(description = "키오스크 PIN", example = "0000")
    @NotBlank(message = "PIN은 필수 입력 항목입니다.")
    String kioskPin
) { }
