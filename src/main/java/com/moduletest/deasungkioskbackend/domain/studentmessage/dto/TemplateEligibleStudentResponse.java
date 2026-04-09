package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "템플릿 미수신 학생 응답")
public record TemplateEligibleStudentResponse(
    @Schema(description = "학생 ID", example = "1")
    Long studentId,
    @Schema(description = "이름", example = "이민희")
    String name,
    @Schema(description = "학번", example = "20250001")
    String studentNumber,
    @Schema(description = "배정 좌석 번호 (없으면 null)", example = "A-12")
    String seatLabel
) {

    public static TemplateEligibleStudentResponse fromEntity(Student student) {
        return new TemplateEligibleStudentResponse(
            student.getId(),
            student.getName(),
            student.getStudentNumber(),
            student.getAssignedSeat() != null ? student.getAssignedSeat().getSeatLabel() : null
        );
    }
}
