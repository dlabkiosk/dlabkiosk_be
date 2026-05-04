package com.moduletest.deasungkioskbackend.domain.seatchangerequest.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.dto.PageResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatWaitingStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequestStatus;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.service.SeatChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 좌석 변경 신청 관리", description = "좌석 변경 신청 조회/승인/거절 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/seat-change-requests")
@RequiredArgsConstructor
public class SeatChangeRequestAdminController {

    private final SeatChangeRequestService seatChangeRequestService;

    @Operation(summary = "좌석 변경 신청 목록 조회",
        description = "MANAGER: 자기 지점만 (storeId 무시). ADMIN: storeId 없으면 전체, 있으면 해당 지점만. "
            + "status 생략 시 전체 상태 조회 (PENDING/APPROVED/REJECTED 합산). "
            + "studentName/studentNumber는 부분일치 검색. "
            + "페이지네이션: ?page=0&size=20&sort=createdAt,desc")
    @GetMapping
    public CommonResponse<PageResponse<SeatChangeRequestResponse>> findAllRequests(
            @RequestParam(required = false) SeatChangeRequestStatus status,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNumber,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        Page<SeatChangeRequestResponse> page = seatChangeRequestService
            .findAllRequests(resolvedStoreId, status, studentName, studentNumber, pageable);
        return CommonResponse.success(PageResponse.from(page));
    }

    @Operation(summary = "좌석별 대기 현황 조회",
        description = "전체 활성 좌석별로 현재 배정자와 PENDING 대기자 목록을 반환한다. "
            + "대기자는 신청 시간순으로 정렬되며, 해당 좌석에 대한 희망 순위(1~3)를 포함한다. "
            + "ADMIN은 storeId 필수, MANAGER는 자기 지점 자동 적용.")
    @GetMapping("/seat-status")
    public CommonResponse<List<SeatWaitingStatusResponse>> findSeatWaitingStatus(
            @RequestParam(required = false) Long storeId) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        List<SeatWaitingStatusResponse> responses =
            seatChangeRequestService.findSeatWaitingStatus(resolvedStoreId);
        return CommonResponse.success(responses);
    }

    @Operation(summary = "좌석별 대기 인원 조회",
        description = "특정 좌석의 현재 배정자와 PENDING 대기자 목록을 반환한다.")
    @GetMapping("/seat-status/{seatId}")
    public CommonResponse<SeatWaitingStatusResponse> findSeatWaitingStatusBySeatId(
            @PathVariable Long seatId) {
        SeatWaitingStatusResponse response =
            seatChangeRequestService.findSeatWaitingStatusBySeatId(seatId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "좌석 변경 신청 단건 조회", description = "신청 ID로 상세 정보를 조회한다.")
    @GetMapping("/{requestId}")
    public CommonResponse<SeatChangeRequestResponse> findRequestById(
            @PathVariable Long requestId) {
        SeatChangeRequestResponse response =
            seatChangeRequestService.findRequestById(requestId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "좌석 변경 승인",
        description = "seatCd(DSA 좌석 코드)로 배정할 좌석을 지정한다. "
            + "해당 좌석이 학생의 신청 순위(1~3)에 포함되어 있어야 한다. "
            + "승인된 순위보다 높은 순위가 남아있으면 PENDING 유지, 아니면 APPROVED 완료. "
            + "입실 중이면 Redis 좌석 상태도 이동된다.")
    @PutMapping("/{requestId}/approve")
    public CommonResponse<SeatChangeRequestResponse> approveRequest(
            @PathVariable Long requestId,
            @RequestParam String seatCd) {
        SeatChangeRequestResponse response =
            seatChangeRequestService.approveRequest(requestId, seatCd);
        return CommonResponse.success(response);
    }

    @Operation(summary = "좌석 변경 거절", description = "신청을 거절한다.")
    @PutMapping("/{requestId}/reject")
    public CommonResponse<SeatChangeRequestResponse> rejectRequest(
            @PathVariable Long requestId) {
        SeatChangeRequestResponse response =
            seatChangeRequestService.rejectRequest(requestId);
        return CommonResponse.success(response);
    }
}
