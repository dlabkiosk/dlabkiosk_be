package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석이탈 신청 요청. QR UUID 또는 RFID UID를 identifier에 전송.")
public record SeatLeaveStartRequest(
    @Schema(description = "학생 식별값 (QR UUID 또는 RFID UID)",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "학생 식별값은 필수입니다.")
    String identifier,

    @Schema(description = "이탈 사유 ID", example = "1")
    @NotNull(message = "이탈 사유는 필수입니다.")
    Long reasonId
) {

}
