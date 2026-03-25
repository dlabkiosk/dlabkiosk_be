package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
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
    @Schema(description = "반", example = "A반")
    String className,
    @Schema(description = "배정 좌석", example = "A-1")
    String seatLabel,
    @Schema(description = "신청 유형", example = "DAILY")
    PhoneSubmissionType submissionType,
    @Schema(description = "휴대폰 뒷자리", example = "1234")
    String phoneLast4,
    @Schema(description = "학부모 전화번호", example = "010-1234-5678")
    String parentPhoneNumber,
    @Schema(description = "시작일")
    LocalDate startDate,
    @Schema(description = "종료일 (무기한이면 null)")
    LocalDate endDate,
    @Schema(description = "메모")
    String memo,
    @Schema(description = "신청 시간")
    LocalDateTime submittedAt
) {

    public static PhoneSubmissionResponse fromEntity(PhoneSubmission ps) {
        return fromEntity(ps, ps.getParentPhone());
    }

    public static PhoneSubmissionResponse fromEntity(PhoneSubmission ps,
        String parentPhoneNumber) {
        return new PhoneSubmissionResponse(
            ps.getId(),
            ps.getStudent().getId(),
            ps.getStudent().getName(),
            ps.getStudent().getStudentNumber(),
            null,
            ps.getStudent().getAssignedSeat() != null
                ? ps.getStudent().getAssignedSeat().getSeatLabel() : null,
            ps.getSubmissionType(),
            ps.getStudent().getPhoneLast4(),
            parentPhoneNumber,
            ps.getStartDate(),
            ps.getEndDate(),
            ps.getMemo(),
            ps.getSubmittedAt()
        );
    }
}
