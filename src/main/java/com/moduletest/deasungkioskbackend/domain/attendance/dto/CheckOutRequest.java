package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "하원(체크아웃) 요청 — QR UUID 또는 RFID UID 중 하나 필수")
public record CheckOutRequest(
    @Schema(description = "학생 QR 코드의 UUID (키오스크 카메라로 스캔한 값). "
        + "QR 사용 시 이 값을 전송하고 rfidUid는 null이어야 한다.",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,

    @Schema(description = "RFID 카드 UID (리더기에서 읽은 값). "
        + "RFID 사용 시 이 값을 전송하고 qrUuid는 null이어야 한다.",
        example = "A1B2C3D4")
    String rfidUid
) { }
