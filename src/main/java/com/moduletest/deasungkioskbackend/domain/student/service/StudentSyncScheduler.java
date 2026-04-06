package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.domain.store.dto.StoreSyncResult;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreSyncService;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncHistory;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncStatus;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncType;
import com.moduletest.deasungkioskbackend.domain.sync.repository.SyncHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class StudentSyncScheduler {

    private final StudentSyncService studentSyncService;
    private final StoreSyncService storeSyncService;
    private final SyncHistoryRepository syncHistoryRepository;

    @Scheduled(cron = "${sync.student.cron:0 0 4 * * *}")
    public void syncAll() {
        syncStores();
        syncStudents();
    }

    private void syncStores() {
        log.info("[스케줄러] 지점 동기화 시작");
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            StoreSyncResult result = storeSyncService.synchronize();
            log.info("[스케줄러] 지점 동기화 완료 - 생성: {}, 수정: {}, 변경없음: {}",
                result.created(), result.updated(), result.unchanged());

            SyncStatus status = result.errors().isEmpty()
                ? SyncStatus.SUCCESS : SyncStatus.PARTIAL;
            String errorMsg = result.errors().isEmpty()
                ? null : String.join("; ", result.errors());

            syncHistoryRepository.save(SyncHistory.builder()
                .syncType(SyncType.STORE)
                .status(status)
                .totalCount(result.totalFromDsa())
                .createdCount(result.created())
                .updatedCount(result.updated())
                .failedCount(result.errors().size())
                .errorMessage(truncate(errorMsg))
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .build());
        } catch (Exception e) {
            log.error("[스케줄러] 지점 동기화 중 예외 발생 — 학생 동기화는 계속 진행", e);

            syncHistoryRepository.save(SyncHistory.builder()
                .syncType(SyncType.STORE)
                .status(SyncStatus.FAILED)
                .errorMessage(truncate(e.getMessage()))
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .build());
        }
    }

    private void syncStudents() {
        log.info("[스케줄러] 학생 동기화 시작");
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            List<StudentSyncResult> results = studentSyncService.synchronizeAllStores();

            int totalCreated = 0;
            int totalUpdated = 0;
            int totalFailed = 0;
            int totalFromDsa = 0;

            for (StudentSyncResult result : results) {
                log.info("[스케줄러] {} - DSA: {}명, 생성: {}, 수정: {}, 변경없음: {}, 실패: {}",
                    result.storeName(), result.totalFromDsa(),
                    result.created(), result.updated(),
                    result.unchanged(), result.failed());

                totalFromDsa += result.totalFromDsa();
                totalCreated += result.created();
                totalUpdated += result.updated();
                totalFailed += result.failed();

                // 지점별 이력 저장
                SyncStatus storeStatus = result.failed() == 0
                    ? SyncStatus.SUCCESS : SyncStatus.PARTIAL;
                String errorMsg = result.errors().isEmpty()
                    ? null : String.join("; ", result.errors());

                syncHistoryRepository.save(SyncHistory.builder()
                    .syncType(SyncType.STUDENT)
                    .storeId(result.storeId())
                    .status(storeStatus)
                    .totalCount(result.totalFromDsa())
                    .createdCount(result.created())
                    .updatedCount(result.updated())
                    .failedCount(result.failed())
                    .errorMessage(truncate(errorMsg))
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                    .build());
            }

            log.info("[스케줄러] 학생 동기화 완료: {}개 지점, DSA: {}명, 생성: {}, 수정: {}, 실패: {}",
                results.size(), totalFromDsa, totalCreated, totalUpdated, totalFailed);
        } catch (Exception e) {
            log.error("[스케줄러] 학생 동기화 중 예외 발생", e);

            syncHistoryRepository.save(SyncHistory.builder()
                .syncType(SyncType.STUDENT)
                .status(SyncStatus.FAILED)
                .errorMessage(truncate(e.getMessage()))
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .build());
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }
}
