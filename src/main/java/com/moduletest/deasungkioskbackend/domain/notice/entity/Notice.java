package com.moduletest.deasungkioskbackend.domain.notice.entity;

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
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder
    public Notice(Store store, String title, String content, boolean pinned, boolean active) {
        this.store = store;
        this.title = title;
        this.content = content;
        this.pinned = pinned;
        this.active = active;
    }

    public void updateInfo(String title, String content, Boolean pinned, Boolean active) {
        this.title = title;
        this.content = content;
        if (pinned != null) {
            this.pinned = pinned;
        }
        if (active != null) {
            this.active = active;
        }
    }
}
