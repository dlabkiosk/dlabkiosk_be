package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSyncService {

    private final DsaStudentService dsaStudentService;
    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;

    @Transactional
    public List<StudentSyncResult> synchronizeAllStores() {
        List<Store> stores = storeRepository.findAllWithDsaCredentials();

        List<StudentSyncResult> results = new ArrayList<>();
        for (Store store : stores) {
            try {
                StudentSyncResult result = synchronizeStudentsByStore(store);
                results.add(result);
            } catch (Exception e) {
                log.error("학생 동기화 실패. storeId: {}, storeName: {}",
                    store.getId(), store.getStoreName(), e);
                results.add(new StudentSyncResult(
                    store.getId(), store.getStoreName(),
                    0, 0, 0, 0, 0, 0, 0, List.of(e.getMessage())));
            }
        }

        return results;
    }

    @Transactional
    public StudentSyncResult synchronizeByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.hasDsaCredentials()) {
            throw new BusinessException(ErrorCode.SYNC_DSA_CREDENTIALS_MISSING);
        }

        return synchronizeStudentsByStore(store);
    }

    @Transactional
    public StudentSyncResult synchronizeStudentsByStore(Store store) {

        try {
            seatService.synchronizeSeats(store);
        } catch (Exception e) {
            log.error("좌석 동기화 실패 - 학생 동기화는 계속 진행. storeId: {}",
                store.getId(), e);
        }

        List<DsaStudentData> dsaStudents = dsaStudentService.findAllStudents(store);
        if (dsaStudents.isEmpty()) {
            log.info("DSA 학생 데이터 없음. storeId: {}", store.getId());
            return new StudentSyncResult(
                store.getId(), store.getStoreName(),
                0, 0, 0, 0, 0, 0, 0, List.of());
        }

        List<Student> existingStudents = studentRepository.findAllByStoreId(store.getId());

        Map<String, Student> byRfidUid = existingStudents.stream()
            .filter(s -> s.getRfidUid() != null && !s.getRfidUid().isBlank())
            .collect(Collectors.toMap(Student::getRfidUid, Function.identity()));

        Map<String, Student> byStudentNumber = existingStudents.stream()
            .filter(s -> s.getStudentNumber() != null && !s.getStudentNumber().isBlank())
            .collect(Collectors.toMap(
                Student::getStudentNumber, Function.identity(), (a, b) -> a));

        Map<String, Long> stdNoCounts = dsaStudents.stream()
            .filter(s -> s.stdNo() != null && !s.stdNo().isBlank())
            .collect(Collectors.groupingBy(
                DsaStudentData::stdNo, Collectors.counting()));

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int deactivated = 0;
        int failed = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        Set<Long> syncedStudentIds = new HashSet<>();

        for (DsaStudentData dsaStudent : dsaStudents) {
            if (dsaStudent.rfidNo() == null || dsaStudent.rfidNo().isBlank()) {
                skipped++;
                log.info("RFID 미부여 학생 동기화 skip. name: {}, stdNo: {}, storeId: {}",
                    dsaStudent.stdNm(), dsaStudent.stdNo(), store.getId());
                continue;
            }
            try {
                Seat seat = resolveSeat(dsaStudent.seatCd(), store);
                String phone = dsaStudentService.findStudentPhone(
                    dsaStudent.rfidNo(), dsaStudent.stdNo(), store);
                Student matched = findMatchingStudent(
                    dsaStudent, byRfidUid, byStudentNumber, stdNoCounts);

                if (matched != null) {
                    syncedStudentIds.add(matched.getId());
                    // DSA 3.24 실패 시 기존 전화번호 유지
                    String resolvedPhone = phone != null ? phone : matched.getPhone();
                    if (hasChanges(matched, dsaStudent, seat, resolvedPhone)) {
                        matched.syncFromDsa(
                            dsaStudent.stdNm(),
                            dsaStudent.rfidNo(),
                            dsaStudent.stdNo(),
                            dsaStudent.hp(),
                            resolvedPhone,
                            seat);
                        updated++;
                    } else {
                        unchanged++;
                    }
                } else {
                    if (studentRepository.existsByRfidUidAndStoreIdNot(
                        dsaStudent.rfidNo(), store.getId())) {
                        skipped++;
                        log.warn("RFID 중복으로 skip - 다른 지점에 동일 RFID 존재."
                                + " rfid: {}, name: {}, stdNo: {}, storeId: {}",
                            dsaStudent.rfidNo(), dsaStudent.stdNm(),
                            dsaStudent.stdNo(), store.getId());
                        continue;
                    }
                    Student newStudent = Student.builder()
                        .store(store)
                        .name(dsaStudent.stdNm())
                        .rfidUid(dsaStudent.rfidNo())
                        .studentNumber(dsaStudent.stdNo())
                        .phoneLast4(dsaStudent.hp())
                        .phone(phone)
                        .assignedSeat(seat)
                        .dsaSynced(true)
                        .build();
                    studentRepository.saveAndFlush(newStudent);

                    syncedStudentIds.add(newStudent.getId());
                    byRfidUid.put(newStudent.getRfidUid(), newStudent);
                    created++;
                }
            } catch (DataAccessException e) {
                // Why: DB 에러 발생 시 트랜잭션이 rollback-only 상태가 되므로
                // 루프를 계속하면 후속 JPQL의 autoflush가 좀비 엔티티로 터짐.
                // 안전하게 sync 전체 중단.
                String errorMsg = String.format("학생 동기화 DB 오류로 중단 [%s / %s]: %s",
                    dsaStudent.stdNm(), dsaStudent.rfidNo(), e.getMessage());
                log.error(errorMsg, e);
                throw e;
            } catch (Exception e) {
                failed++;
                String errorMsg = String.format("학생 동기화 실패 [%s / %s]: %s",
                    dsaStudent.stdNm(), dsaStudent.rfidNo(), e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg, e);
            }
        }

        for (Student existing : existingStudents) {
            if (existing.isActive() && !syncedStudentIds.contains(existing.getId())) {
                existing.deactivate();
                deactivated++;
                log.info("DSA 목록에 없는 학생 비활성화. studentId: {}, name: {}, storeId: {}",
                    existing.getId(), existing.getName(), store.getId());
            }
        }

        return new StudentSyncResult(
            store.getId(), store.getStoreName(),
            dsaStudents.size(), created, updated, unchanged, deactivated, failed, skipped,
            errors);
    }

    private Student findMatchingStudent(DsaStudentData dsaStudent,
        Map<String, Student> byRfidUid,
        Map<String, Student> byStudentNumber,
        Map<String, Long> stdNoCounts) {
        if (dsaStudent.rfidNo() != null) {
            Student matched = byRfidUid.get(dsaStudent.rfidNo());
            if (matched != null) {
                return matched;
            }
        }
        // Why: rfid 매칭 실패 시 학번 폴백.
        // 단, DSA 응답에 같은 학번이 2건 이상이면 의도된 중복으로 보고
        // 폴백을 건너뛰어 별개 row로 INSERT (DSA 데이터 정합성 보존).
        // 1건일 때만 rfid 재발급으로 간주하고 기존 row 재사용.
        if (dsaStudent.stdNo() != null && !dsaStudent.stdNo().isBlank()
                && stdNoCounts.getOrDefault(dsaStudent.stdNo(), 0L) == 1L) {
            return byStudentNumber.get(dsaStudent.stdNo());
        }
        return null;
    }

    private boolean hasChanges(Student student, DsaStudentData dsaStudent,
        Seat seat, String phone) {
        if (!student.getName().equals(dsaStudent.stdNm())) {
            return true;
        }
        if (!equalsNullable(student.getRfidUid(), dsaStudent.rfidNo())) {
            return true;
        }
        if (!equalsNullable(student.getStudentNumber(), dsaStudent.stdNo())) {
            return true;
        }
        if (!equalsNullable(student.getPhoneLast4(), dsaStudent.hp())) {
            return true;
        }
        if (!equalsNullable(student.getPhone(), phone)) {
            return true;
        }
        Long currentSeatId = student.getAssignedSeat() != null
            ? student.getAssignedSeat().getId() : null;
        Long newSeatId = seat != null ? seat.getId() : null;
        return !equalsNullable(currentSeatId, newSeatId);
    }

    private boolean equalsNullable(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private Seat resolveSeat(String seatCd, Store store) {
        if (seatCd == null || seatCd.isBlank()) {
            return null;
        }

        Optional<Seat> seat = seatRepository.findBySeatCdAndStoreId(seatCd, store.getId());

        return seat.orElse(null);
    }
}
