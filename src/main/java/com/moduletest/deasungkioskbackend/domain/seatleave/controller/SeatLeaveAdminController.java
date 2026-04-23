package com.moduletest.deasungkioskbackend.domain.seatleave.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.dto.PageResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "[관리자] 좌석이탈 현황", description = "좌석이탈 현황 조회 및 엑셀 다운로드 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seat-leaves")
@RequiredArgsConstructor
public class SeatLeaveAdminController {

    private final SeatLeaveService seatLeaveService;

    @Operation(summary = "좌석이탈 현황 조회 (페이지네이션)",
        description = "기간별 좌석이탈 현황을 조회한다. 날짜 미지정 시 당일 기준. "
            + "MANAGER: 자기 지점만 (storeId 파라미터 무시). "
            + "ADMIN: storeId 없으면 전체, 있으면 해당 지점만. "
            + "studentName/studentNumber 는 부분일치 검색. "
            + "페이지네이션: ?page=0&size=20&sort=startedAt,desc")
    @GetMapping
    public CommonResponse<PageResponse<SeatLeaveResponse>> findAllSeatLeaves(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) String studentName,
        @RequestParam(required = false) String studentNumber,
        @ParameterObject @PageableDefault(size = 20, sort = "startedAt",
            direction = org.springframework.data.domain.Sort.Direction.DESC)
        Pageable pageable) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        log.info("[DEBUG seat-leaves] requestedStoreId={}, role={}, currentStoreId={}, resolvedStoreId={}",
            storeId, SecurityUtil.getCurrentRole(),
            SecurityUtil.getCurrentStoreId(), resolvedStoreId);
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        Page<SeatLeaveResponse> page = seatLeaveService
            .findAllByPeriod(resolvedStoreId, start, end,
                studentName, studentNumber, pageable);
        return CommonResponse.success(PageResponse.from(page));
    }

    @Operation(summary = "좌석이탈 강제 복귀처리",
        description = "이탈 중인 학생을 관리자가 강제로 복귀 처리한다. "
            + "좌석 상태가 AWAY → IN_USE로 복원되고, 이탈 시간이 차감에 반영된다.")
    @PostMapping("/{id}/force-return")
    public CommonResponse<SeatLeaveResponse> forceEndLeave(@PathVariable Long id) {
        return CommonResponse.success(seatLeaveService.forceEndLeave(id));
    }

    @Operation(summary = "좌석이탈 엑셀 다운로드",
        description = "MANAGER: 자기 지점 기간별 내역. ADMIN: 전체 지점 기간별 내역. "
            + "엑셀(.xlsx) 파일로 다운로드한다.")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportSeatLeaves(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long storeId = SecurityUtil.resolveStoreId(null);
        byte[] excelBytes = seatLeaveService.exportToExcel(storeId, startDate, endDate);

        String filename = "seat-leaves_"
            + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "_"
            + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
