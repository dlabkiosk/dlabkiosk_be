package com.moduletest.deasungkioskbackend.domain.store.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_code", nullable = false, unique = true, length = 20)
    private String storeCode;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active;


    @Builder
    public Store(String storeName, String storeCode, String address, String phone, boolean active) {
        this.storeName = storeName;
        this.storeCode = storeCode;
        this.address = address;
        this.phone = phone;
        this.active = active;
    }

    public void updateInfo(String storeName, String address, String phone, boolean active) {
        this.storeName = storeName;
        this.address = address;
        this.phone = phone;
        this.active = active;
    }


}
