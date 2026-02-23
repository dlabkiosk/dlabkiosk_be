package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

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
    LocalDateTime checkOutAt
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
            attendance.getCheckOutAt()
        );
    }
}
