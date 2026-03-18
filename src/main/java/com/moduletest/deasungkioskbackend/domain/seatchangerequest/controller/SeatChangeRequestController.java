package com.moduletest.deasungkioskbackend.domain.seatchangerequest.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.service.StudentResolverService;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.AvailableSeatResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestCreateRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.service.SeatChangeRequestService;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 좌석 변경 신청", description = "좌석 변경/대기 신청 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/seat-change-requests")
@RequiredArgsConstructor
public class SeatChangeRequestController {

    private final SeatChangeRequestService seatChangeRequestService;
    private final StudentResolverService studentResolverService;

    @Operation(summary = "좌석 변경 신청/수정",
        description = "학생 식별(identifier/학번/전화번호) + 희망 좌석 1~3순위로 좌석 변경을 신청한다. "
            + "1순위 필수, 2~3순위 선택. 이미 대기 중인 신청이 있으면 희망 순위를 수정한다.")
    @PostMapping
    public CommonResponse<SeatChangeRequestResponse> createRequest(
            @Valid @RequestBody SeatChangeRequestCreateRequest request) {
        Long storeId = getStoreIdFromToken();
        SeatChangeRequestResponse response =
            seatChangeRequestService.createRequest(request, storeId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "내 좌석 변경 신청 조회",
        description = "현재 대기(PENDING) 중인 내 좌석 변경 신청을 조회한다. "
            + "신청 내역이 없으면 data=null을 반환한다.")
    @GetMapping("/my")
    public CommonResponse<SeatChangeRequestResponse> findMyPendingRequest(
            @RequestParam String identifier) {
        Long storeId = getStoreIdFromToken();
        SeatChangeRequestResponse response =
            seatChangeRequestService.findMyPendingRequest(identifier, storeId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "신청 가능한 좌석 목록",
        description = "현재 지점의 전체 활성 좌석 목록을 조회한다. "
            + "각 좌석의 현재 배정자 이름과 빈 좌석 여부를 포함한다.")
    @GetMapping("/available-seats")
    public CommonResponse<List<AvailableSeatResponse>> findAvailableSeats() {
        Long storeId = getStoreIdFromToken();
        List<AvailableSeatResponse> seats =
            seatChangeRequestService.findAvailableSeats(storeId);
        return CommonResponse.success(seats);
    }

    @Operation(summary = "좌석 변경 신청 취소",
        description = "PENDING 상태의 신청만 취소 가능하다. "
            + "카드/QR/좌석번호/폰뒷자리 중 하나 필수.")
    @DeleteMapping("/{requestId}")
    public CommonResponse<Void> cancelRequest(
            @PathVariable Long requestId,
            @RequestParam String identifier) {
        Long storeId = getStoreIdFromToken();
        Student student = studentResolverService.resolveAuto(identifier, storeId);
        seatChangeRequestService.cancelRequest(requestId, student.getId());
        return CommonResponse.success(null);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
