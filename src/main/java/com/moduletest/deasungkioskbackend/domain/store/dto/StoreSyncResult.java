package com.moduletest.deasungkioskbackend.domain.store.dto;

import java.util.List;

public record StoreSyncResult(
    int totalFromDsa,
    int created,
    int updated,
    int unchanged,
    List<String> errors
) {
}
