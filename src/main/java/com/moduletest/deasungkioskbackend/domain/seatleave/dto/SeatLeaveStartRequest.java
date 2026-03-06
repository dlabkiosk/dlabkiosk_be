package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석이탈 신청 요청. 좌석번호를 입력하면 해당 좌석에 배정된 학생을 자동 식별한다.")
public record SeatLeaveStartRequest(
    @Schema(description = "좌석번호 (좌석 라벨)", example = "A-1")
    @NotBlank(message = "좌석번호는 필수입니다.")
    String seatLabel,

    @Schema(description = "이탈 사유 ID", example = "1")
    @NotNull(message = "이탈 사유는 필수입니다.")
    Long reasonId
) {

}
