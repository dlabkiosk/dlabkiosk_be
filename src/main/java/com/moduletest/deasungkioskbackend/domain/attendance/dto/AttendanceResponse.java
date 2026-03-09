package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "출석 응답")
public record AttendanceResponse(
    @Schema(description = "출석 기록 ID", example = "1")
    Long id,
    @Schema(description = "학생 ID", example = "1")
    Long studentId,
    @Schema(description = "학생 이름", example = "김대성")
    String studentName,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "지점명", example = "대성학원 강남점")
    String storeName,
    @Schema(description = "출석 상태 (CHECKED_IN: 등원, CHECKED_OUT: 하원)", example = "CHECKED_IN")
    AttendanceStatus status,
    @Schema(description = "등원 시각")
    LocalDateTime checkInAt,
    @Schema(description = "하원 시각 (하원 전이면 null)")
    LocalDateTime checkOutAt,
    @Schema(description = "금일 순공시간 (분 단위, 하원 시에만 계산됨)", example = "578")
    Long studyTimeMinutes,
    @Schema(description = "학생 공지 메시지 (등원 시에만 포함, 없으면 null)")
    List<String> messages
) {

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getStudent().getId(),
            attendance.getStudent().getName(),
            attendance.getStore().getId(),
            attendance.getStore().getStoreName(),
            attendance.getStatus(),
            attendance.getCheckInAt(),
            attendance.getCheckOutAt(),
            null,
            null
        );
    }

    public static AttendanceResponse fromEntityWithMessages(Attendance attendance,
        List<String> messages) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getStudent().getId(),
            attendance.getStudent().getName(),
            attendance.getStore().getId(),
            attendance.getStore().getStoreName(),
            attendance.getStatus(),
            attendance.getCheckInAt(),
            attendance.getCheckOutAt(),
            null,
            messages.isEmpty() ? null : messages
        );
    }

    public static AttendanceResponse fromEntityWithStudyTime(Attendance attendance,
        long studyTimeMinutes) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getStudent().getId(),
            attendance.getStudent().getName(),
            attendance.getStore().getId(),
            attendance.getStore().getStoreName(),
            attendance.getStatus(),
            attendance.getCheckInAt(),
            attendance.getCheckOutAt(),
            studyTimeMinutes,
            null
        );
    }
}
