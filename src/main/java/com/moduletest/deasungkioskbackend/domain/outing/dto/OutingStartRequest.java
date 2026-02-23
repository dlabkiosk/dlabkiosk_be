package com.moduletest.deasungkioskbackend.domain.outing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "외출 시작 요청 — QR UUID 또는 RFID UID 중 하나 필수")
public record OutingStartRequest(
    @Schema(description = "학생 QR UUID. "
        + "QR 사용 시 이 값을 전송하고 rfidUid는 null이어야 한다.",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,

    @Schema(description = "RFID 카드 UID. "
        + "RFID 사용 시 이 값을 전송하고 qrUuid는 null이어야 한다.",
        example = "A1B2C3D4")
    String rfidUid,

    @Schema(description = "외출 사유 (선택)", example = "점심 식사")
    @Size(max = 100, message = "외출 사유는 100자 이내여야 합니다")
    String reason
) { }
