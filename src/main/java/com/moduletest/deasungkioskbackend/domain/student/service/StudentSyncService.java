package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStudentService;
import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.student.repository.StudentRepository;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentSyncTxService.UpsertOutcome;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 학생 동기화 오케스트레이터.
 * Why: DSA HTTP 호출(목록/전화번호)을 트랜잭션 밖에서 수행하고, 학생 1명 upsert와
 * 비활성화 처리만 StudentSyncTxService에 위임해 짧은 트랜잭션으로 분리한다.
 * 단일 트랜잭션이 수백 학생을 한 커넥션으로 묶던 풀 고갈 + 전체 롤백 문제를 해소.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSyncService {

    private final DsaStudentService dsaStudentService;
    private final StudentRepository studentRepository;
    private final StoreRepository storeRepository;
    private final SeatService seatService;
    private final StudentSyncTxService txService;

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

    public StudentSyncResult synchronizeByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.hasDsaCredentials()) {
            throw new BusinessException(ErrorCode.SYNC_DSA_CREDENTIALS_MISSING);
        }

        return synchronizeStudentsByStore(store);
    }

    public StudentSyncResult synchronizeStudentsByStore(Store store) {
        // 좌석 동기화 - SeatService는 별도 빈이라 프록시 거침 → 자체 트랜잭션 (1지점 단위)
        try {
            seatService.synchronizeSeats(store);
        } catch (Exception e) {
            log.error("좌석 동기화 실패 - 학생 동기화는 계속 진행. storeId: {}",
                store.getId(), e);
        }

        // DSA 학생 목록 조회 - HTTP, 트랜잭션 밖
        List<DsaStudentData> dsaStudents = dsaStudentService.findAllStudents(store);
        if (dsaStudents.isEmpty()) {
            log.info("DSA 학생 데이터 없음. storeId: {}", store.getId());
            return new StudentSyncResult(
                store.getId(), store.getStoreName(),
                0, 0, 0, 0, 0, 0, 0, List.of());
        }

        // 학번 폴백 매칭에 사용할 카운트 (DSA 응답 기준)
        Map<String, Long> stdNoCounts = dsaStudents.stream()
            .filter(s -> s.stdNo() != null && !s.stdNo().isBlank())
            .collect(Collectors.groupingBy(
                DsaStudentData::stdNo, Collectors.counting()));

        // 비활성화 후보: sync 시작 시점의 활성 학생 ID 스냅샷
        // Why: sync 도중 다른 경로로 추가된 학생은 비활성화 대상에서 제외하기 위함.
        Set<Long> existingActiveIds = studentRepository.findAllByStoreId(store.getId())
            .stream()
            .filter(Student::isActive)
            .map(Student::getId)
            .collect(Collectors.toSet());

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        int failed = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        Set<Long> syncedStudentIds = new HashSet<>();
        Set<String> seenRfidsInSync = new HashSet<>();

        for (DsaStudentData dsaStudent : dsaStudents) {
            if (dsaStudent.rfidNo() == null || dsaStudent.rfidNo().isBlank()) {
                skipped++;
                log.info("RFID 미부여 학생 동기화 skip. name: {}, stdNo: {}, storeId: {}",
                    dsaStudent.stdNm(), dsaStudent.stdNo(), store.getId());
                continue;
            }
            // Why: DSA가 같은 sync 응답에 동일 rfid를 여러 번 내려주는 케이스
            // (학생 분리/중복 행). 두 번째 이후는 skip해서 갱신 충돌 차단.
            if (!seenRfidsInSync.add(dsaStudent.rfidNo())) {
                skipped++;
                log.warn("DSA 응답 내 RFID 중복 - skip. rfid: {}, name: {}, stdNo: {},"
                        + " storeId: {}",
                    dsaStudent.rfidNo(), dsaStudent.stdNm(),
                    dsaStudent.stdNo(), store.getId());
                continue;
            }

            // DSA 전화번호 조회 - HTTP, 트랜잭션 밖
            String phone;
            try {
                phone = dsaStudentService.findStudentPhone(
                    dsaStudent.rfidNo(), dsaStudent.stdNo(), store);
            } catch (Exception e) {
                log.warn("DSA findStudentPhone 실패 - 전화번호 없이 진행. rfid: {}, msg: {}",
                    dsaStudent.rfidNo(), e.getMessage());
                phone = null;
            }

            // 학생 1명 upsert - 짧은 별도 트랜잭션
            try {
                UpsertOutcome outcome = txService.upsertOneStudent(
                    store.getId(), dsaStudent, phone, stdNoCounts);

                if (outcome.studentId() != null) {
                    syncedStudentIds.add(outcome.studentId());
                }
                switch (outcome.status()) {
                    case CREATED -> created++;
                    case UPDATED -> updated++;
                    case UNCHANGED -> unchanged++;
                    case SKIPPED -> skipped++;
                    default -> { }
                }
            } catch (Exception e) {
                failed++;
                String errorMsg = String.format("학생 동기화 실패 [%s / %s]: %s",
                    dsaStudent.stdNm(), dsaStudent.rfidNo(), e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg, e);
            }
        }

        // 비활성화 - 별도 트랜잭션
        Set<Long> idsToDeactivate = new HashSet<>(existingActiveIds);
        idsToDeactivate.removeAll(syncedStudentIds);
        int deactivated = 0;
        try {
            deactivated = txService.deactivateByIds(store.getId(), idsToDeactivate);
        } catch (Exception e) {
            log.error("비활성화 처리 실패. storeId: {}", store.getId(), e);
            errors.add("비활성화 처리 실패: " + e.getMessage());
        }

        return new StudentSyncResult(
            store.getId(), store.getStoreName(),
            dsaStudents.size(), created, updated, unchanged, deactivated, failed, skipped,
            errors);
    }
}
