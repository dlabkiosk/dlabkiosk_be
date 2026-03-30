package com.moduletest.deasungkioskbackend.domain.attendance.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.attendance.dto.AttendanceStudentResponse;
import com.moduletest.deasungkioskbackend.domain.attendance.service.AttendanceAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
            + "attendanceStatus 필터 값:\n"
            + "- 등원: DSA 좌석 상태 S\n"
            + "- 외출: DSA 좌석 상태 D\n"
            + "- 미출석: DSA 좌석 상태 N/B\n"
            + "- 미확인: DSA 좌석 매칭 안 됨 (테스트 서버 데이터 불일치로 seat_cd 미매칭)\n\n"
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
}
