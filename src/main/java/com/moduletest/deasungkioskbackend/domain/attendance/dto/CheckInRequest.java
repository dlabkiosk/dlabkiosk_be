package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "등원(체크인) 요청 — identifier 또는 학번 중 하나 필수")
public record CheckInRequest(
    @Schema(description = "학생 식별값 (RFID UID 또는 QR UUID)",
        example = "A1B2C3D4")
    String identifier,

    @Schema(description = "학번",
        example = "2024-001")
    String studentNumber
) { }
