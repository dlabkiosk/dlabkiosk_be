package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석 변경 신청 요청. 카드/QR/좌석번호/폰뒷자리 중 하나로 학생을 식별한다.")
public record SeatChangeRequestCreateRequest(
    @Schema(description = "학생 식별값 (카드/QR/좌석번호/폰뒷자리)", example = "A1B2C3D4")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier,

    @NotNull(message = "1순위 희망 좌석 ID는 필수입니다")
    @Schema(description = "1순위 희망 좌석 ID", example = "5")
    Long desiredSeatId1,

    @Schema(description = "2순위 희망 좌석 ID (선택)", example = "8")
    Long desiredSeatId2,

    @Schema(description = "3순위 희망 좌석 ID (선택)", example = "12")
    Long desiredSeatId3
) {

}
