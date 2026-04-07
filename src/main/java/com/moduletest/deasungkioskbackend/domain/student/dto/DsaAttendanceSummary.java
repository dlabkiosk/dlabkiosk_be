package com.moduletest.deasungkioskbackend.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DsaAttendanceSummary(
    @Schema(description = "잔여외출 횟수")
    int remainingOutCount,

    @Schema(description = "잔여휴가 횟수")
    int remainingLeaveCount,

    @Schema(description = "무단지각 횟수")
    int lateCount,

    @Schema(description = "무단조퇴 횟수")
    int earlyLeaveCount,

    @Schema(description = "상점")
    int plusPoint,

    @Schema(description = "벌점")
    int minusPoint
) {
}
