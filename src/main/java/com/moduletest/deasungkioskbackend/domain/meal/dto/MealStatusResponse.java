package com.moduletest.deasungkioskbackend.domain.meal.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record MealStatusResponse(
    Long studentId,
    String studentName,
    String studentNumber,
    String seatLabel,
    LocalDate date,
    boolean lunchApplied,
    boolean lunchChecked,
    LocalTime lunchCheckedTime,
    boolean dinnerApplied,
    boolean dinnerChecked,
    LocalTime dinnerCheckedTime
) {
}
