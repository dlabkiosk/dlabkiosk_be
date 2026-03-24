package com.moduletest.deasungkioskbackend.domain.mealmenu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "일별 식단 요청")
public record MealMenuDayRequest(
    @Schema(description = "날짜", example = "2026-03-23")
    @NotNull(message = "날짜는 필수입니다")
    LocalDate menuDate,

    @Schema(description = "점심 메뉴", example = "김치찌개, 불고기, 샐러드")
    String lunch,

    @Schema(description = "저녁 메뉴", example = "된장찌개, 제육볶음, 나물")
    String dinner,

    @Schema(description = "휴무 여부", example = "false")
    boolean closed
) {
}
