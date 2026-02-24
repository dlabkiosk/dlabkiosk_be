package com.moduletest.deasungkioskbackend.domain.seat.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCheckInRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 좌석", description = "좌석 현황 조회 및 입퇴실 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "좌석 현황 조회",
        description = "로그인한 지점의 전체 좌석 현황을 조회한다. "
            + "화면 진입 시 1회 호출. 입실 실패 시 최신 현황 재조회.")
    @GetMapping
    public CommonResponse<List<SeatStatusResponse>> findSeatStatus() {
        Long storeId = getStoreIdFromToken();
        List<SeatStatusResponse> seats = seatService.findSeatStatusByStoreId(storeId);
        return CommonResponse.success(seats);
    }

    @Operation(summary = "좌석 입실",
        description = "QR/RFID로 좌석에 입실한다. 이미 사용 중인 좌석이면 거부된다. "
            + "qrUuid와 rfidUid 중 하나만 전송해야 하며, 나머지는 null이어야 한다.")
    @PostMapping("/{seatId}/check-in")
    public CommonResponse<Void> seatCheckIn(
        @PathVariable Long seatId,
        @Valid @RequestBody SeatCheckInRequest request) {
        Long storeId = getStoreIdFromToken();
        seatService.seatCheckIn(seatId, request, storeId);
        return CommonResponse.success(null);
    }

    @Operation(summary = "좌석 퇴실",
        description = "좌석에서 퇴실한다. 사용 중이 아닌 좌석이면 거부된다.")
    @PostMapping("/{seatId}/check-out")
    public CommonResponse<Void> seatCheckOut(@PathVariable Long seatId) {
        seatService.seatCheckOut(seatId);
        return CommonResponse.success(null);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
