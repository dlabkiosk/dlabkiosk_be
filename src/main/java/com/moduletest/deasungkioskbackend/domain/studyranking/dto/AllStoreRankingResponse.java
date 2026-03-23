package com.moduletest.deasungkioskbackend.domain.studyranking.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AllStoreRankingResponse(
    List<RankingEntry> rankings
) {

    @Builder
    public record RankingEntry(
        int rank,
        String studentName,
        String storeName,
        String studyTime,
        long studyTimeMinutes
    ) {
    }
}
