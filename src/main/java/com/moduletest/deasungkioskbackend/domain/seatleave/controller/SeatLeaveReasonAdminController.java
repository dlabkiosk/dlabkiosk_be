package com.moduletest.deasungkioskbackend.domain.seatleave.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "[관리자] 좌석이탈 사유", description = "좌석이탈 사유 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seat-leave-reasons")
@RequiredArgsConstructor
public class SeatLeaveReasonAdminController {

    private final SeatLeaveReasonService seatLeaveReasonService;

    @Operation(summary = "이탈 사유 목록 조회",
        description = "MANAGER: 자기 지점 이탈 사유 조회. ADMIN: 전체 지점 이탈 사유 조회.")
    @GetMapping
    public CommonResponse<List<SeatLeaveReasonResponse>> findAllReasons() {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            seatLeaveReasonService.findAllByStoreId(storeId));
    }

    @Operation(summary = "이탈 사유 등록",
        description = "MANAGER: 자기 지점에 자동 등록. ADMIN: storeId 필수 — 등록할 지점 지정. "
            + "multipart/form-data로 아이콘 파일과 설정을 함께 전송. "
            + "지점당 최대 9개까지 등록 가능, 아이콘 파일 필수.")
    @PostMapping(consumes = "multipart/form-data")
    public CommonResponse<SeatLeaveReasonResponse> createReason(
        @RequestParam(required = false) Long storeId,
        @RequestParam("iconFile") MultipartFile iconFile,
        @RequestParam String reasonName,
        @RequestParam(defaultValue = "0") int displayOrder,
        @RequestParam(defaultValue = "true") boolean active) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        return CommonResponse.success(
            seatLeaveReasonService.createReason(
                resolvedStoreId, iconFile, reasonName, displayOrder, active));
    }

    @Operation(summary = "이탈 사유 수정",
        description = "아이콘 변경 시 iconFile 포함, 설정만 변경 시 iconFile 없이 전송. "
            + "iconFile 전송 시 기존 S3 파일은 삭제된다.")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public CommonResponse<SeatLeaveReasonResponse> updateReason(
        @PathVariable Long id,
        @RequestParam(value = "iconFile", required = false) MultipartFile iconFile,
        @RequestParam String reasonName,
        @RequestParam(defaultValue = "0") int displayOrder,
        @RequestParam(defaultValue = "true") boolean active) {
        return CommonResponse.success(
            seatLeaveReasonService.updateReason(id, iconFile, reasonName, displayOrder, active));
    }

    @Operation(summary = "이탈 사유 삭제",
        description = "사유 삭제 시 등록된 아이콘 파일도 S3에서 함께 삭제된다.")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteReason(@PathVariable Long id) {
        seatLeaveReasonService.deleteReason(id);
        return CommonResponse.success(null);
    }
}
