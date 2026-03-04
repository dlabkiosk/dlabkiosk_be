package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "좌석이탈 복귀 요청. QR UUID 또는 RFID UID를 identifier에 전송.")
public record SeatLeaveEndRequest(
    @Schema(description = "학생 식별값 (QR UUID 또는 RFID UID)",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "학생 식별값은 필수입니다.")
    String identifier
) {

}
