package com.moduletest.deasungkioskbackend.domain.seat.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 좌석", description = "좌석 현황 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "좌석 현황 조회",
        description = "로그인한 지점의 전체 좌석 현황을 조회한다. "
            + "좌석 입실/퇴실은 등원/하원 시 자동 처리된다.")
    @GetMapping
    public CommonResponse<List<SeatStatusResponse>> findSeatStatus() {
        Long storeId = getStoreIdFromToken();
        List<SeatStatusResponse> seats = seatService.findSeatStatusByStoreId(storeId);
        return CommonResponse.success(seats);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
