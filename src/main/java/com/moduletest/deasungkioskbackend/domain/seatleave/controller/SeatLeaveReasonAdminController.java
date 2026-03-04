package com.moduletest.deasungkioskbackend.domain.seatleave.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 좌석이탈 사유", description = "좌석이탈 사유 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seat-leave-reasons")
@RequiredArgsConstructor
public class SeatLeaveReasonAdminController {

    private final SeatLeaveReasonService seatLeaveReasonService;

    @Operation(summary = "이탈 사유 목록 조회",
        description = "지점별 이탈 사유를 조회한다. storeId 필수.")
    @GetMapping
    public CommonResponse<List<SeatLeaveReasonResponse>> findAllReasons(
        @RequestParam Long storeId) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        return CommonResponse.success(
            seatLeaveReasonService.findAllByStoreId(resolvedStoreId));
    }

    @Operation(summary = "이탈 사유 등록")
    @PostMapping
    public CommonResponse<SeatLeaveReasonResponse> createReason(
        @Valid @RequestBody SeatLeaveReasonCreateRequest request) {
        return CommonResponse.success(seatLeaveReasonService.createReason(request));
    }

    @Operation(summary = "이탈 사유 수정")
    @PutMapping("/{id}")
    public CommonResponse<SeatLeaveReasonResponse> updateReason(
        @PathVariable Long id,
        @Valid @RequestBody SeatLeaveReasonUpdateRequest request) {
        return CommonResponse.success(seatLeaveReasonService.updateReason(id, request));
    }

    @Operation(summary = "이탈 사유 삭제")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteReason(@PathVariable Long id) {
        seatLeaveReasonService.deleteReason(id);
        return CommonResponse.success(null);
    }
}
