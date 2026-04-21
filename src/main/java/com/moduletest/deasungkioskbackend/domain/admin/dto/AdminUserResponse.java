package com.moduletest.deasungkioskbackend.domain.admin.dto;

import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;

public record AdminUserResponse(
    Long id,
    String loginId,
    String name,
    String role,
    Long storeId,
    String storeName
) {

    public static AdminUserResponse fromEntity(AdminUser adminUser) {
        return new AdminUserResponse(
            adminUser.getId(),
            adminUser.getLoginId(),
            adminUser.getName(),
            adminUser.getRole(),
            adminUser.getStore() != null ? adminUser.getStore().getId() : null,
            adminUser.getStore() != null ? adminUser.getStore().getStoreName() : null
        );
    }
}
