package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.util.PhoneNormalizer;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학생 동기화의 트랜잭션 단위 작업.
 * 학생 1명 upsert와 비활성화 처리를 짧은 별도 트랜잭션으로 수행한다.
 * StudentSyncService가 외부에서 호출해야 Spring AOP 프록시가 적용된다 (자기 호출 회피).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSyncTxService {

    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final SeatRepository seatRepository;

    public enum UpsertStatus { CREATED, UPDATED, UNCHANGED, SKIPPED }

    public record UpsertOutcome(Long studentId, UpsertStatus status) {
    }

    @Transactional
    public UpsertOutcome upsertOneStudent(Long storeId, DsaStudentData dsaStudent,
        String phone, Map<String, Long> stdNoCounts) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Seat seat = resolveSeat(dsaStudent.seatCd(), storeId);
        Student matched = findMatchingStudent(dsaStudent, storeId, stdNoCounts);

        if (matched != null) {
            String oldRfid = matched.getRfidUid();
            boolean rfidChanging = !equalsNullable(oldRfid, dsaStudent.rfidNo());
            // Why: rfid 동일 + 학번 다름 → 카드 양도/회수 누락된 다른 학생.
            // 기존 row를 그대로 덮어쓰면 X의 출결/이탈/급식 이력이 Y에게 잘못 귀속됨.
            // X를 비활성화해 이력을 분리하고, Y는 아래 INSERT 경로로 신규 생성.
            // deactivate()가 rfid=null로 만들어 unique 충돌도 동시 해소.
            if (!rfidChanging && isDifferentStudentByStdNo(matched, dsaStudent)) {
                log.warn("RFID 동일 + 학번 다름 - 다른 학생으로 분리 처리."
                        + " 기존 studentId: {}, oldName: {}, oldStdNo: {},"
                        + " newName: {}, newStdNo: {}, rfid: {}, storeId: {}",
                    matched.getId(), matched.getName(), matched.getStudentNumber(),
                    dsaStudent.stdNm(), dsaStudent.stdNo(), oldRfid, storeId);
                matched.deactivate();
                studentRepository.flush();
                matched = null;
            } else {
                // Why: 학번 폴백으로 매칭된 경우, rfid가 바뀐다면 같은 지점 내
                // 다른 row가 이미 그 rfid를 점유 중일 수 있음. 사전 체크로
                // unique constraint 예외 차단.
                if (rfidChanging && studentRepository.existsByRfidUidAndStoreId(
                        dsaStudent.rfidNo(), storeId)) {
                    log.warn("RFID 변경 시 같은 지점 중복 - skip. stdNo: {}, name: {},"
                            + " oldRfid: {}, newRfid: {}, storeId: {}",
                        dsaStudent.stdNo(), dsaStudent.stdNm(),
                        oldRfid, dsaStudent.rfidNo(), storeId);
                    // Why: 옛 동작과 동일하게 syncedStudentIds에 추가하지 않아
                    // 마지막 비활성화 단계에서 이 학생이 비활성화되도록 한다
                    // (rfid 충돌 상태 노출을 위함).
                    return new UpsertOutcome(null, UpsertStatus.SKIPPED);
                }
                // DSA 3.24 실패 시 기존 전화번호 유지
                String resolvedPhone = phone != null ? phone : matched.getPhone();
                String normalizedLast4 = PhoneNormalizer.last(
                    PhoneNormalizer.normalizeDigits(dsaStudent.hp()), 4);
                if (hasChanges(matched, dsaStudent, seat, resolvedPhone, normalizedLast4)) {
                    matched.syncFromDsa(
                        dsaStudent.stdNm(),
                        dsaStudent.rfidNo(),
                        dsaStudent.stdNo(),
                        normalizedLast4,
                        resolvedPhone,
                        seat);
                    return new UpsertOutcome(matched.getId(), UpsertStatus.UPDATED);
                }
                return new UpsertOutcome(matched.getId(), UpsertStatus.UNCHANGED);
            }
        }

        if (studentRepository.existsByRfidUidAndStoreIdNot(
                dsaStudent.rfidNo(), storeId)) {
            log.warn("RFID 중복으로 skip - 다른 지점에 동일 RFID 존재."
                    + " rfid: {}, name: {}, stdNo: {}, storeId: {}",
                dsaStudent.rfidNo(), dsaStudent.stdNm(),
                dsaStudent.stdNo(), storeId);
            return new UpsertOutcome(null, UpsertStatus.SKIPPED);
        }
        if (studentRepository.existsByRfidUidAndStoreId(
                dsaStudent.rfidNo(), storeId)) {
            log.warn("RFID 중복으로 skip - 같은 지점에 동일 RFID 존재."
                    + " rfid: {}, name: {}, stdNo: {}, storeId: {}",
                dsaStudent.rfidNo(), dsaStudent.stdNm(),
                dsaStudent.stdNo(), storeId);
            return new UpsertOutcome(null, UpsertStatus.SKIPPED);
        }
        Student newStudent = Student.builder()
            .store(store)
            .name(dsaStudent.stdNm())
            .rfidUid(dsaStudent.rfidNo())
            .studentNumber(dsaStudent.stdNo())
            .phoneLast4(PhoneNormalizer.last(
                PhoneNormalizer.normalizeDigits(dsaStudent.hp()), 4))
            .phone(phone)
            .assignedSeat(seat)
            .dsaSynced(true)
            .build();
        studentRepository.save(newStudent);
        return new UpsertOutcome(newStudent.getId(), UpsertStatus.CREATED);
    }

    @Transactional
    public int deactivateByIds(Long storeId, Set<Long> idsToDeactivate) {
        if (idsToDeactivate.isEmpty()) {
            return 0;
        }
        List<Student> students = studentRepository.findAllById(idsToDeactivate);
        int count = 0;
        for (Student s : students) {
            if (s.isActive()) {
                s.deactivate();
                count++;
                log.info("DSA 목록에 없는 학생 비활성화. studentId: {}, name: {}, storeId: {}",
                    s.getId(), s.getName(), storeId);
            }
        }
        return count;
    }

    private Student findMatchingStudent(DsaStudentData dsaStudent, Long storeId,
        Map<String, Long> stdNoCounts) {
        if (dsaStudent.rfidNo() != null && !dsaStudent.rfidNo().isBlank()) {
            Optional<Student> matched = studentRepository.findByRfidUidAndStoreIdAny(
                dsaStudent.rfidNo(), storeId);
            if (matched.isPresent()) {
                return matched.get();
            }
        }
        // Why: rfid 매칭 실패 시 학번 폴백.
        // 단, DSA 응답에 같은 학번이 2건 이상이면 의도된 중복으로 보고
        // 폴백을 건너뛰어 별개 row로 INSERT (DSA 데이터 정합성 보존).
        // 1건일 때만 rfid 재발급으로 간주하고 기존 row 재사용.
        if (dsaStudent.stdNo() != null && !dsaStudent.stdNo().isBlank()
                && stdNoCounts.getOrDefault(dsaStudent.stdNo(), 0L) == 1L) {
            List<Student> candidates = studentRepository.findAllByStudentNumberAndStoreId(
                dsaStudent.stdNo(), storeId);
            // Why: 같은 지점에 같은 학번 row가 여러 건이면 active를 우선해 선택
            // (기존 toMap merge function `(a, b) -> a.isActive() ? a : b` 동등).
            return candidates.stream()
                .max(Comparator.comparing(Student::isActive))
                .orElse(null);
        }
        return null;
    }

    private boolean hasChanges(Student student, DsaStudentData dsaStudent,
        Seat seat, String phone, String normalizedLast4) {
        // Why: DSA 응답에 있는 학생은 활성 상태여야 함.
        // 비활성인 row가 매칭되면 syncFromDsa로 복원해야 하므로 변경으로 취급.
        if (!student.isActive()) {
            return true;
        }
        if (!student.getName().equals(dsaStudent.stdNm())) {
            return true;
        }
        if (!equalsNullable(student.getRfidUid(), dsaStudent.rfidNo())) {
            return true;
        }
        if (!equalsNullable(student.getStudentNumber(), dsaStudent.stdNo())) {
            return true;
        }
        if (!equalsNullable(student.getPhoneLast4(), normalizedLast4)) {
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

    // Why: 양쪽 학번이 모두 존재하고 다를 때만 "다른 학생"으로 판정.
    // 한쪽이 비어있으면 비교 불가 → 기존 동작(같은 학생 가정) 유지해 안전 측 선택.
    private boolean isDifferentStudentByStdNo(Student matched, DsaStudentData dsa) {
        String matchedStdNo = matched.getStudentNumber();
        String dsaStdNo = dsa.stdNo();
        if (matchedStdNo == null || matchedStdNo.isBlank()) {
            return false;
        }
        if (dsaStdNo == null || dsaStdNo.isBlank()) {
            return false;
        }
        return !matchedStdNo.equals(dsaStdNo);
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

    private Seat resolveSeat(String seatCd, Long storeId) {
        if (seatCd == null || seatCd.isBlank()) {
            return null;
        }
        Optional<Seat> seat = seatRepository.findBySeatCdAndStoreId(seatCd, storeId);
        return seat.orElse(null);
    }
}
