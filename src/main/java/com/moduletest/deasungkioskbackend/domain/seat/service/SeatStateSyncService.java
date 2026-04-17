package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.Attendance;
import com.moduletest.deasungkioskbackend.domain.attendance.entity.AttendanceStatus;
import com.moduletest.deasungkioskbackend.domain.attendance.repository.AttendanceRepository;
import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStateSyncResult;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStateSyncService {

    private static final String DSA_STATE_CHECKED_IN = "S";
    private static final String DSA_STATE_OUTING = "D";

    private final DsaStudentService dsaStudentService;
    private final DsaAreaService dsaAreaService;
    private final StoreRepository storeRepository;
    private final StudentRepository studentRepository;
    private final SeatRepository seatRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final AttendanceRepository attendanceRepository;
    private final OutingRepository outingRepository;
    private final SeatService seatService;
    private final SeatRedisService seatRedisService;

    @Transactional
    public SeatStateSyncResult synchronizeByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.hasDsaCredentials()) {
            throw new BusinessException(ErrorCode.SYNC_DSA_CREDENTIALS_MISSING);
        }

        try {
            seatService.synchronizeSeats(store);
        } catch (Exception e) {
            log.error("좌석 동기화 실패 - 상태 동기화는 계속 진행. storeId: {}", store.getId(), e);
        }

        Map<String, String> seatStateByCd = buildSeatStateMap(store);
        List<DsaStudentData> dsaStudents = dsaStudentService.findAllStudents(store);

        Map<String, Student> studentsByRfid = studentRepository.findAllByStoreId(store.getId()).stream()
            .filter(s -> s.getRfidUid() != null && !s.getRfidUid().isBlank())
            .collect(Collectors.toMap(Student::getRfidUid, Function.identity(), (a, b) -> a));

        int inUseApplied = 0;
        int outingApplied = 0;
        int skippedNoRfid = 0;
        int skippedNoSeat = 0;
        int skippedStudentNotFound = 0;
        int skippedStoreMismatch = 0;
        int skippedNonActiveState = 0;
        int alreadyInUse = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        for (DsaStudentData dsaStudent : dsaStudents) {
            try {
                SyncDecision decision = decide(
                    dsaStudent, store, seatStateByCd, studentsByRfid);
                if (decision.isSkip()) {
                    switch (decision.skipReason()) {
                        case NO_RFID -> skippedNoRfid++;
                        case NO_SEAT -> skippedNoSeat++;
                        case NON_ACTIVE_STATE -> skippedNonActiveState++;
                        case STUDENT_NOT_FOUND -> skippedStudentNotFound++;
                        case STORE_MISMATCH -> skippedStoreMismatch++;
                    }
                    continue;
                }

                Student student = decision.student();
                Seat seat = decision.seat();
                String state = decision.state();

                Optional<SeatUsage> existingUsage = seatUsageRepository.findByStudentIdAndStatus(
                    student.getId(), SeatUsageStatus.IN_USE);

                if (existingUsage.isEmpty()) {
                    SeatUsage usage = SeatUsage.builder()
                        .seat(seat)
                        .student(student)
                        .store(store)
                        .startedAt(now)
                        .build();
                    seatUsageRepository.save(usage);
                } else {
                    alreadyInUse++;
                }

                Optional<Attendance> existingAttendance = attendanceRepository
                    .findTodayAttendanceByStudentAndStatus(
                        student.getId(), startOfDay, endOfDay, AttendanceStatus.CHECKED_IN);
                if (existingAttendance.isEmpty()) {
                    Attendance attendance = Attendance.builder()
                        .student(student)
                        .store(store)
                        .checkInAt(now)
                        .build();
                    attendanceRepository.save(attendance);
                }

                if (DSA_STATE_CHECKED_IN.equals(state)) {
                    seatRedisService.markSeatInUse(
                        store.getId(), seat.getId(), student.getId(), student.getName());
                    inUseApplied++;
                } else {
                    Optional<Outing> existingOuting = outingRepository
                        .findActiveOutingByStudentToday(
                            student.getId(), startOfDay, endOfDay);
                    if (existingOuting.isEmpty()) {
                        Outing outing = Outing.builder()
                            .student(student)
                            .store(store)
                            .startedAt(now)
                            .build();
                        outingRepository.save(outing);
                    }

                    seatRedisService.markSeatOuting(
                        store.getId(), seat.getId(), student.getId(), student.getName());
                    outingApplied++;
                }
            } catch (Exception e) {
                failed++;
                String msg = String.format("좌석 상태 동기화 실패 [%s / %s]: %s",
                    dsaStudent.stdNm(), dsaStudent.rfidNo(), e.getMessage());
                errors.add(msg);
                log.warn(msg, e);
            }
        }

        log.info("좌석 상태 동기화 완료. storeId: {}, inUse: {}, outing: {}, alreadyInUse: {}, "
                + "skippedNoRfid: {}, skippedNoSeat: {}, skippedStudentNotFound: {}, "
                + "skippedStoreMismatch: {}, skippedNonActiveState: {}, failed: {}",
            store.getId(), inUseApplied, outingApplied, alreadyInUse,
            skippedNoRfid, skippedNoSeat, skippedStudentNotFound,
            skippedStoreMismatch, skippedNonActiveState, failed);

        return new SeatStateSyncResult(
            store.getId(), store.getStoreName(),
            dsaStudents.size(), inUseApplied, outingApplied,
            skippedNoRfid, skippedNoSeat, skippedStudentNotFound,
            skippedStoreMismatch, skippedNonActiveState, alreadyInUse,
            failed, errors);
    }

    private Map<String, String> buildSeatStateMap(Store store) {
        Map<String, String> result = new HashMap<>();
        List<AreaResponse> areas = dsaAreaService.findAreas(store);
        for (AreaResponse area : areas) {
            List<SeatStatusResponse> seats = dsaAreaService.findSeatStatusByArea(
                area.areaCd(), store);
            for (SeatStatusResponse seat : seats) {
                if (seat.seatCd() != null) {
                    result.put(seat.seatCd(), seat.state());
                }
            }
        }
        return result;
    }

    private SyncDecision decide(DsaStudentData dsa, Store store,
        Map<String, String> seatStateByCd, Map<String, Student> studentsByRfid) {
        if (dsa.rfidNo() == null || dsa.rfidNo().isBlank()) {
            return SyncDecision.skip(SkipReason.NO_RFID);
        }
        if (dsa.seatCd() == null || dsa.seatCd().isBlank()) {
            return SyncDecision.skip(SkipReason.NO_SEAT);
        }

        String state = seatStateByCd.get(dsa.seatCd());
        if (!DSA_STATE_CHECKED_IN.equals(state) && !DSA_STATE_OUTING.equals(state)) {
            return SyncDecision.skip(SkipReason.NON_ACTIVE_STATE);
        }

        Student student = studentsByRfid.get(dsa.rfidNo());
        if (student == null) {
            return SyncDecision.skip(SkipReason.STUDENT_NOT_FOUND);
        }
        if (student.getStore() == null
                || !student.getStore().getId().equals(store.getId())) {
            return SyncDecision.skip(SkipReason.STORE_MISMATCH);
        }

        Optional<Seat> seatOpt = seatRepository.findBySeatCdAndStoreId(
            dsa.seatCd(), store.getId());
        if (seatOpt.isEmpty()) {
            return SyncDecision.skip(SkipReason.NO_SEAT);
        }

        return SyncDecision.ok(student, seatOpt.get(), state);
    }

    private enum SkipReason {
        NO_RFID, NO_SEAT, NON_ACTIVE_STATE, STUDENT_NOT_FOUND, STORE_MISMATCH
    }

    private record SyncDecision(
        SkipReason skipReason,
        Student student,
        Seat seat,
        String state) {

        static SyncDecision skip(SkipReason reason) {
            return new SyncDecision(reason, null, null, null);
        }

        static SyncDecision ok(Student student, Seat seat, String state) {
            return new SyncDecision(null, student, seat, state);
        }

        boolean isSkip() {
            return skipReason != null;
        }
    }
}
