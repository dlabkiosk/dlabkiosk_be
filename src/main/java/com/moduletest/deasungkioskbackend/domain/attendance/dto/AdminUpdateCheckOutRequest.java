package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "관리자 하원/조퇴 수정 요청. checkOutAt=null이면 하원 취소(다시 등원중으로 복원)")
public record AdminUpdateCheckOutRequest(
    @NotNull
    @Schema(description = "학생 ID", example = "1")
    Long studentId,

    @Schema(description = "수정할 하원 시각. null이면 하원 취소", example = "2026-04-23T18:00:00")
    LocalDateTime checkOutAt,

    @Schema(description = "하원 유형 (T=하원, C=조퇴). 시각 수정 시에만 사용. 취소 시 무시",
        example = "T")
    String checkOutAction
) {
}
