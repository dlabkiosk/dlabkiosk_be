package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
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

    @Scheduled(cron = "${sync.student.cron:0 0 4 * * *}")
    public void syncStudents() {
        log.info("[스케줄러] 학생 동기화 시작");

        try {
            List<StudentSyncResult> results = studentSyncService.synchronizeAllStores();

            for (StudentSyncResult result : results) {
                log.info("[스케줄러] {} - DSA: {}명, 생성: {}, 수정: {}, 변경없음: {}, 실패: {}",
                    result.storeName(), result.totalFromDsa(),
                    result.created(), result.updated(),
                    result.unchanged(), result.failed());
            }

            log.info("[스케줄러] 학생 동기화 완료: {}개 지점", results.size());
        } catch (Exception e) {
            log.error("[스케줄러] 학생 동기화 중 예외 발생", e);
        }
    }
}
