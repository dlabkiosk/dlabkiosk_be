package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "하원(체크아웃) 요청 — QR UUID, RFID UID, 학번, 전화번호 중 하나 필수")
public record CheckOutRequest(
    @Schema(description = "학생 QR 코드의 UUID (키오스크 카메라로 스캔한 값). "
        + "QR 사용 시 이 값만 전송하고 나머지는 null.",
        example = "550e8400-e29b-41d4-a716-446655440000")
    String qrUuid,

    @Schema(description = "RFID 카드 UID (리더기에서 읽은 값). "
        + "RFID 사용 시 이 값만 전송하고 나머지는 null.",
        example = "A1B2C3D4")
    String rfidUid,

    @Schema(description = "학번. 학번으로 인식 시 이 값만 전송하고 나머지는 null.",
        example = "2024-001")
    String studentNumber,

    @Schema(description = "전화번호. 전화번호로 인식 시 이 값만 전송하고 나머지는 null.",
        example = "010-1234-5678")
    String phone
) { }
