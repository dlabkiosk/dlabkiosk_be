package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import java.time.LocalDateTime;

public record AttendanceResponse(
    Long id,
    Long studentId,
    String studentName,
    Long storeId,
    String storeName,
    AttendanceStatus status,
    LocalDateTime checkInAt,
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
