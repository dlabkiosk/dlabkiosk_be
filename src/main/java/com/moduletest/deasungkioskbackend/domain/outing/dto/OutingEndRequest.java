package com.moduletest.deasungkioskbackend.domain.outing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "외출 복귀 요청 — QR UUID 또는 RFID UID 중 하나 필수")
public record OutingEndRequest(
    @Schema(description = "학생 QR UUID. "
        + "QR 사용 시 이 값을 전송하고 rfidUid는 null이어야 한다.",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,

    @Schema(description = "RFID 카드 UID. "
        + "RFID 사용 시 이 값을 전송하고 qrUuid는 null이어야 한다.",
        example = "A1B2C3D4")
    String rfidUid
) { }
