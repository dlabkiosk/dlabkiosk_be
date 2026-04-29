package com.moduletest.deasungkioskbackend.domain.store.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreSyncResult;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreSyncService;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncType;
import com.moduletest.deasungkioskbackend.domain.sync.service.SyncHistoryRecorder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "[관리자] 지점 동기화")
@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public final class StoreSyncAdminController {

    private final StoreSyncService storeSyncService;
    private final SyncHistoryRecorder syncHistoryRecorder;

    @Operation(summary = "지점 동기화 (DSA → DB)",
        description = "DSA 3.19 getDlabList를 호출하여 지점 데이터를 동기화합니다. "
            + "기존 지점은 이름을 업데이트하고, 새 지점은 자동 생성합니다.")
    @PostMapping("/stores")
    public CommonResponse<StoreSyncResult> syncStores() {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            StoreSyncResult result = storeSyncService.synchronize();
            syncHistoryRecorder.recordStore(result, startedAt);
            return CommonResponse.success(result);
        } catch (RuntimeException e) {
            log.error("[수동] 지점 동기화 중 예외 발생", e);
            syncHistoryRecorder.recordFailure(SyncType.STORE, null, e, startedAt);
            throw e;
        }
    }
}
