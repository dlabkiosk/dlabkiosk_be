package com.moduletest.deasungkioskbackend.domain.attendance.dto;

import lombok.Builder;

@Builder
public record AttendanceStudentResponse(
    Long studentId,
    String studentName,
    String studentNumber,
    String seatLabel,
    String attendanceStatus,
    boolean phoneSubmitted
) {
}
