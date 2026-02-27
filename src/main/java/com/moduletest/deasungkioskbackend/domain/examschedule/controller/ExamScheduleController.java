package com.moduletest.deasungkioskbackend.domain.examschedule.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleResponse;
import com.moduletest.deasungkioskbackend.domain.examschedule.service.ExamScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 시험 일정", description = "시험 일정 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    @Operation(summary = "지점 시험 일정 조회",
        description = "해당 지점의 시험 일정을 시험일 오름차순으로 조회한다.")
    @GetMapping
    public CommonResponse<List<ExamScheduleResponse>> findExamSchedulesByStore() {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        List<ExamScheduleResponse> examSchedules =
            examScheduleService.findAllExamSchedulesByStoreId(storeId);
        return CommonResponse.success(examSchedules);
    }
}
