package com.moduletest.deasungkioskbackend.domain.phonesubmission.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.dto.PageResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.service.PhoneSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 휴대폰 미소지", description = "휴대폰 미소지 현황 조회 및 엑셀 다운로드 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/phone-submissions")
@RequiredArgsConstructor
public class PhoneSubmissionAdminController {

    private final PhoneSubmissionService phoneSubmissionService;

    @Operation(summary = "휴대폰 미소지 현황 조회 (페이지네이션)",
        description = "기간별 미소지 현황을 조회한다. 날짜 미지정 시 당일 기준. "
            + "MANAGER: 자기 지점만 (storeId 파라미터 무시). "
            + "ADMIN: storeId 없으면 전체, 있으면 해당 지점만. "
            + "studentName/studentNumber 는 부분일치 검색. "
            + "페이지네이션: ?page=0&size=20&sort=submittedAt,desc. "
            + "신청 유형 — DAILY: 당일, PERIOD: 기간 설정, NO_PHONE: 휴대폰 미보유(무기한).")
    @GetMapping
    public CommonResponse<PageResponse<PhoneSubmissionResponse>> findAllPhoneSubmissions(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) String studentName,
        @RequestParam(required = false) String studentNumber,
        @ParameterObject @PageableDefault(size = 20, sort = "submittedAt",
            direction = org.springframework.data.domain.Sort.Direction.DESC)
        Pageable pageable) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Page<PhoneSubmissionResponse> page = phoneSubmissionService
            .findAllByPeriod(resolvedStoreId, start, end,
                studentName, studentNumber, pageable);
        return CommonResponse.success(PageResponse.from(page));
    }

    @Operation(summary = "휴대폰 미소지 수정",
        description = "신청 유형, 기간, 메모를 수정한다. "
            + "DAILY: startDate/endDate 무시 (기존 신청일 유지). "
            + "PERIOD: startDate/endDate 필수. "
            + "NO_PHONE: endDate 무시 (무기한).")
    @PutMapping("/{id}")
    public CommonResponse<PhoneSubmissionResponse> updatePhoneSubmission(
        @PathVariable Long id,
        @Valid @RequestBody PhoneSubmissionUpdateRequest request) {
        return CommonResponse.success(
            phoneSubmissionService.updatePhoneSubmission(id, request));
    }

    @Operation(summary = "휴대폰 미소지 삭제",
        description = "신청 내역을 삭제한다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoneSubmission(@PathVariable Long id) {
        phoneSubmissionService.deletePhoneSubmission(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "휴대폰 미소지 엑셀 다운로드",
        description = "MANAGER: 자기 지점 기간별 내역. ADMIN: 전체 지점 기간별 내역. "
            + "엑셀(.xlsx) 파일로 다운로드한다.")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPhoneSubmissions(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long storeId = SecurityUtil.resolveStoreId(null);
        byte[] excelBytes = phoneSubmissionService.exportToExcel(storeId, startDate, endDate);

        String filename = "phone-submissions_"
            + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "_"
            + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
