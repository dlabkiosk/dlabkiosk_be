package com.moduletest.deasungkioskbackend.domain.mealmenu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "주별 식단 등록/수정 요청")
public record MealMenuWeekRequest(
    @Schema(description = "주 시작일 (월요일)", example = "2026-03-23")
    @NotNull(message = "주 시작일은 필수입니다")
    LocalDate weekStartDate,

    @Schema(description = "일별 식단 목록 (최대 7일)")
    @NotEmpty(message = "식단 목록은 비어있을 수 없습니다")
    @Valid
    List<MealMenuDayRequest> days
) {
}
