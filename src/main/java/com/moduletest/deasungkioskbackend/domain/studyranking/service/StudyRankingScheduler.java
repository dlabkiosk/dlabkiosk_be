package com.moduletest.deasungkioskbackend.domain.studyranking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class StudyRankingScheduler {

    private final StudyRankingService studyRankingService;

    @Scheduled(cron = "${sync.study-ranking.cron:0 30 4 * * *}")
    public void refresh() {
        log.info("[스케줄러] 순공랭킹 캐시 갱신 시작");
        try {
            studyRankingService.refreshAllCaches();
            log.info("[스케줄러] 순공랭킹 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 순공랭킹 캐시 갱신 중 예외 발생", e);
        }
    }
}
