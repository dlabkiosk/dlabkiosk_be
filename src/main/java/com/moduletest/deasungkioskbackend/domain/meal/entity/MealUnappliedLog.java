package com.moduletest.deasungkioskbackend.domain.meal.entity;

import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 급식 신청을 하지 않은 학생이 급식시간 동안 매장에서 태깅한 흔적을 기록한다.
 * 외출/조퇴로 처리된 케이스는 confirmTag 단계에서 row가 삭제된다.
 */
@Entity
@Table(name = "meal_unapplied_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealUnappliedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 10)
    private MealType mealType;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "tag_action", length = 20)
    private String tagAction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MealUnappliedLog(Store store, Student student, LocalDate mealDate,
        MealType mealType, LocalDateTime detectedAt, String tagAction) {
        this.store = store;
        this.student = student;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.detectedAt = detectedAt;
        this.tagAction = tagAction;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
