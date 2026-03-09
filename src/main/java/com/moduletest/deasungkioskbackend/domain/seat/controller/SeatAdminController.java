package com.moduletest.deasungkioskbackend.domain.seat.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 좌석 관리", description = "좌석 CRUD 및 배치 관리 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seats")
@RequiredArgsConstructor
public class SeatAdminController {

    private final SeatService seatService;

    @Operation(summary = "좌석 목록 조회",
        description = "MANAGER: 자기 지점 좌석만 조회. ADMIN: 전체 좌석 조회.")
    @GetMapping
    public CommonResponse<List<SeatResponse>> findAllSeats() {
        Long storeId = SecurityUtil.resolveStoreId(null);
        List<SeatResponse> seats = seatService.findAllSeats(storeId);
        return CommonResponse.success(seats);
    }

    @Operation(summary = "좌석 단건 조회", description = "좌석 ID로 단건 조회한다.")
    @GetMapping("/{seatId}")
    public CommonResponse<SeatResponse> findSeatById(@PathVariable Long seatId) {
        SeatResponse seat = seatService.findSeatById(seatId);
        return CommonResponse.success(seat);
    }

    @Operation(summary = "좌석 등록",
        description = "MANAGER: 자기 지점에 자동 등록. "
            + "ADMIN: storeId 필수 — 등록할 지점 지정. "
            + "좌표(x_pos, y_pos)는 캔버스 픽셀 단위.")
    @PostMapping
    public CommonResponse<SeatResponse> createSeat(
        @RequestParam(required = false) Long storeId,
        @Valid @RequestBody SeatCreateRequest request) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        SeatResponse seat = seatService.createSeat(request, resolvedStoreId);
        return CommonResponse.success(seat);
    }

    @Operation(summary = "좌석 수정", description = "좌석 정보(라벨, 좌표, 활성 여부 등)를 수정한다.")
    @PutMapping("/{seatId}")
    public CommonResponse<SeatResponse> updateSeat(
        @PathVariable Long seatId,
        @Valid @RequestBody SeatUpdateRequest request) {
        SeatResponse seat = seatService.updateSeat(seatId, request);
        return CommonResponse.success(seat);
    }

    @Operation(summary = "좌석 삭제", description = "좌석을 삭제한다.")
    @DeleteMapping("/{seatId}")
    public CommonResponse<Void> deleteSeat(@PathVariable Long seatId) {
        seatService.deleteSeat(seatId);
        return CommonResponse.success(null);
    }
}
