package com.moduletest.deasungkioskbackend.domain.seat.dto;

import java.util.List;

public record SeatStateSyncResult(
    Long storeId,
    String storeName,
    int dsaStudentCount,
    int inUseApplied,
    int outingApplied,
    int skippedNoRfid,
    int skippedNoSeat,
    int skippedStudentNotFound,
    int skippedStoreMismatch,
    int skippedNonActiveState,
    int alreadyInUse,
    int failed,
    List<String> errors) {
}
