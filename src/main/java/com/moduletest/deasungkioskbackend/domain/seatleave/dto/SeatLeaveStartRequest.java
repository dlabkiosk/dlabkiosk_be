package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석이탈 신청 요청. 카드/QR/좌석번호/폰뒷자리 중 하나로 학생을 식별한다.")
public record SeatLeaveStartRequest(
    @Schema(description = "학생 식별값 (RFID UID 또는 QR UUID)", example = "A1B2C3D4")
    String identifier,

    @Schema(description = "학번", example = "20250101")
    String studentNumber,

    @Schema(description = "좌석번호", example = "A-1")
    String seatLabel,

    @Schema(description = "전화번호 뒷자리 4자리", example = "1234")
    String phoneLast4,

    @Schema(description = "이탈 사유 ID", example = "1")
    @NotNull(message = "이탈 사유는 필수입니다.")
    Long reasonId
) {

}
