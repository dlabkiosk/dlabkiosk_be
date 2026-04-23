package com.moduletest.deasungkioskbackend.domain.outing.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.outing.dto.AdminUpdateOutingRequest;
import com.moduletest.deasungkioskbackend.domain.outing.service.OutingAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 외출 관리")
@RestController
@RequestMapping("/api/v1/admin/outings")
@RequiredArgsConstructor
public final class OutingAdminController {

    private final OutingAdminService outingAdminService;

    @Operation(summary = "외출 수정 / 복귀 취소 / 복귀 처리",
        description = "해당 학생의 오늘치 최신 외출 기록을 수정합니다.\n\n"
            + "- 기존 endedAt=null + 새 endedAt=null → startedAt만 수정\n"
            + "- 기존 endedAt=null + 새 endedAt=값 → 복귀 처리 (차감 누적, 좌석 IN_USE)\n"
            + "- 기존 endedAt=값 + 새 endedAt=null → 복귀 취소 (기존 차감 롤백, 좌석 OUTING)\n"
            + "- 기존 endedAt=값 + 새 endedAt=값 → 기간 수정 (기존 차감 롤백 후 새 차감 누적)\n\n"
            + "⚠ DSA 상태와 어긋나지 않으려면 DSA 쪽을 먼저 수정한 뒤 이 API를 호출하세요.")
    @PutMapping
    public CommonResponse<Void> updateOuting(@Valid @RequestBody AdminUpdateOutingRequest request) {
        outingAdminService.updateOutingByStudent(
            request.studentId(), request.startedAt(), request.endedAt());
        return CommonResponse.success(null);
    }

    @Operation(summary = "진행 중 외출 삭제 (유령 외출 정리)",
        description = "해당 학생의 오늘치 진행 중(ended_at IS NULL) 외출 기록을 삭제합니다.\n\n"
            + "- 더블 태그 등으로 잘못 생성된 phantom 외출 정리 용도\n"
            + "- Redis 좌석 상태 OUTING→IN_USE 복원\n"
            + "- 진행 중 외출이 없으면 404 응답")
    @DeleteMapping
    public CommonResponse<Void> deleteActiveOuting(@RequestParam Long studentId) {
        outingAdminService.deleteActiveOutingByStudent(studentId);
        return CommonResponse.success(null);
    }
}
