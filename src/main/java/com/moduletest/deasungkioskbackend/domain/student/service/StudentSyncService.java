package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public final class StudentSyncService {

    private final DsaStudentService dsaStudentService;
    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;

    public List<StudentSyncResult> synchronizeAllStores() {
        List<Store> stores = storeRepository.findAllWithDsaCredentials();
        log.info("학생 동기화 시작: DSA 인증정보 있는 지점 {}개", stores.size());

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
                    0, 0, 0, 0, 0, List.of(e.getMessage())));
            }
        }

        log.info("학생 동기화 완료: 전체 {}개 지점", results.size());
        return results;
    }

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
        List<DsaStudentData> dsaStudents = dsaStudentService.findAllStudents(store);
        if (dsaStudents.isEmpty()) {
            log.info("DSA 학생 데이터 없음. storeId: {}", store.getId());
            return new StudentSyncResult(
                store.getId(), store.getStoreName(),
                0, 0, 0, 0, 0, List.of());
        }

        List<Student> existingStudents = studentRepository.findAllByStoreId(store.getId());

        Map<String, Student> byRfidUid = existingStudents.stream()
            .filter(s -> s.getRfidUid() != null)
            .collect(Collectors.toMap(Student::getRfidUid, Function.identity()));

        Map<String, Student> byStudentNumber = existingStudents.stream()
            .filter(s -> s.getStudentNumber() != null)
            .collect(Collectors.toMap(Student::getStudentNumber, Function.identity(),
                (existing, duplicate) -> existing));

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (DsaStudentData dsaStudent : dsaStudents) {
            try {
                Seat seat = resolveSeat(dsaStudent.seatCd(), store);
                Student matched = findMatchingStudent(dsaStudent, byRfidUid, byStudentNumber);

                if (matched != null) {
                    if (hasChanges(matched, dsaStudent, seat)) {
                        matched.syncFromDsa(
                            dsaStudent.stdNm(),
                            dsaStudent.rfidNo(),
                            dsaStudent.stdNo(),
                            dsaStudent.hp(),
                            seat);
                        updated++;
                    } else {
                        unchanged++;
                    }
                } else {
                    Student newStudent = Student.builder()
                        .store(store)
                        .name(dsaStudent.stdNm())
                        .rfidUid(dsaStudent.rfidNo())
                        .studentNumber(dsaStudent.stdNo())
                        .phoneLast4(dsaStudent.hp())
                        .assignedSeat(seat)
                        .dsaSynced(true)
                        .build();
                    studentRepository.save(newStudent);

                    byRfidUid.put(newStudent.getRfidUid(), newStudent);
                    if (newStudent.getStudentNumber() != null) {
                        byStudentNumber.put(newStudent.getStudentNumber(), newStudent);
                    }
                    created++;
                }
            } catch (Exception e) {
                failed++;
                String errorMsg = String.format("학생 동기화 실패 [%s / %s]: %s",
                    dsaStudent.stdNm(), dsaStudent.rfidNo(), e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg, e);
            }
        }

        log.info("학생 동기화 결과 - storeId: {}, 전체: {}, 생성: {}, 수정: {}, 변경없음: {}, 실패: {}",
            store.getId(), dsaStudents.size(), created, updated, unchanged, failed);

        return new StudentSyncResult(
            store.getId(), store.getStoreName(),
            dsaStudents.size(), created, updated, unchanged, failed, errors);
    }

    private Student findMatchingStudent(DsaStudentData dsaStudent,
                                        Map<String, Student> byRfidUid,
                                        Map<String, Student> byStudentNumber) {
        if (dsaStudent.rfidNo() != null) {
            Student matched = byRfidUid.get(dsaStudent.rfidNo());
            if (matched != null) {
                return matched;
            }
        }

        if (dsaStudent.stdNo() != null) {
            return byStudentNumber.get(dsaStudent.stdNo());
        }

        return null;
    }

    private boolean hasChanges(Student student, DsaStudentData dsaStudent, Seat seat) {
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
        return seatRepository.findBySeatCdAndStoreId(seatCd, store.getId())
            .orElse(null);
    }
}
