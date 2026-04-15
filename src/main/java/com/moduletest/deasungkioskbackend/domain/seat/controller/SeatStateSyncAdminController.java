package com.moduletest.deasungkioskbackend.domain.seat.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStateSyncResult;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatStateSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 좌석 상태 동기화")
@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public final class SeatStateSyncAdminController {

    private final SeatStateSyncService seatStateSyncService;

    @Operation(summary = "좌석 상태 동기화 (DSA → DB/Redis)",
        description = "DSA 3.20 + 3.7 + 3.8 + 3.10을 조합하여 현재 DSA의 좌석 상태(등원/외출)를 "
            + "우리 DB의 SeatUsage(IN_USE)와 Redis 좌석 상태에 반영합니다. "
            + "기존 DB를 날린 뒤 운영 중인 DSA와 상태를 맞출 때 사용합니다. "
            + "state=S는 IN_USE, state=D는 OUTING으로 반영하며, 나머지 상태는 skip합니다. "
            + "startedAt은 현재 시각으로 박히므로 순공시간 계산은 정확하지 않을 수 있습니다. "
            + "MANAGER는 자기 지점만 호출 가능합니다.")
    @PostMapping("/seat-states")
    public CommonResponse<SeatStateSyncResult> syncSeatStates(
        @RequestParam Long storeId) {

        Long resolved = SecurityUtil.resolveStoreIdRequired(storeId);
        SeatStateSyncResult result = seatStateSyncService.synchronizeByStoreId(resolved);
        return CommonResponse.success(result);
    }
}
