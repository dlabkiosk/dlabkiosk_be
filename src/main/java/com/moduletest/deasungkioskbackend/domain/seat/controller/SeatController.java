package com.moduletest.deasungkioskbackend.domain.seat.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 좌석", description = "좌석 현황 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "구역별 좌석 현황 조회",
        description = "DSA에서 구역별 좌석 레이아웃과 상태를 조회한다. "
            + "areaCd 필수. 구역 목록은 /areas에서 먼저 조회.\n\n"
            + "**좌석 구분(seatGn)**: Y(사용 좌석), N(미사용 좌석), E(통로)\n\n"
            + "**좌석 상태(state)**: S(등원), D(외출), N(미출석), B(공석), A(좌석이탈)\n\n"
            + "[참고] 키오스크 프론트에서는 좌석 상태(state)를 노출하지 않음. 레이아웃 표시 용도로만 사용.")
    @GetMapping
    public CommonResponse<List<SeatStatusResponse>> findSeatStatus(
        @RequestParam String areaCd) {
        Long storeId = getStoreIdFromToken();
        List<SeatStatusResponse> seats = seatService.findSeatStatusByArea(storeId, areaCd);
        return CommonResponse.success(seats);
    }

    @Operation(summary = "구역(강의실) 목록 조회",
        description = "로그인한 지점의 구역 목록을 조회한다. 좌석 현황 조회 전 구역 선택에 사용.")
    @GetMapping("/areas")
    public CommonResponse<List<AreaResponse>> findAreas() {
        Long storeId = getStoreIdFromToken();
        List<AreaResponse> areas = seatService.findAreasByStoreId(storeId);
        return CommonResponse.success(areas);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
