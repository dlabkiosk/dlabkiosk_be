package com.moduletest.deasungkioskbackend.domain.meal.service;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealUnappliedLog;
import com.moduletest.deasungkioskbackend.domain.meal.repository.MealUnappliedLogRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 급식 미신청 학생이 급식시간에 매장에 있었던 흔적을 기록/삭제한다.
 * 메인 태깅 흐름이 실패하지 않도록 모든 호출은 try-catch로 감싸진다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MealUnappliedLogService {

    private final MealUnappliedLogRepository mealUnappliedLogRepository;

    /**
     * 식사시간 + 미신청 + 등원/하원/복귀/대기 액션일 때 흔적 기록.
     * 같은 (student, date, mealType) 키는 idempotent.
     */
    public void recordIfUnapplied(Student student, Store store, MealType mealType,
        LocalDate mealDate, String tagAction) {
        if (mealType == null) {
            return;
        }
        try {
            if (mealUnappliedLogRepository.existsByStudentIdAndMealDateAndMealType(
                    student.getId(), mealDate, mealType)) {
                return;
            }

            MealUnappliedLog entry = MealUnappliedLog.builder()
                .store(store)
                .student(student)
                .mealDate(mealDate)
                .mealType(mealType)
                .detectedAt(LocalDateTime.now())
                .tagAction(tagAction)
                .build();

            mealUnappliedLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("급식 미신청 재원 로그 저장 실패 - studentId: {}, mealType: {}, error: {}",
                student.getId(), mealType, e.getMessage());
        }
    }

    /**
     * 외출/조퇴 확정 시 기존 흔적 row 삭제 (매장 떠난 거라 기록 의미 없음).
     */
    public void removeIfExists(Long studentId, LocalDate mealDate, MealType mealType) {
        if (mealType == null) {
            return;
        }
        try {
            mealUnappliedLogRepository
                .findByStudentIdAndMealDateAndMealType(studentId, mealDate, mealType)
                .ifPresent(mealUnappliedLogRepository::delete);
        } catch (Exception e) {
            log.warn("급식 미신청 재원 로그 삭제 실패 - studentId: {}, mealType: {}, error: {}",
                studentId, mealType, e.getMessage());
        }
    }
}
