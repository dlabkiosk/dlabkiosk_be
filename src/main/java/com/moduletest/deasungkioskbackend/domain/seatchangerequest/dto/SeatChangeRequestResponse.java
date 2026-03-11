package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "좌석 변경 신청 응답 (관리자)")
public record SeatChangeRequestResponse(

    @Schema(description = "신청 ID", example = "1")
    Long id,

    @Schema(description = "학생 ID", example = "3")
    Long studentId,

    @Schema(description = "학생 이름", example = "김민준")
    String studentName,

    @Schema(description = "학번", example = "20250101")
    String studentNumber,

    @Schema(description = "반", example = "A반")
    String className,

    @Schema(description = "지점 ID", example = "1")
    Long storeId,

    @Schema(description = "지점명", example = "강남점")
    String storeName,

    @Schema(description = "현재 좌석 라벨 (배정 없으면 null)", example = "A-2")
    String currentSeatLabel,

    @Schema(description = "1순위 희망 좌석 라벨", example = "B-3")
    String desiredSeat1Label,

    @Schema(description = "2순위 희망 좌석 라벨 (없으면 null)", example = "C-1")
    String desiredSeat2Label,

    @Schema(description = "3순위 희망 좌석 라벨 (없으면 null)", example = "D-5")
    String desiredSeat3Label,

    @Schema(description = "승인된 좌석 라벨 (미승인 시 null)", example = "B-3")
    String approvedSeatLabel,

    @Schema(description = "신청 상태", example = "PENDING")
    String status,

    @Schema(description = "신청 시간")
    LocalDateTime createdAt,

    @Schema(description = "처리 시간 (미처리 시 null)")
    LocalDateTime processedAt
) {

    public static SeatChangeRequestResponse fromEntity(SeatChangeRequest request) {
        return new SeatChangeRequestResponse(
            request.getId(),
            request.getStudent().getId(),
            request.getStudent().getName(),
            request.getStudent().getStudentNumber(),
            request.getStudent().getClassName(),
            request.getStore().getId(),
            request.getStore().getStoreName(),
            request.getCurrentSeat() != null
                ? request.getCurrentSeat().getSeatLabel() : null,
            request.getDesiredSeat1().getSeatLabel(),
            request.getDesiredSeat2() != null
                ? request.getDesiredSeat2().getSeatLabel() : null,
            request.getDesiredSeat3() != null
                ? request.getDesiredSeat3().getSeatLabel() : null,
            request.getApprovedSeat() != null
                ? request.getApprovedSeat().getSeatLabel() : null,
            request.getStatus().name(),
            request.getCreatedAt(),
            request.getProcessedAt()
        );
    }
}
