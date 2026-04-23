package com.moduletest.deasungkioskbackend.domain.attendance.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AdminUpdateCheckInTimeRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AdminUpdateCheckOutRequest;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceStudentResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.service.AttendanceAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 출결 관리")
@RestController
@RequestMapping("/api/v1/admin/attendances")
@RequiredArgsConstructor
public final class AttendanceAdminController {

    private final AttendanceAdminService attendanceAdminService;

    @Operation(summary = "출결 현황 조회",
        description = "해당 지점 학생들의 출결 현황을 DSA 좌석 상태 기준으로 조회합니다.\n\n"
            + "응답의 checkedInAt 필드:\n"
            + "- 값이 있으면: 우리 시스템에서 등원 처리된 시각\n"
            + "- null이면: DSA에서 직접 등원 처리된 케이스 (우리 시스템에 기록 없음)\n\n"
            + "attendanceStatus 필터 값:\n"
            + "- 등원: DSA 좌석 상태 S\n"
            + "- 외출: DSA 좌석 상태 D\n"
            + "- 하원: DSA 좌석 상태 T\n"
            + "- 미출석: DSA 좌석 상태 X\n"
            + "- 공석: DSA 좌석 상태 B\n"
            + "- 통로: DSA 좌석 상태 E\n"
            + "- 미확인: DSA 좌석 매칭 안 됨 또는 규격 외 상태값\n\n"
            + "phoneSubmitted 필터:\n"
            + "- true: 오늘 휴대폰 미소지 신청한 학생만\n"
            + "- false: 미신청 학생만\n"
            + "- 미입력: 전체")
    @GetMapping
    public CommonResponse<List<AttendanceStudentResponse>> findAttendanceList(
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) String studentName,
        @RequestParam(required = false) String studentNumber,
        @RequestParam(required = false) String attendanceStatus,
        @RequestParam(required = false) Boolean phoneSubmitted) {

        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);

        List<AttendanceStudentResponse> result = attendanceAdminService.findAttendanceList(
            resolvedStoreId, studentName, studentNumber, attendanceStatus, phoneSubmitted);

        return CommonResponse.success(result);
    }

    @Operation(summary = "등원 시각 수정",
        description = "이미 등원 처리된 학생의 등원 시각을 수정합니다.\n"
            + "순공시간 보정 등의 용도로 사용합니다.")
    @PutMapping("/check-in-time")
    public CommonResponse<Void> updateCheckInTime(
        @Valid @RequestBody AdminUpdateCheckInTimeRequest request) {

        attendanceAdminService.updateCheckInTime(request.studentId(), request.checkInAt());
        return CommonResponse.success(null);
    }

    @Operation(summary = "하원/조퇴 시각 수정 또는 하원 취소",
        description = "학생의 당일 최신 하원(또는 조퇴) 기록을 수정합니다.\n\n"
            + "- checkOutAt에 시각을 넣으면: 하원 시각 수정 (checkOutAction으로 유형 변경도 가능. T=하원, C=조퇴)\n"
            + "- checkOutAt을 null로 보내면: 하원 취소 → 등원중 상태로 복원.\n"
            + "  Attendance status=CHECKED_IN 으로 되돌리고, 좌석(SeatUsage + Redis)도 IN_USE로 복원합니다.\n\n"
            + "⚠ 하원 취소 시 학생에게 이미 다른 CHECKED_IN 기록이 있으면 거절합니다 (이중 등원 방지).\n"
            + "DSA 상태와 어긋나지 않으려면 DSA 쪽을 먼저 수정한 뒤 이 API를 호출하세요.")
    @PutMapping("/check-out")
    public CommonResponse<Void> updateCheckOut(
        @Valid @RequestBody AdminUpdateCheckOutRequest request) {

        attendanceAdminService.updateCheckOut(
            request.studentId(), request.checkOutAt(), request.checkOutAction());
        return CommonResponse.success(null);
    }
}
