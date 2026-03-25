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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meal_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 10)
    private MealType mealType;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Column(name = "tagged_at", nullable = false)
    private LocalDateTime taggedAt;

    @Builder
    public MealTag(Student student, Store store, MealType mealType,
                   LocalDate mealDate, LocalDateTime taggedAt) {
        this.student = student;
        this.store = store;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.taggedAt = taggedAt;
    }
}
