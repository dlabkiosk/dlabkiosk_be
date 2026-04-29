package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.domain.store.dto.StoreSyncResult;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreSyncService;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncType;
import com.moduletest.deasungkioskbackend.domain.sync.service.SyncHistoryRecorder;
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
    private final SyncHistoryRecorder syncHistoryRecorder;

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
            syncHistoryRecorder.recordStore(result, startedAt);
        } catch (Exception e) {
            log.error("[스케줄러] 지점 동기화 중 예외 발생 — 학생 동기화는 계속 진행", e);
            syncHistoryRecorder.recordFailure(SyncType.STORE, null, e, startedAt);
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
                log.info("[스케줄러] {} - DSA: {}명, 생성: {}, 수정: {}, 변경없음: {},"
                        + " 비활성화: {}, 실패: {}",
                    result.storeName(), result.totalFromDsa(),
                    result.created(), result.updated(),
                    result.unchanged(), result.deactivated(), result.failed());

                totalFromDsa += result.totalFromDsa();
                totalCreated += result.created();
                totalUpdated += result.updated();
                totalFailed += result.failed();

                syncHistoryRecorder.recordStudent(result, startedAt);
            }

            log.info("[스케줄러] 학생 동기화 완료: {}개 지점, DSA: {}명, 생성: {}, 수정: {}, 실패: {}",
                results.size(), totalFromDsa, totalCreated, totalUpdated, totalFailed);
        } catch (Exception e) {
            log.error("[스케줄러] 학생 동기화 중 예외 발생", e);
            syncHistoryRecorder.recordFailure(SyncType.STUDENT, null, e, startedAt);
        }
    }
}
