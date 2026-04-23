package com.moduletest.deasungkioskbackend.domain.outing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "관리자 외출 수정 요청. endedAt=null 이면 외출 진행 중 상태로 되돌림(복귀 취소)")
public record AdminUpdateOutingRequest(
    @NotNull
    @Schema(description = "외출 시작 시각", example = "2026-04-23T14:00:00")
    LocalDateTime startedAt,

    @Schema(description = "외출 종료 시각. null이면 복귀 취소 (외출 진행 중으로 복원)",
        example = "2026-04-23T14:30:00")
    LocalDateTime endedAt
) {
}
