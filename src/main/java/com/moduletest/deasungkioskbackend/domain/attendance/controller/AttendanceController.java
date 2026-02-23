package com.moduletest.deasungkioskbackend.domain.attendance.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckInRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckOutRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 출석", description = "QR 스캔을 통한 등원/하원 처리 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "등원 (체크인)",
        description = "키오스크에서 QR을 스캔하면 UUID로 등원 처리한다. "
            + "당일 이미 등원 상태면 거부된다. 학생이 해당 지점 소속이 아니면 거부된다. "
            + "qrUuid와 rfidUid 중 하나만 전송해야 하며, 나머지는 null이어야 한다.")
    @PostMapping("/check-in")
    public CommonResponse<AttendanceResponse> checkIn(
        @Valid @RequestBody CheckInRequest request) {
        Long storeId = getStoreIdFromToken();
        AttendanceResponse attendance = attendanceService.checkIn(request, storeId);
        return CommonResponse.success(attendance);
    }

    @Operation(summary = "하원 (체크아웃)",
        description = "QR/RFID로 하원 처리한다. 당일 등원 기록이 없으면 거부된다. "
            + "qrUuid와 rfidUid 중 하나만 전송해야 하며, 나머지는 null이어야 한다.")
    @PostMapping("/check-out")
    public CommonResponse<AttendanceResponse> checkOut(
        @Valid @RequestBody CheckOutRequest request) {
        Long storeId = getStoreIdFromToken();
        AttendanceResponse attendance = attendanceService.checkOut(request, storeId);
        return CommonResponse.success(attendance);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
