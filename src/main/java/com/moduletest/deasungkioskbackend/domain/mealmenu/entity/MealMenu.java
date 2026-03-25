package com.moduletest.deasungkioskbackend.domain.mealmenu.entity;

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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meal_menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealMenu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Column(length = 500)
    private String lunch;

    @Column(length = 500)
    private String dinner;

    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    @Builder
    public MealMenu(Store store, LocalDate menuDate, String lunch,
                    String dinner, boolean closed) {
        this.store = store;
        this.menuDate = menuDate;
        this.lunch = lunch;
        this.dinner = dinner;
        this.closed = closed;
    }

    public void updateMenu(String lunch, String dinner, boolean closed) {
        this.lunch = lunch;
        this.dinner = dinner;
        this.closed = closed;
    }
}
