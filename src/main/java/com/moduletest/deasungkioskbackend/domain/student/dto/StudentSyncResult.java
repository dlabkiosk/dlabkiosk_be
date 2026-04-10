package com.moduletest.deasungkioskbackend.domain.student.dto;

import java.util.List;

public record StudentSyncResult(
    Long storeId,
    String storeName,
    int totalFromDsa,
    int created,
    int updated,
    int unchanged,
    int deactivated,
    int failed,
    List<String> errors
) {
}
