package com.moduletest.deasungkioskbackend.domain.advertisement.entity;

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
@Table(name = "advertisements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "media_type", nullable = false, length = 10)
    private String mediaType;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "display_seconds", nullable = false)
    private int displaySeconds;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public Advertisement(Store store, String imageUrl, String mediaType,
        int displayOrder, int displaySeconds, boolean active) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.mediaType = mediaType;
        this.displayOrder = displayOrder;
        this.displaySeconds = displaySeconds;
        this.active = active;
    }

    public void updateInfo(String imageUrl, String mediaType,
        Integer displayOrder, Integer displaySeconds, Boolean active) {
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        if (mediaType != null) {
            this.mediaType = mediaType;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
        if (displaySeconds != null) {
            this.displaySeconds = displaySeconds;
        }
        if (active != null) {
            this.active = active;
        }
    }
}
