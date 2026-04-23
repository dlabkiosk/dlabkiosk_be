package com.moduletest.deasungkioskbackend.domain.outing.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import com.moduletest.deasungkioskbackend.domain.outing.exception.OutingException;
import com.moduletest.deasungkioskbackend.domain.outing.repository.OutingRepository;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsage;
import com.moduletest.deasungkioskbackend.domain.seat.entity.SeatUsageStatus;
import com.moduletest.deasungkioskbackend.domain.seat.repository.SeatUsageRepository;
import com.moduletest.deasungkioskbackend.domain.seat.service.SeatRedisService;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import com.moduletest.deasungkioskbackend.domain.studytime.service.StudyTimeRedisService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutingAdminService {

    private final OutingRepository outingRepository;
    private final SeatUsageRepository seatUsageRepository;
    private final SeatRedisService seatRedisService;
    private final StudyTimeRedisService studyTimeRedisService;

    /**
     * 외출 수정 — 시각 변경 / 복귀 취소 / 복귀 처리 모두 이 메서드로.
     * 학생의 오늘치 최신 외출 1건을 대상으로 한다.
     *
     * <pre>
     * 기존 endedAt | 새 endedAt | 동작
     * ------------|-----------|---------------------------
     * null        | null      | startedAt만 수정 (차감 변화 없음)
     * null        | 값        | 복귀 처리 — 차감 누적 + Redis IN_USE
     * 값          | null      | 복귀 취소 — 기존 차감 롤백 + Redis OUTING
     * 값          | 값        | 기간 수정 — 기존 차감 롤백 후 새 차감 누적
     * </pre>
     */
    @Transactional
    public void updateOutingByStudent(Long studentId,
                                       LocalDateTime newStartedAt, LocalDateTime newEndedAt) {
        Outing outing = findLatestTodayOrThrow(studentId);

        validateStoreAccess(outing);

        if (newEndedAt != null && newEndedAt.isBefore(newStartedAt)) {
            throw new BusinessException(ErrorCode.INVALID_OUTING_PERIOD);
        }

        Student student = outing.getStudent();
        Long storeId = outing.getStore().getId();
        LocalDateTime oldStartedAt = outing.getStartedAt();
        LocalDateTime oldEndedAt = outing.getEndedAt();

        if (oldEndedAt != null) {
            studyTimeRedisService.subtractOutingDeduction(
                storeId, student.getId(), oldStartedAt, oldEndedAt);
        }

        outing.updatePeriod(newStartedAt, newEndedAt);

        if (newEndedAt != null) {
            studyTimeRedisService.addOutingDeduction(
                storeId, student.getId(), newStartedAt, newEndedAt);
        }

        applySeatState(student, storeId, oldEndedAt, newEndedAt);
    }

    /**
     * 학생의 오늘치 진행 중 외출을 삭제 — 유령 외출(더블 태그로 잘못 생성된 row) 정리용.
     * 진행 중 외출이 없으면 404. Redis 좌석 OUTING→IN_USE 복원.
     */
    @Transactional
    public void deleteActiveOutingByStudent(Long studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Outing outing = outingRepository
            .findActiveTodayByStudentIdWithStore(studentId, startOfDay, endOfDay)
            .orElseThrow(() -> new OutingException(ErrorCode.OUTING_NOT_FOUND));

        validateStoreAccess(outing);

        Student student = outing.getStudent();
        Long storeId = outing.getStore().getId();

        markSeatInUseIfPossible(student, storeId);
        outingRepository.delete(outing);
    }

    private Outing findLatestTodayOrThrow(Long studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Outing> list = outingRepository
            .findLatestTodayByStudentId(studentId, startOfDay, endOfDay);
        if (list.isEmpty()) {
            throw new OutingException(ErrorCode.OUTING_NOT_FOUND);
        }
        return list.get(0);
    }

    private void applySeatState(Student student, Long storeId,
                                 LocalDateTime oldEndedAt, LocalDateTime newEndedAt) {
        boolean wasActive = oldEndedAt == null;
        boolean nowActive = newEndedAt == null;
        if (wasActive == nowActive) {
            return;
        }
        if (nowActive) {
            markSeatOutingIfPossible(student, storeId);
        } else {
            markSeatInUseIfPossible(student, storeId);
        }
    }

    private void markSeatInUseIfPossible(Student student, Long storeId) {
        SeatUsage usage = seatUsageRepository
            .findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .orElse(null);
        if (usage == null) {
            log.warn("외출 수정 — 복원할 활성 좌석 없음. studentId: {}", student.getId());
            return;
        }
        seatRedisService.markSeatInUse(
            storeId, usage.getSeat().getId(), student.getId(), student.getName());
    }

    private void markSeatOutingIfPossible(Student student, Long storeId) {
        SeatUsage usage = seatUsageRepository
            .findByStudentIdAndStatus(student.getId(), SeatUsageStatus.IN_USE)
            .orElse(null);
        if (usage == null) {
            log.warn("외출 복귀 취소 — 활성 좌석 없음. studentId: {}", student.getId());
            return;
        }
        seatRedisService.markSeatOuting(
            storeId, usage.getSeat().getId(), student.getId(), student.getName());
    }

    private void validateStoreAccess(Outing outing) {
        if (SecurityUtil.isAdmin()) {
            return;
        }
        Long currentStoreId = SecurityUtil.getCurrentStoreId();
        if (!outing.getStore().getId().equals(currentStoreId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
