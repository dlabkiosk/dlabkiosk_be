package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
    @NotBlank(message = "QR UUID는 필수 입력 항목입니다.")
    String qrUuid
) {
}
