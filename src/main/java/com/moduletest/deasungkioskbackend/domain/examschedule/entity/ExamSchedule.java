package com.moduletest.deasungkioskbackend.domain.examschedule.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String examName;

    @Column(nullable = false)
    private LocalDate examDate;

    @Builder
    public ExamSchedule(Store store, String examName, LocalDate examDate) {
        this.store = store;
        this.examName = examName;
        this.examDate = examDate;
    }

    public void updateInfo(String examName, LocalDate examDate) {
        this.examName = examName;
        this.examDate = examDate;
    }
}
