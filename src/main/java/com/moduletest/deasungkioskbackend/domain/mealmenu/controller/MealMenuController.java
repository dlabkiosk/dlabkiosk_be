package com.moduletest.deasungkioskbackend.domain.mealmenu.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuWeekResponse;
import com.moduletest.deasungkioskbackend.domain.mealmenu.service.MealMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 식단표", description = "키오스크 식단표 조회 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/meal-menus")
@RequiredArgsConstructor
public class MealMenuController {

    private final MealMenuService mealMenuService;

    @Operation(summary = "주별 식단 조회",
        description = "weekStartDate(월요일) 기준 해당 주 식단을 조회한다. "
            + "미입력 시 이번 주 월요일 기준.")
    @GetMapping
    public CommonResponse<MealMenuWeekResponse> findWeeklyMenu(
        @RequestParam(required = false) LocalDate weekStartDate) {
        Long storeId = getStoreIdFromToken();
        if (weekStartDate == null) {
            LocalDate today = LocalDate.now();
            weekStartDate = today.minusDays(
                today.getDayOfWeek().getValue() - 1);
        }
        MealMenuWeekResponse response = mealMenuService
            .findWeeklyMenu(storeId, weekStartDate);
        return CommonResponse.success(response);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
