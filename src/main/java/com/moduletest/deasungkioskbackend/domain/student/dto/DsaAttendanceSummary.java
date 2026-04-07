package com.moduletest.deasungkioskbackend.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DsaAttendanceSummary(
    @Schema(description = "결석 횟수")
    int absenceCount,

    @Schema(description = "조퇴 횟수")
    int earlyLeaveCount,

    @Schema(description = "외출 횟수")
    int outingCount,

    @Schema(description = "무단지각 횟수")
    int lateCount
) {
}
