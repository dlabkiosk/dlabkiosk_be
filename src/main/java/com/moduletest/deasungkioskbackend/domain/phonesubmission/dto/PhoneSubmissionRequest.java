package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "휴대폰 미소지 신청 요청. 좌석번호를 입력하면 해당 좌석에 배정된 학생을 자동 식별한다.")
public record PhoneSubmissionRequest(
    @Schema(description = "좌석번호 (좌석 라벨)", example = "A-1")
    @NotBlank(message = "좌석번호는 필수입니다")
    String seatLabel
) {

}
