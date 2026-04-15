package com.moduletest.deasungkioskbackend.domain.seat.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaAreaService;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
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
import java.time.LocalDateTime;
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

        for (DsaStudentData dsaStudent : dsaStudents) {
            try {
                if (dsaStudent.rfidNo() == null || dsaStudent.rfidNo().isBlank()) {
                    skippedNoRfid++;
                    continue;
                }
                if (dsaStudent.seatCd() == null || dsaStudent.seatCd().isBlank()) {
                    skippedNoSeat++;
                    continue;
                }

                String state = seatStateByCd.get(dsaStudent.seatCd());
                if (!DSA_STATE_CHECKED_IN.equals(state) && !DSA_STATE_OUTING.equals(state)) {
                    skippedNonActiveState++;
                    continue;
                }

                Student student = studentsByRfid.get(dsaStudent.rfidNo());
                if (student == null) {
                    skippedStudentNotFound++;
                    continue;
                }
                if (student.getStore() == null
                    || !student.getStore().getId().equals(store.getId())) {
                    skippedStoreMismatch++;
                    continue;
                }

                Optional<Seat> seatOpt = seatRepository.findBySeatCdAndStoreId(
                    dsaStudent.seatCd(), store.getId());
                if (seatOpt.isEmpty()) {
                    skippedNoSeat++;
                    continue;
                }
                Seat seat = seatOpt.get();

                Optional<SeatUsage> existing = seatUsageRepository.findByStudentIdAndStatus(
                    student.getId(), SeatUsageStatus.IN_USE);

                if (existing.isEmpty()) {
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

                if (DSA_STATE_CHECKED_IN.equals(state)) {
                    seatRedisService.markSeatInUse(
                        store.getId(), seat.getId(), student.getId(), student.getName());
                    inUseApplied++;
                } else {
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
}
