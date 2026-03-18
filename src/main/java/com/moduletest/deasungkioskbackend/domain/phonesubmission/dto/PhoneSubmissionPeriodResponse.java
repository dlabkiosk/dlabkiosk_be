package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "휴대폰 미소지 신청 기간 응답")
public record PhoneSubmissionPeriodResponse(
    @Schema(description = "시작일", example = "2026-03-18")
    LocalDate startDate,
    @Schema(description = "종료일 (무기한이면 null)", example = "2026-03-20")
    LocalDate endDate,
    @Schema(description = "신청 유형", example = "DAILY")
    PhoneSubmissionType submissionType
) {

    public static PhoneSubmissionPeriodResponse fromEntity(PhoneSubmission ps) {
        return new PhoneSubmissionPeriodResponse(
            ps.getStartDate(),
            ps.getEndDate(),
            ps.getSubmissionType()
        );
    }
}
