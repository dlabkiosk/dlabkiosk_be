package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "좌석이탈 신청 요청. 전화번호 뒷자리로 학생을 식별한다.")
public record SeatLeaveStartRequest(
    @Schema(description = "전화번호 뒷자리 4자리", example = "1234")
    @NotBlank(message = "전화번호 뒷자리는 필수입니다.")
    String phoneLast4,

    @Schema(description = "이탈 사유 ID", example = "1")
    @NotNull(message = "이탈 사유는 필수입니다.")
    Long reasonId
) {

}
