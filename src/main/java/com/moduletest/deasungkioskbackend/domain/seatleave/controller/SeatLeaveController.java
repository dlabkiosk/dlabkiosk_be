package com.moduletest.deasungkioskbackend.domain.seatleave.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveEndRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveReasonResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveStartRequest;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveReasonService;
import com.moduletest.deasungkioskbackend.domain.seatleave.service.SeatLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 좌석이탈", description = "좌석이탈 신청/복귀 (KIOSK 인증 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/seat-leaves")
@RequiredArgsConstructor
public class SeatLeaveController {

    private final SeatLeaveService seatLeaveService;
    private final SeatLeaveReasonService seatLeaveReasonService;

    @Operation(summary = "이탈 사유 목록 조회",
        description = "현재 지점의 활성화된 이탈 사유를 조회한다.")
    @GetMapping("/reasons")
    public CommonResponse<List<SeatLeaveReasonResponse>> findActiveReasons() {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            seatLeaveReasonService.findAllActiveByStoreId(storeId));
    }

    @Operation(summary = "좌석이탈 신청",
        description = "좌석이탈을 신청한다. 학생 인식은 qrUuid/rfidUid/studentNumber/phone 중 하나만 전송.")
    @PostMapping("/start")
    public CommonResponse<SeatLeaveResponse> startSeatLeave(
        @Valid @RequestBody SeatLeaveStartRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(seatLeaveService.startLeave(storeId, request));
    }

    @Operation(summary = "좌석이탈 복귀",
        description = "좌석이탈 상태에서 복귀한다. 학생 인식은 qrUuid/rfidUid/studentNumber/phone 중 하나만 전송.")
    @PostMapping("/end")
    public CommonResponse<SeatLeaveResponse> endSeatLeave(
        @Valid @RequestBody SeatLeaveEndRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(seatLeaveService.endLeave(storeId, request));
    }
}
