package com.moduletest.deasungkioskbackend.domain.examschedule.dto;

import com.moduletest.deasungkioskbackend.domain.examschedule.entity.ExamSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "시험 일정 응답")
public record ExamScheduleResponse(
    @Schema(description = "시험 일정 ID", example = "1")
    Long id,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "시험명", example = "2학기 중간고사")
    String examName,
    @Schema(description = "시험일", example = "2026-04-15")
    LocalDate examDate,
    @Schema(description = "생성일시")
    LocalDateTime createdAt,
    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

    public static ExamScheduleResponse fromEntity(ExamSchedule examSchedule) {
        return new ExamScheduleResponse(
            examSchedule.getId(),
            examSchedule.getStore().getId(),
            examSchedule.getStore().getStoreName(),
            examSchedule.getExamName(),
            examSchedule.getExamDate(),
            examSchedule.getCreatedAt(),
            examSchedule.getUpdatedAt()
        );
    }
}
