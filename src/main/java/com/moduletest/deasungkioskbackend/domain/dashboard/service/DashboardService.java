package com.moduletest.deasungkioskbackend.domain.dashboard.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.AttendanceSummary;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.DailyOperationSummary;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.NoticeSummaryRecord;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.SeatChangeRequestRecord;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.MealTagDetail;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.MealTagSummary;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse.SeatLeaveSummary;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealTagRepository;
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

    private static final int DASHBOARD_LIST_SIZE = 5;

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;
    private final OutingRepository outingRepository;
    private final SeatLeaveRepository seatLeaveRepository;
    private final NoticeRepository noticeRepository;
    private final SeatChangeRequestRepository seatChangeRequestRepository;
    private final MealTagRepository mealTagRepository;

    public DashboardAllResponse getAll(Long storeId) {
        Store store = findStore(storeId);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        Long todayAttendance = parseTotalInwon(
            callDsaSafe("/kiosk/getTotalAttendCount", store));

        return DashboardAllResponse.builder()
            .dailyOperation(buildDailyOperation(storeId, todayAttendance, store))
            .mealTagSummary(buildMealTagSummary(storeId))
            .attendanceSummary(buildAttendanceSummary(todayAttendance, storeId, startOfDay, store))
            .seatLeaveSummary(buildSeatLeaveSummary(storeId, startOfDay))
            .studyRanking(callDsaSafe("/kiosk/getLastWeekStudyTimeList", store))
            .pendingApprovals(null) // TODO: DSA 3.16 출결 사유신청 대기 연동 후 구현
            .seatChangeRequests(buildSeatChangeRequests(storeId))
            .notices(buildNotices(storeId))
            .build();
    }

    private DailyOperationSummary buildDailyOperation(Long storeId, Long todayAttendance,
                                                       Store store) {
        long registeredStudents = studentRepository.countByStoreId(storeId);
        Long mealRequests = countTodayMealRequests(store);

        return DailyOperationSummary.builder()
            .registeredStudents(registeredStudents)
            .todayAttendance(todayAttendance)
            .mealRequests(mealRequests)
            .build();
    }

    private Long countTodayMealRequests(Store store) {
        try {
            LocalDate today = LocalDate.now();
            String month = today.toString().substring(0, 7);
            String todayDay = String.valueOf(today.getDayOfMonth());

            Map<String, Object> params = new HashMap<>();
            params.put("month", month);

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getMealApplyStdInfo", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                return null;
            }

            long count = response.getDataAsList().stream()
                .filter(item -> todayDay.equals(String.valueOf(item.get("day"))))
                .count();

            return count;
        } catch (Exception e) {
            log.warn("DSA getMealApplyStdInfo 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    private AttendanceSummary buildAttendanceSummary(
            Long todayAttendance, Long storeId, LocalDateTime startOfDay, Store store) {
        long outing = outingRepository.countActiveByStoreIdToday(
            storeId, startOfDay, startOfDay.plusDays(1));

        Long earlyLeave = null;
        Long absent = null;
        Long late = null;

        DsaResponse attendState = callDsaAttendState(store);
        if (attendState != null) {
            earlyLeave = parseLongSafe(attendState.getExtra().get("early_cnt"));
            absent = parseLongSafe(attendState.getExtra().get("absence_cnt"));
            late = parseLongSafe(attendState.getExtra().get("late_cnt"));
        }

        return AttendanceSummary.builder()
            .present(todayAttendance)
            .earlyLeave(earlyLeave)
            .absent(absent)
            .outing(outing)
            .late(late)
            .build();
    }

    private DsaResponse callDsaAttendState(Store store) {
        try {
            String today = LocalDate.now().toString();
            Map<String, Object> params = new HashMap<>();
            params.put("st_dt", today);
            params.put("ed_dt", today);

            return dsaApiClient.post(
                "/kiosk/getAttendState", params, DsaResponse.class, store);
        } catch (Exception e) {
            log.warn("DSA getAttendState 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    private Long parseLongSafe(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            log.warn("숫자 파싱 실패: {}", value);
            return null;
        }
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
                .createdBy(n.getCreatedBy())
                .createdAt(n.getCreatedAt())
                .build())
            .toList();
    }

    private MealTagSummary buildMealTagSummary(Long storeId) {
        LocalDate today = LocalDate.now();
        long lunchCount = mealTagRepository.countByStoreIdAndMealDateAndMealType(
            storeId, today, MealType.LUNCH);
        long dinnerCount = mealTagRepository.countByStoreIdAndMealDateAndMealType(
            storeId, today, MealType.DINNER);

        List<MealTagDetail> details = mealTagRepository
            .findAllByStoreIdAndMealDateWithStudent(storeId, today)
            .stream()
            .limit(DASHBOARD_LIST_SIZE)
            .map(mt -> MealTagDetail.builder()
                .studentName(mt.getStudent().getName())
                .mealType(mt.getMealType())
                .taggedAt(mt.getTaggedAt())
                .build())
            .toList();

        return MealTagSummary.builder()
            .lunchCount(lunchCount)
            .dinnerCount(dinnerCount)
            .details(details)
            .build();
    }

    private List<SeatChangeRequestRecord> buildSeatChangeRequests(Long storeId) {
        return seatChangeRequestRepository
            .findAllByStoreIdAndStatusWithStudent(storeId, SeatChangeRequestStatus.PENDING)
            .stream()
            .limit(DASHBOARD_LIST_SIZE)
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
            .requestedAt(r.getCreatedAt())
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
