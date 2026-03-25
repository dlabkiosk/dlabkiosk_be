package com.moduletest.deasungkioskbackend.domain.seat.dto;

import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "좌석 입실 요청. QR UUID 또는 RFID UID를 identifier에 전송. 배정된 좌석으로 자동 입실.")
public record SeatCheckInRequest(
    @Schema(description = "학생 식별값 (QR UUID 또는 RFID UID)",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "학생 식별값은 필수입니다.")
    String identifier,

    @Schema(description = "입력 방식\n- RFID: 카드/QR\n- SEAT_LABEL: 좌석번호\n- PHONE_LAST4: 전화번호 뒷자리")
    InputMethod inputMethod
) { }
