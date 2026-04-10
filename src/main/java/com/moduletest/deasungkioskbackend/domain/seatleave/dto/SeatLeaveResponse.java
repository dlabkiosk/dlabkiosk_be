package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeave;
import java.time.LocalDateTime;

public record SeatLeaveResponse(
    Long id,
    Long storeId,
    String storeName,
    Long studentId,
    String studentName,
    String seatLabel,
    String reasonName,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {

    public static SeatLeaveResponse fromEntity(SeatLeave seatLeave) {
        return new SeatLeaveResponse(
            seatLeave.getId(),
            seatLeave.getStore().getId(),
            seatLeave.getStore().getStoreName(),
            seatLeave.getStudent().getId(),
            seatLeave.getStudent().getName(),
            seatLeave.getSeat().getSeatLabel(),
            seatLeave.getReason().getReasonName(),
            seatLeave.getStartedAt(),
            seatLeave.getEndedAt()
        );
    }
}
