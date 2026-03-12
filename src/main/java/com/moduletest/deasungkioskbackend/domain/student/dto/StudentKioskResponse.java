package com.moduletest.deasungkioskbackend.domain.student.dto;

import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "키오스크 학생 조회 응답 (좌석 변경 신청 포함)")
public record StudentKioskResponse(

    @Schema(description = "학생 ID", example = "1")
    Long id,

    @Schema(description = "학생 이름", example = "김민준")
    String name,

    @Schema(description = "학번", example = "2024-001")
    String studentNumber,

    @Schema(description = "배정 좌석 라벨", example = "A-1")
    String assignedSeatLabel,

    @Schema(description = "좌석 변경 신청 정보 (없으면 null)")
    SeatChangeRequestInfo seatChangeRequest
) {

    public static StudentKioskResponse of(Student student, SeatChangeRequest request) {
        String seatLabel = null;
        if (student.getAssignedSeat() != null) {
            seatLabel = student.getAssignedSeat().getSeatLabel();
        }

        SeatChangeRequestInfo requestInfo = null;
        if (request != null) {
            requestInfo = SeatChangeRequestInfo.fromEntity(request);
        }

        return new StudentKioskResponse(
            student.getId(),
            student.getName(),
            student.getStudentNumber(),
            seatLabel,
            requestInfo
        );
    }

    @Schema(description = "좌석 변경 신청 정보")
    public record SeatChangeRequestInfo(

        @Schema(description = "신청 ID", example = "1")
        Long id,

        @Schema(description = "신청 상태", example = "PENDING")
        String status,

        @Schema(description = "현재 좌석 라벨", example = "A-2")
        String currentSeatLabel,

        @Schema(description = "1순위 희망 좌석 라벨", example = "B-3")
        String desiredSeat1Label,

        @Schema(description = "2순위 희망 좌석 라벨 (없으면 null)", example = "C-1")
        String desiredSeat2Label,

        @Schema(description = "3순위 희망 좌석 라벨 (없으면 null)", example = "D-5")
        String desiredSeat3Label,

        @Schema(description = "승인된 좌석 라벨 (미승인 시 null)", example = "B-3")
        String approvedSeatLabel,

        @Schema(description = "신청 시간")
        LocalDateTime createdAt,

        @Schema(description = "처리 시간 (미처리 시 null)")
        LocalDateTime processedAt
    ) {

        public static SeatChangeRequestInfo fromEntity(SeatChangeRequest request) {
            return new SeatChangeRequestInfo(
                request.getId(),
                request.getStatus().name(),
                request.getCurrentSeat() != null
                    ? request.getCurrentSeat().getSeatLabel() : null,
                request.getDesiredSeat1().getSeatLabel(),
                request.getDesiredSeat2() != null
                    ? request.getDesiredSeat2().getSeatLabel() : null,
                request.getDesiredSeat3() != null
                    ? request.getDesiredSeat3().getSeatLabel() : null,
                request.getApprovedSeat() != null
                    ? request.getApprovedSeat().getSeatLabel() : null,
                request.getCreatedAt(),
                request.getProcessedAt()
            );
        }
    }
}
