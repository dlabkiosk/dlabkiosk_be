package com.moduletest.deasungkioskbackend.domain.meal.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.meal.dto.MealCheckRequest;
import com.moduletest.deasungkioskbackend.domain.meal.dto.MealStatusResponse;
import com.moduletest.deasungkioskbackend.domain.meal.service.MealAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 식사 관리")
@RestController
@RequestMapping("/api/v1/admin/meals")
@RequiredArgsConstructor
public final class MealAdminController {

    private final MealAdminService mealAdminService;

    @Operation(summary = "식사 신청 및 체크 현황 조회",
        description = "기간 내 학생별 급식 신청(DSA) 및 체크(키오스크 태깅) 현황을 조회합니다.\n\n"
            + "- 점심/저녁 신청: DSA 3.30 getMealApplyStdInfo 조회\n"
            + "- 점심/저녁 체크: 키오스크 급식 태깅 기록 (meal_tags)\n"
            + "- 체크 시간: 태깅 시각 반환, 미체크 시 null\n"
            + "- startDate, endDate 미입력 시 오늘 날짜 기본값")
    @GetMapping
    public CommonResponse<List<MealStatusResponse>> findMealStatusList(
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String studentName,
        @RequestParam(required = false) String studentNumber) {

        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<MealStatusResponse> result = mealAdminService.findMealStatusList(
            resolvedStoreId, startDate, endDate, studentName, studentNumber);

        return CommonResponse.success(result);
    }

    @Operation(summary = "관리자 수동 급식 체크",
        description = "급식 신청은 했으나 키오스크에서 태깅하지 않은 학생을 관리자가 수동으로 체크합니다. "
            + "DSA 신청 여부를 검증하며, 미신청 학생은 거부됩니다(MEAL_NOT_APPLIED).\n\n"
            + "MANAGER는 자기 지점 학생만, ADMIN은 전 지점 가능.")
    @PostMapping("/check")
    public CommonResponse<MealStatusResponse> markMealAsTagged(
        @Valid @RequestBody MealCheckRequest request) {
        MealStatusResponse response = mealAdminService.markMealAsTagged(
            request.studentId(), request.date(), request.mealType());
        return CommonResponse.success(response);
    }

    @Operation(summary = "관리자 급식 체크 해제",
        description = "관리자가 수동으로 체크한(또는 키오스크 태깅된) 급식 기록을 해제합니다. "
            + "MANAGER는 자기 지점 학생만, ADMIN은 전 지점 가능.")
    @DeleteMapping("/check")
    public CommonResponse<MealStatusResponse> unmarkMealAsTagged(
        @Valid @RequestBody MealCheckRequest request) {
        MealStatusResponse response = mealAdminService.unmarkMealAsTagged(
            request.studentId(), request.date(), request.mealType());
        return CommonResponse.success(response);
    }
}
