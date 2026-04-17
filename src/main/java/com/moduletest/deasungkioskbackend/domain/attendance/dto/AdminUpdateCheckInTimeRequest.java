package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "관리자 등원 시각 수정 요청")
public record AdminUpdateCheckInTimeRequest(
    @NotNull
    @Schema(description = "학생 ID", example = "1")
    Long studentId,

    @NotNull
    @Schema(description = "수정할 등원 시각", example = "2026-04-17T08:30:00")
    LocalDateTime checkInAt
) {
}
