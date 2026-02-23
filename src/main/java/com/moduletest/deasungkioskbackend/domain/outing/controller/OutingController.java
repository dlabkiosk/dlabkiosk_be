package com.moduletest.deasungkioskbackend.domain.outing.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingEndRequest;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingResponse;
import com.moduletest.deasungkioskbackend.domain.outing.dto.OutingStartRequest;
import com.moduletest.deasungkioskbackend.domain.outing.service.OutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 외출", description = "학생 외출/복귀 처리 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/outings")
@RequiredArgsConstructor
public class OutingController {

    private final OutingService outingService;

    @Operation(summary = "외출 시작",
        description = "QR/RFID로 외출을 시작한다. 등원 상태가 아니면 거부된다. "
            + "좌석 사용 중이면 좌석 상태가 '외출 중'으로 변경된다. "
            + "qrUuid와 rfidUid 중 하나만 전송해야 하며, 나머지는 null이어야 한다.")
    @PostMapping("/start")
    public CommonResponse<OutingResponse> startOuting(
        @Valid @RequestBody OutingStartRequest request) {
        Long storeId = getStoreIdFromToken();
        OutingResponse response = outingService.startOuting(request, storeId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "외출 복귀",
        description = "QR/RFID로 외출에서 복귀한다. 진행 중인 외출이 없으면 거부된다. "
            + "좌석이 유지되어 있으면 좌석 상태가 '사용 중'으로 복원된다. "
            + "qrUuid와 rfidUid 중 하나만 전송해야 하며, 나머지는 null이어야 한다.")
    @PostMapping("/end")
    public CommonResponse<OutingResponse> endOuting(
        @Valid @RequestBody OutingEndRequest request) {
        Long storeId = getStoreIdFromToken();
        OutingResponse response = outingService.endOuting(request, storeId);
        return CommonResponse.success(response);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
