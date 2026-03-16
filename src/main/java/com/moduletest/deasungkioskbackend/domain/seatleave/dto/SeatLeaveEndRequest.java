package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "좌석이탈 복귀 요청. 전화번호 뒷자리로 학생을 식별한다.")
public record SeatLeaveEndRequest(
    @Schema(description = "전화번호 뒷자리 4자리", example = "1234")
    @NotBlank(message = "전화번호 뒷자리는 필수입니다.")
    String phoneLast4
) {

}
