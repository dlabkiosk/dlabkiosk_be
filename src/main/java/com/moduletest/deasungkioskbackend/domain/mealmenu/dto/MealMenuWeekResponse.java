package com.moduletest.deasungkioskbackend.domain.mealmenu.dto;

import com.moduletest.deasungkioskbackend.domain.mealmenu.entity.MealMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "주별 식단 응답")
public record MealMenuWeekResponse(
    @Schema(description = "주 시작일 (월요일)", example = "2026-03-23")
    LocalDate weekStartDate,

    @Schema(description = "주 종료일 (일요일)", example = "2026-03-29")
    LocalDate weekEndDate,

    @Schema(description = "일별 식단 목록")
    List<MealMenuDayResponse> days
) {

    @Schema(description = "일별 식단 응답")
    public record MealMenuDayResponse(
        @Schema(description = "식단 ID", example = "1")
        Long id,

        @Schema(description = "날짜", example = "2026-03-23")
        LocalDate menuDate,

        @Schema(description = "요일", example = "MONDAY")
        String dayOfWeek,

        @Schema(description = "점심 메뉴", example = "김치찌개, 불고기, 샐러드")
        String lunch,

        @Schema(description = "저녁 메뉴", example = "된장찌개, 제육볶음, 나물")
        String dinner,

        @Schema(description = "휴무 여부", example = "false")
        boolean closed
    ) {

        public static MealMenuDayResponse fromEntity(MealMenu mealMenu) {
            return new MealMenuDayResponse(
                mealMenu.getId(),
                mealMenu.getMenuDate(),
                mealMenu.getMenuDate().getDayOfWeek().name(),
                mealMenu.getLunch(),
                mealMenu.getDinner(),
                mealMenu.isClosed()
            );
        }
    }

    public static MealMenuWeekResponse of(LocalDate weekStartDate,
                                           List<MealMenu> menus) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<MealMenuDayResponse> days = menus.stream()
            .map(MealMenuDayResponse::fromEntity)
            .toList();
        return new MealMenuWeekResponse(weekStartDate, weekEndDate, days);
    }
}
