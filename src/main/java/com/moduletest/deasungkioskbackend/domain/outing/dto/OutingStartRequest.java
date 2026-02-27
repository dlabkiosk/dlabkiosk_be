package com.moduletest.deasungkioskbackend.domain.outing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "외출 시작 요청 — QR UUID, RFID UID, 학번, 전화번호 중 하나 필수")
public record OutingStartRequest(
    @Schema(description = "학생 QR UUID. "
        + "QR 사용 시 이 값만 전송하고 나머지는 null.",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,

    @Schema(description = "RFID 카드 UID. "
        + "RFID 사용 시 이 값만 전송하고 나머지는 null.",
        example = "A1B2C3D4")
    String rfidUid,

    @Schema(description = "학번. 학번으로 인식 시 이 값만 전송하고 나머지는 null.",
        example = "2024-001")
    String studentNumber,

    @Schema(description = "전화번호. 전화번호로 인식 시 이 값만 전송하고 나머지는 null.",
        example = "010-1234-5678")
    String phone,

    @Schema(description = "외출 사유 (선택)", example = "점심 식사")
    @Size(max = 100, message = "외출 사유는 100자 이내여야 합니다")
    String reason
) { }
