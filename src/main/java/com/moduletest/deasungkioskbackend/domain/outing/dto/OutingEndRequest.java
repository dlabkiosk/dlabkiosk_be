package com.moduletest.deasungkioskbackend.domain.outing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "외출 복귀 요청 — identifier, 학번, 전화번호 중 하나 필수")
public record OutingEndRequest(
    @Schema(description = "학생 식별값 (QR UUID 또는 RFID UID)",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String identifier,

    @Schema(description = "학번. 학번으로 인식 시 이 값만 전송.",
        example = "2024-001")
    String studentNumber,

    @Schema(description = "전화번호. 전화번호로 인식 시 이 값만 전송.",
        example = "010-1234-5678")
    String phone
) { }
