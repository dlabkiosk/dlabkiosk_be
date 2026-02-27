package com.moduletest.deasungkioskbackend.domain.examschedule.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleCreateRequest;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleResponse;
import com.moduletest.deasungkioskbackend.domain.examschedule.dto.ExamScheduleUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.examschedule.service.ExamScheduleService;
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

@Tag(name = "[관리자] 시험 일정", description = "시험 일정 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleAdminController {

    private final ExamScheduleService examScheduleService;

    @Operation(summary = "시험 일정 목록 조회",
        description = "전체 또는 지점별 시험 일정을 조회한다. storeId 미입력 시 전체 조회.")
    @GetMapping
    public CommonResponse<List<ExamScheduleResponse>> findAllExamSchedules(
        @RequestParam(required = false) Long storeId) {
        List<ExamScheduleResponse> examSchedules;
        if (storeId != null) {
            examSchedules = examScheduleService.findAllExamSchedulesByStoreId(storeId);
        } else {
            examSchedules = examScheduleService.findAllExamSchedules();
        }
        return CommonResponse.success(examSchedules);
    }

    @Operation(summary = "시험 일정 단건 조회", description = "시험 일정 ID로 단건 조회한다.")
    @GetMapping("/{examScheduleId}")
    public CommonResponse<ExamScheduleResponse> findExamScheduleById(
        @PathVariable Long examScheduleId) {
        ExamScheduleResponse examSchedule =
            examScheduleService.findExamScheduleById(examScheduleId);
        return CommonResponse.success(examSchedule);
    }

    @Operation(summary = "시험 일정 등록", description = "새 시험 일정을 등록한다.")
    @PostMapping
    public CommonResponse<ExamScheduleResponse> createExamSchedule(
        @Valid @RequestBody ExamScheduleCreateRequest request) {
        ExamScheduleResponse examSchedule = examScheduleService.createExamSchedule(request);
        return CommonResponse.success(examSchedule);
    }

    @Operation(summary = "시험 일정 수정", description = "시험 일정을 수정한다.")
    @PutMapping("/{examScheduleId}")
    public CommonResponse<ExamScheduleResponse> updateExamSchedule(
        @PathVariable Long examScheduleId,
        @Valid @RequestBody ExamScheduleUpdateRequest request) {
        ExamScheduleResponse examSchedule =
            examScheduleService.updateExamSchedule(examScheduleId, request);
        return CommonResponse.success(examSchedule);
    }

    @Operation(summary = "시험 일정 삭제", description = "시험 일정을 삭제한다.")
    @DeleteMapping("/{examScheduleId}")
    public CommonResponse<Void> deleteExamSchedule(@PathVariable Long examScheduleId) {
        examScheduleService.deleteExamSchedule(examScheduleId);
        return CommonResponse.success(null);
    }
}
