package com.moduletest.deasungkioskbackend.domain.outing.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.outing.dto.AdminUpdateOutingRequest;
import com.moduletest.deasungkioskbackend.domain.outing.service.OutingAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 외출 관리")
@RestController
@RequestMapping("/api/v1/admin/outings")
@RequiredArgsConstructor
public final class OutingAdminController {

    private final OutingAdminService outingAdminService;

    @Operation(summary = "외출 수정 / 복귀 취소 / 복귀 처리",
        description = "외출 기록의 시작/종료 시각을 수정합니다.\n\n"
            + "- 기존 endedAt=null + 새 endedAt=null → startedAt만 수정\n"
            + "- 기존 endedAt=null + 새 endedAt=값 → 복귀 처리 (차감 누적, 좌석 IN_USE)\n"
            + "- 기존 endedAt=값 + 새 endedAt=null → 복귀 취소 (기존 차감 롤백, 좌석 OUTING)\n"
            + "- 기존 endedAt=값 + 새 endedAt=값 → 기간 수정 (기존 차감 롤백 후 새 차감 누적)\n\n"
            + "⚠ DSA 상태와 어긋나지 않으려면 DSA 쪽을 먼저 수정한 뒤 이 API를 호출하세요.")
    @PutMapping("/{outingId}")
    public CommonResponse<Void> updateOuting(
        @PathVariable Long outingId,
        @Valid @RequestBody AdminUpdateOutingRequest request) {

        outingAdminService.updateOuting(
            outingId, request.startedAt(), request.endedAt());
        return CommonResponse.success(null);
    }

    @Operation(summary = "외출 삭제",
        description = "외출 기록을 완전히 삭제합니다.\n\n"
            + "- 진행 중 외출 → Redis 좌석 OUTING→IN_USE 복원\n"
            + "- 완료된 외출 → 해당 외출 시간만큼 순공시간 차감 롤백")
    @DeleteMapping("/{outingId}")
    public CommonResponse<Void> deleteOuting(@PathVariable Long outingId) {
        outingAdminService.deleteOuting(outingId);
        return CommonResponse.success(null);
    }
}
