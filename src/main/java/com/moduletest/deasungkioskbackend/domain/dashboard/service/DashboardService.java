package com.moduletest.deasungkioskbackend.domain.dashboard.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.AttendanceSummary;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.DailyOperationSummary;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.NoticeSummaryRecord;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.SeatChangeRequestRecord;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.SeatLeaveSummary;
import com.moduletest.deasungkioskbackend.domain.notice.repository.NoticeRepository;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequest;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity.SeatChangeRequestStatus;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.repository.SeatChangeRequestRepository;
import com.moduletest.deasungkioskbackend.domain.seatleave.repository.SeatLeaveRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int DASHBOARD_LIST_SIZE = 3;

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;
    private final OutingRepository outingRepository;
    private final SeatLeaveRepository seatLeaveRepository;
    private final NoticeRepository noticeRepository;
    private final SeatChangeRequestRepository seatChangeRequestRepository;

    public DashboardAllResponse getAll(Long storeId) {
        Store store = findStore(storeId);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        Long todayAttendance = parseTotalInwon(
            callDsaSafe("/kiosk/getTotalAttendCount", store));

        return DashboardAllResponse.builder()
            .dailyOperation(buildDailyOperation(storeId, todayAttendance))
            .mealTags(null)
            .attendanceSummary(buildAttendanceSummary(todayAttendance, storeId, startOfDay))
            .seatLeaveSummary(buildSeatLeaveSummary(storeId, startOfDay))
            .studyRanking(callDsaSafe("/kiosk/getLastWeekStudyTimeList", store))
            .pendingApprovals(null) // TODO: DSA 3.16 출결 사유신청 대기 연동 후 구현
            .seatChangeRequests(buildSeatChangeRequests(storeId))
            .notices(buildNotices(storeId))
            .build();
    }

    private DailyOperationSummary buildDailyOperation(Long storeId, Long todayAttendance) {
        long registeredStudents = studentRepository.countByStoreId(storeId);

        return DailyOperationSummary.builder()
            .registeredStudents(registeredStudents)
            .todayAttendance(todayAttendance)
            .mealRequests(null)
            .build();
    }

    private AttendanceSummary buildAttendanceSummary(
            Long todayAttendance, Long storeId, LocalDateTime startOfDay) {
        long outing = outingRepository.countActiveByStoreIdToday(
            storeId, startOfDay, startOfDay.plusDays(1));

        // TODO: 조퇴/결석/지각 — DSA 개별 집계 API 없음, DSA 연동 확장 시 구현
        return AttendanceSummary.builder()
            .present(todayAttendance)
            .earlyLeave(null)
            .absent(null)
            .outing(outing)
            .late(null)
            .build();
    }

    private SeatLeaveSummary buildSeatLeaveSummary(Long storeId, LocalDateTime startOfDay) {
        long totalLeave = seatLeaveRepository.countTodayByStoreId(storeId, startOfDay);
        long waitingReturn = seatLeaveRepository.countActiveByStoreId(storeId, startOfDay);

        return SeatLeaveSummary.builder()
            .totalLeave(totalLeave)
            .waitingReturn(waitingReturn)
            .build();
    }

    private List<NoticeSummaryRecord> buildNotices(Long storeId) {
        return noticeRepository.findAllActiveByStoreIdWithStore(storeId)
            .stream()
            .limit(DASHBOARD_LIST_SIZE)
            .map(n -> NoticeSummaryRecord.builder()
                .id(n.getId())
                .title(n.getTitle())
                .createdAt(n.getCreatedAt())
                .build())
            .toList();
    }

    private List<SeatChangeRequestRecord> buildSeatChangeRequests(Long storeId) {
        return seatChangeRequestRepository
            .findAllByStoreIdAndStatusWithStudent(storeId, SeatChangeRequestStatus.PENDING)
            .stream()
            .map(this::toSeatChangeRecord)
            .toList();
    }

    private SeatChangeRequestRecord toSeatChangeRecord(SeatChangeRequest r) {
        return SeatChangeRequestRecord.builder()
            .id(r.getId())
            .studentName(r.getStudent().getName())
            .currentSeatLabel(r.getCurrentSeat() != null
                ? r.getCurrentSeat().getSeatLabel() : null)
            .desiredSeat1Label(r.getDesiredSeat1().getSeatLabel())
            .desiredSeat2Label(r.getDesiredSeat2() != null
                ? r.getDesiredSeat2().getSeatLabel() : null)
            .desiredSeat3Label(r.getDesiredSeat3() != null
                ? r.getDesiredSeat3().getSeatLabel() : null)
            .requestedAt(r.getCreatedAt().toLocalDate())
            .build();
    }

    private DsaResponse callDsaSafe(String path, Store store) {
        try {
            return dsaApiClient.post(path, buildParams(), DsaResponse.class, store);
        } catch (Exception e) {
            log.warn("DSA API 호출 실패 [{}]: {}", path, e.getMessage());
            return null;
        }
    }

    private Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    private Long parseTotalInwon(DsaResponse response) {
        if (response == null || response.getTotalInwon() == null) {
            return null;
        }
        try {
            return Long.parseLong(response.getTotalInwon());
        } catch (NumberFormatException e) {
            log.warn("DSA total_inwon 파싱 실패: {}", response.getTotalInwon());
            return null;
        }
    }

    private Map<String, Object> buildParams() {
        return new HashMap<>();
    }
}
