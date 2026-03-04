package com.moduletest.deasungkioskbackend.domain.seatleave.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveService;
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

@Tag(name = "[관리자] 좌석이탈 현황", description = "좌석이탈 현황 조회 및 엑셀 다운로드 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seat-leaves")
@RequiredArgsConstructor
public class SeatLeaveAdminController {

    private final SeatLeaveService seatLeaveService;

    @Operation(summary = "당일 좌석이탈 현황 조회",
        description = "지점의 당일 좌석이탈 내역을 조회한다.")
    @GetMapping
    public CommonResponse<List<SeatLeaveResponse>> findAllSeatLeavesToday(
        @RequestParam(required = false) Long storeId) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        return CommonResponse.success(
            seatLeaveService.findAllByStoreIdToday(resolvedStoreId));
    }

    @Operation(summary = "좌석이탈 엑셀 다운로드",
        description = "기간별 좌석이탈 내역을 엑셀(.xlsx)로 다운로드한다.")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportSeatLeaves(
        @RequestParam(required = false) Long storeId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        byte[] excelBytes = seatLeaveService.exportToExcel(resolvedStoreId, startDate, endDate);

        String filename = "seat-leaves_"
            + startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "_"
            + endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }
}
