package com.moduletest.deasungkioskbackend.domain.student.dto;

import lombok.Builder;

@Builder
public record DsaPointRecord(
    String pointDate,
    String reason,
    int point
) {
}
