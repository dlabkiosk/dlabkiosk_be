package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "휴대폰 미소지 신청 요청. 카드/QR/좌석번호/폰뒷자리 중 하나로 학생을 식별한다.")
public record PhoneSubmissionRequest(
    @Schema(description = "학생 식별값 (카드/QR/좌석번호/폰뒷자리)", example = "A1B2C3D4")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier,

    @Schema(description = "신청 유형 (DAILY: 당일, PERIOD: 기간 설정, NO_PHONE: 휴대폰 미보유)",
        example = "DAILY")
    @NotNull(message = "신청 유형은 필수입니다")
    PhoneSubmissionType submissionType,

    @Schema(description = "시작일 (PERIOD 시 필수, DAILY/NO_PHONE 시 무시)", example = "2026-03-16")
    LocalDate startDate,

    @Schema(description = "종료일 (PERIOD 시 필수, NO_PHONE 시 null=무기한)", example = "2026-03-20")
    LocalDate endDate
) {

}
