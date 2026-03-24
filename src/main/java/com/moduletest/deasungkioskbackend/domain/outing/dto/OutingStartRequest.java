package com.moduletest.deasungkioskbackend.domain.outing.dto;

import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "외출 시작 요청 — identifier 또는 학번 중 하나 필수")
public record OutingStartRequest(
    @Schema(description = "학생 식별값 (RFID UID 또는 QR UUID)",
        example = "A1B2C3D4")
    String identifier,

    @Schema(description = "입력 방식\n- RFID: 카드/QR\n- SEAT_LABEL: 좌석번호\n- PHONE_LAST4: 전화번호 뒷자리")
    InputMethod inputMethod,

    @Schema(description = "학번",
        example = "2024-001")
    String studentNumber
) { }
