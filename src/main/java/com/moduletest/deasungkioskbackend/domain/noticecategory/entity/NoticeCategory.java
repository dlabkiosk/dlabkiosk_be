package com.moduletest.deasungkioskbackend.domain.noticecategory.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice_categories",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_notice_categories_store_name",
        columnNames = {"store_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public NoticeCategory(Store store, String name, int displayOrder) {
        this.store = store;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
