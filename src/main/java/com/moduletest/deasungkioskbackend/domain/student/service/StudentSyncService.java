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
                    0, 0, 0, 0, 0, List.of(e.getMessage())));
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

        seatService.synchronizeSeats(store);

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

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (DsaStudentData dsaStudent : dsaStudents) {
            try {
                Seat seat = resolveSeat(dsaStudent.seatCd(), store);
                String phone = dsaStudentService.findStudentPhone(
                    dsaStudent.rfidNo(), store);
                Student matched = findMatchingStudent(dsaStudent, byRfidUid);

                if (matched != null) {
                    if (hasChanges(matched, dsaStudent, seat, phone)) {
                        matched.syncFromDsa(
                            dsaStudent.stdNm(),
                            dsaStudent.rfidNo(),
                            dsaStudent.stdNo(),
                            dsaStudent.hp(),
                            phone,
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
                        .phone(phone)
                        .assignedSeat(seat)
                        .dsaSynced(true)
                        .build();
                    studentRepository.save(newStudent);

                    byRfidUid.put(newStudent.getRfidUid(), newStudent);
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

        return new StudentSyncResult(
            store.getId(), store.getStoreName(),
            dsaStudents.size(), created, updated, unchanged, failed, errors);
    }

    private Student findMatchingStudent(DsaStudentData dsaStudent,
        Map<String, Student> byRfidUid) {
        if (dsaStudent.rfidNo() != null) {
            return byRfidUid.get(dsaStudent.rfidNo());
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
