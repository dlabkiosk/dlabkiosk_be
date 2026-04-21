package com.moduletest.deasungkioskbackend.domain.admin.entity;

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
@Table(name = "admin_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder
    public AdminUser(String loginId, String password, String name, String role, Store store) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.store = store;
    }

    public void changeRole(String role) {
        this.role = role;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateStore(Store store) {
        this.store = store;
    }
}
