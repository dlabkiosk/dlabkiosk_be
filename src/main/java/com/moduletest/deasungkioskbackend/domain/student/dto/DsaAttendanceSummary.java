package com.moduletest.deasungkioskbackend.domain.student.dto;

import lombok.Builder;

@Builder
public record DsaAttendanceSummary(
    int absenceCount,
    int earlyLeaveCount,
    int outingCount
) {
}
