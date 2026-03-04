package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "휴대폰 미소지 응답")
public record PhoneSubmissionResponse(
    @Schema(description = "신청 ID", example = "1")
    Long id,
    @Schema(description = "학생 ID", example = "3")
    Long studentId,
    @Schema(description = "학생 이름", example = "김민준")
    String studentName,
    @Schema(description = "학번", example = "2024-001")
    String studentNumber,
    @Schema(description = "배정 좌석", example = "A-1")
    String seatLabel,
    @Schema(description = "신청 시간")
    LocalDateTime submittedAt
) {

    public static PhoneSubmissionResponse fromEntity(PhoneSubmission ps) {
        return new PhoneSubmissionResponse(
            ps.getId(),
            ps.getStudent().getId(),
            ps.getStudent().getName(),
            ps.getStudent().getStudentNumber(),
            ps.getStudent().getAssignedSeat() != null
                ? ps.getStudent().getAssignedSeat().getSeatLabel() : null,
            ps.getSubmittedAt()
        );
    }
}
