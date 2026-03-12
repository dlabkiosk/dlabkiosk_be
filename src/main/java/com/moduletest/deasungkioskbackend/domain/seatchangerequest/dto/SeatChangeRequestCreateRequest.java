package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석 변경 신청 요청")
public record SeatChangeRequestCreateRequest(

    @Schema(description = "학생 식별자 (RFID UID 또는 QR UUID)", example = "A1B2C3D4")
    String identifier,

    @Schema(description = "학번", example = "20250101")
    String studentNumber,

    @NotNull(message = "1순위 희망 좌석 ID는 필수입니다")
    @Schema(description = "1순위 희망 좌석 ID", example = "5")
    Long desiredSeatId1,

    @Schema(description = "2순위 희망 좌석 ID (선택)", example = "8")
    Long desiredSeatId2,

    @Schema(description = "3순위 희망 좌석 ID (선택)", example = "12")
    Long desiredSeatId3
) {

}
