package com.moduletest.deasungkioskbackend.domain.seatleave.entity;

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
@Table(name = "seat_leave_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatLeaveReason extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "reason_name", nullable = false, length = 50)
    private String reasonName;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Builder
    public SeatLeaveReason(Store store, String reasonName, int displayOrder, boolean active,
        String iconUrl) {
        this.store = store;
        this.reasonName = reasonName;
        this.displayOrder = displayOrder;
        this.active = active;
        this.iconUrl = iconUrl;
    }

    public void updateInfo(String reasonName, int displayOrder, boolean active) {
        this.reasonName = reasonName;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public void replaceIcon(String newIconUrl) {
        this.iconUrl = newIconUrl;
    }

    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }
}
