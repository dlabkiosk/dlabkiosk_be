package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AttendanceStudentResponse(
    Long studentId,
    String studentName,
    String studentNumber,
    String seatLabel,
    String attendanceStatus,
    LocalDateTime checkedInAt,
    boolean phoneSubmitted
) {
}
