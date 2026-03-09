package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "좌석이탈 복귀 요청. 좌석번호를 입력하면 해당 좌석에서 이탈 중인 학생을 자동 식별한다.")
public record SeatLeaveEndRequest(
    @Schema(description = "좌석번호 (좌석 라벨)", example = "A-1")
    @NotBlank(message = "좌석번호는 필수입니다.")
    String seatLabel
) {

}
