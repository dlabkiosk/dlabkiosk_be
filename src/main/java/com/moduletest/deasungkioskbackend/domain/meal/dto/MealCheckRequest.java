package com.moduletest.deasungkioskbackend.domain.meal.dto;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "관리자 급식 체크/해제 요청")
public record MealCheckRequest(
    @Schema(description = "학생 ID", example = "1")
    @NotNull(message = "studentId는 필수입니다")
    Long studentId,

    @Schema(description = "식사 날짜 (yyyy-MM-dd)", example = "2026-04-08")
    @NotNull(message = "date는 필수입니다")
    LocalDate date,

    @Schema(description = "식사 유형", example = "LUNCH")
    @NotNull(message = "mealType은 필수입니다")
    MealType mealType
) {
}
