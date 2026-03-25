package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "휴대폰 미소지 수정 요청")
public record PhoneSubmissionUpdateRequest(
    @Schema(description = "신청 유형 (DAILY: 당일, PERIOD: 기간 설정, NO_PHONE: 휴대폰 미보유)",
        example = "DAILY")
    @NotNull(message = "신청 유형은 필수입니다")
    PhoneSubmissionType submissionType,

    @Schema(description = "시작일 (PERIOD 시 필수)", example = "2026-03-16")
    LocalDate startDate,

    @Schema(description = "종료일 (PERIOD 시 필수, NO_PHONE 시 null=무기한)", example = "2026-03-20")
    LocalDate endDate,

    @Schema(description = "메모", example = "학부모 요청으로 기간 변경")
    String memo
) {

}
