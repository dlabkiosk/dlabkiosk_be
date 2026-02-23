package com.moduletest.deasungkioskbackend.domain.attendance.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckInRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.CheckOutRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public CommonResponse<AttendanceResponse> checkIn(
        @Valid @RequestBody CheckInRequest request) {
        AttendanceResponse attendance = attendanceService.checkIn(request);
        return CommonResponse.success(attendance);
    }

    @PostMapping("/check-out")
    public CommonResponse<AttendanceResponse> checkOut(
        @Valid @RequestBody CheckOutRequest request) {
        AttendanceResponse attendance = attendanceService.checkOut(request);
        return CommonResponse.success(attendance);
    }
}
