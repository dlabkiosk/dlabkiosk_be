package com.moduletest.deasungkioskbackend.domain.student.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "qr_uuid", nullable = false, unique = true, length = 36)
    private String qrUuid;

    @Column(nullable = false, length = 20)
    private String grade;

    @Column(name = "class_name", length = 30)
    private String className;

    @Column(name = "rfid_uid", unique = true, length = 50)
    private String rfidUid;

    @Column(name = "student_number", unique = true, length = 30)
    private String studentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_seat_id")
    private Seat assignedSeat;

    @Builder
    public Student(Store store, String name, String phone, String qrUuid,
                   String grade, String className, String rfidUid,
                   String studentNumber, Seat assignedSeat) {
        this.store = store;
        this.name = name;
        this.phone = phone;
        this.qrUuid = qrUuid;
        this.grade = grade;
        this.className = className;
        this.rfidUid = rfidUid;
        this.studentNumber = studentNumber;
        this.assignedSeat = assignedSeat;
    }

    public void updateInfo(String name, String phone, String grade,
                           String className, Store store, String studentNumber) {
        this.name = name;
        this.phone = phone;
        this.grade = grade;
        this.className = className;
        this.store = store;
        this.studentNumber = studentNumber;
    }

    public void updateRfidUid(String rfidUid) {
        this.rfidUid = rfidUid;
    }

    public void assignSeat(Seat seat) {
        this.assignedSeat = seat;
    }
}
