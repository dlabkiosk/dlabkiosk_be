package com.moduletest.deasungkioskbackend.domain.studytime.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.studytime.dto.StudyTimeStudentResponse;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 순공시간 관리")
@RestController
@RequestMapping("/api/v1/admin/study-times")
@RequiredArgsConstructor
public final class StudyTimeAdminController {

    private final StudyTimeAdminService studyTimeAdminService;

    @Operation(summary = "학생별 순공시간 조회",
        description = "기간 내 학생별 일별 순공시간을 조회합니다. (DSA 3.27 getStudyTimeList)\n\n"
            + "- 금일 이후 데이터가 없는 날짜는 0으로 반환됩니다.\n"
            + "- startDate, endDate 미입력 시 전일 기준 최근 7일 조회")
    @GetMapping
    public CommonResponse<List<StudyTimeStudentResponse>> findStudyTimeList(
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String studentName,
        @RequestParam(required = false) String studentNumber) {

        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);

        if (endDate == null) {
            endDate = LocalDate.now().minusDays(1);
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        List<StudyTimeStudentResponse> result = studyTimeAdminService.findStudyTimeList(
            resolvedStoreId, startDate, endDate, studentName, studentNumber);

        return CommonResponse.success(result);
    }
}
