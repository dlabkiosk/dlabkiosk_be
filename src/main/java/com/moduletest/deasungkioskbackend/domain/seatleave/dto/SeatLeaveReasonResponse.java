package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import com.moduletest.deasungkioskbackend.domain.seatleave.entity.SeatLeaveReason;

public record SeatLeaveReasonResponse(
    Long id,
    Long storeId,
    String reasonName,
    int displayOrder,
    boolean active,
    String iconUrl
) {

    public static SeatLeaveReasonResponse fromEntity(SeatLeaveReason reason) {
        return new SeatLeaveReasonResponse(
            reason.getId(),
            reason.getStore().getId(),
            reason.getReasonName(),
            reason.getDisplayOrder(),
            reason.isActive(),
            reason.getIconUrl()
        );
    }
}
