package com.moduletest.deasungkioskbackend.domain.student.dto;

import lombok.Builder;

@Builder
public record DsaMealApplication(
    String day,
    String mealType
) {
}
