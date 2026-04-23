package com.moduletest.deasungkioskbackend.domain.attendance.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseTimeEntity {

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
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "check_in_at", nullable = false)
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "check_out_action", length = 10)
    private String checkOutAction;

    @Builder
    public Attendance(Student student, Store store, LocalDateTime checkInAt) {
        this.student = student;
        this.store = store;
        this.status = AttendanceStatus.CHECKED_IN;
        this.checkInAt = checkInAt;
    }

    public void checkOut(LocalDateTime checkOutAt, String action) {
        this.status = AttendanceStatus.CHECKED_OUT;
        this.checkOutAt = checkOutAt;
        this.checkOutAction = action;
    }

    public void updateCheckInAt(LocalDateTime checkInAt) {
        this.checkInAt = checkInAt;
    }

    public void updateCheckOut(LocalDateTime checkOutAt, String action) {
        this.checkOutAt = checkOutAt;
        if (action != null) {
            this.checkOutAction = action;
        }
    }

    public void cancelCheckOut() {
        this.status = AttendanceStatus.CHECKED_IN;
        this.checkOutAt = null;
        this.checkOutAction = null;
    }
}
