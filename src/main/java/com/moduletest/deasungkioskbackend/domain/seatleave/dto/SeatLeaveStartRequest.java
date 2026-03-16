package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석이탈 신청 요청. 카드/QR/좌석번호/폰뒷자리 중 하나로 학생을 식별한다.")
public record SeatLeaveStartRequest(
    @Schema(description = "학생 식별값 (카드/QR/좌석번호/폰뒷자리)", example = "A1B2C3D4")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier,

    @Schema(description = "이탈 사유 ID", example = "1")
    @NotNull(message = "이탈 사유는 필수입니다.")
    Long reasonId
) {

}
