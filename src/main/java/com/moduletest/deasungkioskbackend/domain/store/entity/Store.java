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

    @Column(name = "kiosk_pin", nullable = false, length = 10)
    private String kioskPin;

    @Column(name = "dsa_acad_cd", length = 50)
    private String dsaAcadCd;

    @Column(name = "dsa_client_id", length = 100)
    private String dsaClientId;

    @Column(name = "dsa_secret_id", length = 200)
    private String dsaSecretId;

    @Column(name = "is_dsa_synced", nullable = false)
    private boolean dsaSynced;

    @Builder
    public Store(String storeName, String storeCode, String address, String phone,
                 boolean active, String kioskPin, String dsaAcadCd, String dsaClientId,
                 String dsaSecretId, boolean dsaSynced) {
        this.storeName = storeName;
        this.storeCode = storeCode;
        this.address = address;
        this.phone = phone;
        this.active = active;
        this.kioskPin = kioskPin != null ? kioskPin : "0000";
        this.dsaAcadCd = dsaAcadCd;
        this.dsaClientId = dsaClientId;
        this.dsaSecretId = dsaSecretId;
        this.dsaSynced = dsaSynced;
    }

    public void updateInfo(String storeName, String address, String phone, Boolean active) {
        if (storeName != null && !storeName.isBlank()) {
            this.storeName = storeName;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (active != null) {
            this.active = active;
        }
    }

    public void updateKioskPin(String kioskPin) {
        if (kioskPin != null && !kioskPin.isBlank()) {
            this.kioskPin = kioskPin;
        }
    }

    public void updateDsaCredentials(String dsaAcadCd, String dsaClientId, String dsaSecretId) {
        if (dsaAcadCd != null && !dsaAcadCd.isBlank()) {
            this.dsaAcadCd = dsaAcadCd;
        }
        if (dsaClientId != null && !dsaClientId.isBlank()) {
            this.dsaClientId = dsaClientId;
        }
        if (dsaSecretId != null && !dsaSecretId.isBlank()) {
            this.dsaSecretId = dsaSecretId;
        }
    }

    public boolean hasDsaCredentials() {
        return dsaAcadCd != null && dsaClientId != null && dsaSecretId != null;
    }

    public void syncFromDsa(String storeName) {
        this.storeName = storeName;
        this.dsaSynced = true;
    }
}
