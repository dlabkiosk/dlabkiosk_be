package com.moduletest.deasungkioskbackend.domain.examschedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "시험 일정 생성 요청")
public record ExamScheduleCreateRequest(
    @Schema(description = "시험명", example = "2학기 중간고사")
    @NotBlank(message = "시험명은 필수입니다")
    @Size(max = 100, message = "시험명은 100자를 초과할 수 없습니다")
    String examName,

    @Schema(description = "시험일 (YYYY-MM-DD)", example = "2026-04-15")
    @NotNull(message = "시험일은 필수입니다")
    LocalDate examDate
) { }
