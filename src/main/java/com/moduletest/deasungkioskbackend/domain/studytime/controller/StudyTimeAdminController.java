package com.moduletest.deasungkioskbackend.domain.studytime.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.studytime.dto.StudyTimeStudentResponse;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "학생별 순공시간 엑셀 다운로드",
        description = "조회 조건 그대로 .xlsx 파일로 내려준다. "
            + "컬럼: 번호/학생명/학번/좌석/기간 내 일별 순공시간(MM-dd)/합계. "
            + "MANAGER: 자기 지점만. ADMIN: storeId 필수.")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStudyTimes(
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

        byte[] excelBytes = studyTimeAdminService.exportToExcel(
            resolvedStoreId, startDate, endDate, studentName, studentNumber);

        String filename = "study-times_"
            + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "_"
            + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
