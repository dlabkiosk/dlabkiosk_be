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

    @Column(name = "rfid_uid", nullable = false, unique = true, length = 50)
    private String rfidUid;

    @Column(name = "student_number", unique = true, length = 30)
    private String studentNumber;

    @Column(name = "phone_last4", length = 4)
    private String phoneLast4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_seat_id")
    private Seat assignedSeat;

    @Column(name = "is_dsa_synced", nullable = false)
    private boolean dsaSynced;

    @Builder
    public Student(Store store, String name, String rfidUid,
                   String studentNumber, String phoneLast4, Seat assignedSeat,
                   boolean dsaSynced) {
        this.store = store;
        this.name = name;
        this.rfidUid = rfidUid;
        this.studentNumber = studentNumber;
        this.phoneLast4 = phoneLast4;
        this.assignedSeat = assignedSeat;
        this.dsaSynced = dsaSynced;
    }

    public void updateInfo(String name, String studentNumber, String phoneLast4) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.phoneLast4 = phoneLast4;
    }

    public void updateRfidUid(String rfidUid) {
        this.rfidUid = rfidUid;
    }

    public void assignSeat(Seat seat) {
        this.assignedSeat = seat;
    }
}
