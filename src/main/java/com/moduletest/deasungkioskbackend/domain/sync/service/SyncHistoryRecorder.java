package com.moduletest.deasungkioskbackend.domain.sync.service;

import com.moduletest.deasungkioskbackend.domain.store.dto.StoreSyncResult;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncHistory;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncStatus;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncType;
import com.moduletest.deasungkioskbackend.domain.sync.repository.SyncHistoryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 동기화 결과를 sync_histories에 적재하는 공통 로직.
 * Why: 스케줄러/관리자 수동 트리거에서 동일한 형태로 이력을 남기기 위해 공유.
 */
@Component
@RequiredArgsConstructor
public class SyncHistoryRecorder {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final SyncHistoryRepository syncHistoryRepository;

    public void recordStore(StoreSyncResult result, LocalDateTime startedAt) {
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
    }

    public void recordStudent(StudentSyncResult result, LocalDateTime startedAt) {
        SyncStatus status = result.failed() == 0
            ? SyncStatus.SUCCESS : SyncStatus.PARTIAL;
        String errorMsg = result.errors().isEmpty()
            ? null : String.join("; ", result.errors());

        syncHistoryRepository.save(SyncHistory.builder()
            .syncType(SyncType.STUDENT)
            .storeId(result.storeId())
            .status(status)
            .totalCount(result.totalFromDsa())
            .createdCount(result.created())
            .updatedCount(result.updated())
            .failedCount(result.failed())
            .errorMessage(truncate(errorMsg))
            .startedAt(startedAt)
            .finishedAt(LocalDateTime.now())
            .build());
    }

    public void recordFailure(SyncType type, Long storeId, Throwable e,
                              LocalDateTime startedAt) {
        syncHistoryRepository.save(SyncHistory.builder()
            .syncType(type)
            .storeId(storeId)
            .status(SyncStatus.FAILED)
            .errorMessage(truncate(e.getMessage()))
            .startedAt(startedAt)
            .finishedAt(LocalDateTime.now())
            .build());
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > ERROR_MESSAGE_MAX_LENGTH
            ? value.substring(0, ERROR_MESSAGE_MAX_LENGTH) : value;
    }
}
