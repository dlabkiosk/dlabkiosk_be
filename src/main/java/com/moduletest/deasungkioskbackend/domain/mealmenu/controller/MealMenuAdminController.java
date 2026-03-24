package com.moduletest.deasungkioskbackend.domain.mealmenu.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuWeekRequest;
import com.moduletest.deasungkioskbackend.domain.mealmenu.dto.MealMenuWeekResponse;
import com.moduletest.deasungkioskbackend.domain.mealmenu.service.MealMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 식단표 관리", description = "주별 식단표 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/meal-menus")
@RequiredArgsConstructor
public class MealMenuAdminController {

    private final MealMenuService mealMenuService;

    @Operation(summary = "주별 식단 조회",
        description = "weekStartDate(월요일) 기준 해당 주 식단을 조회한다.")
    @GetMapping
    public CommonResponse<MealMenuWeekResponse> findWeeklyMenu(
        @RequestParam(required = false) Long storeId,
        @RequestParam LocalDate weekStartDate) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        MealMenuWeekResponse response = mealMenuService
            .findWeeklyMenu(resolvedStoreId, weekStartDate);
        return CommonResponse.success(response);
    }

    @Operation(summary = "주별 식단 등록/수정",
        description = "해당 주 식단을 등록하거나 수정한다. 이미 존재하는 날짜는 덮어쓴다.")
    @PostMapping
    public CommonResponse<MealMenuWeekResponse> saveWeeklyMenu(
        @RequestParam(required = false) Long storeId,
        @Valid @RequestBody MealMenuWeekRequest request) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        MealMenuWeekResponse response = mealMenuService
            .saveWeeklyMenu(resolvedStoreId, request);
        return CommonResponse.success(response);
    }

    @Operation(summary = "주별 식단 삭제",
        description = "weekStartDate(월요일) 기준 해당 주 식단을 전체 삭제한다.")
    @DeleteMapping
    public CommonResponse<Void> deleteWeeklyMenu(
        @RequestParam(required = false) Long storeId,
        @RequestParam LocalDate weekStartDate) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        mealMenuService.deleteWeeklyMenu(resolvedStoreId, weekStartDate);
        return CommonResponse.success(null);
    }
}
