package com.moduletest.deasungkioskbackend.domain.student.entity;

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

    @Column(name = "rfid_uid", unique = true, length = 50)
    private String rfidUid;

    @Builder
    public Student(Store store, String name, String phone, String qrUuid,
                   String grade, String rfidUid) {
        this.store = store;
        this.name = name;
        this.phone = phone;
        this.qrUuid = qrUuid;
        this.grade = grade;
        this.rfidUid = rfidUid;
    }

    public void updateInfo(String name, String phone, String grade, Store store) {
        this.name = name;
        this.phone = phone;
        this.grade = grade;
        this.store = store;
    }

    public void updateRfidUid(String rfidUid) {
        this.rfidUid = rfidUid;
    }
}
