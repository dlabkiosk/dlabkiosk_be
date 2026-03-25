package com.moduletest.deasungkioskbackend.domain.seat.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "seat_label", nullable = false, length = 20)
    private String seatLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;

    @Column(name = "x_pos", nullable = false)
    private int xPos;

    @Column(name = "y_pos", nullable = false)
    private int yPos;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "area_cd", length = 50)
    private String areaCd;

    @Column(name = "area_nm", length = 100)
    private String areaNm;

    @Column(name = "seat_cd", length = 50)
    private String seatCd;

    @Column(name = "seat_gn", length = 5)
    private String seatGn;

    @Column(name = "is_dsa_synced", nullable = false)
    private boolean dsaSynced;

    @Builder
    public Seat(Store store, String seatLabel, SeatType seatType, int xPos, int yPos,
        boolean active, String areaCd, String areaNm, String seatCd, String seatGn,
        boolean dsaSynced) {
        this.store = store;
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.xPos = xPos;
        this.yPos = yPos;
        this.active = active;
        this.areaCd = areaCd;
        this.areaNm = areaNm;
        this.seatCd = seatCd;
        this.seatGn = seatGn;
        this.dsaSynced = dsaSynced;
    }

    public void updateInfo(String seatLabel, SeatType seatType, int xPos, int yPos,
        boolean active) {
        this.seatLabel = seatLabel;
        this.seatType = seatType;
        this.xPos = xPos;
        this.yPos = yPos;
        this.active = active;
    }

    public void updateArea(String areaCd, String areaNm) {
        this.areaCd = areaCd;
        this.areaNm = areaNm;
    }

    public void syncFromDsa(String seatLabel, String seatCd, int xPos, int yPos,
                            String areaCd, String areaNm, String seatGn) {
        this.seatLabel = seatLabel;
        this.seatCd = seatCd;
        this.xPos = xPos;
        this.yPos = yPos;
        this.areaCd = areaCd;
        this.areaNm = areaNm;
        this.seatGn = seatGn;
        this.active = true;
        this.dsaSynced = true;
    }

    public void syncDsaSeatInfo(String seatCd, int xPos, int yPos, String seatGn) {
        this.seatCd = seatCd;
        this.xPos = xPos;
        this.yPos = yPos;
        this.seatGn = seatGn;
        this.dsaSynced = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
