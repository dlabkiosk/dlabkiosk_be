package com.moduletest.deasungkioskbackend.domain.admin.dto;

import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;

public record AdminUserResponse(
    Long id,
    String loginId,
    String name,
    String role
) {

    public static AdminUserResponse fromEntity(AdminUser adminUser) {
        return new AdminUserResponse(
            adminUser.getId(),
            adminUser.getLoginId(),
            adminUser.getName(),
            adminUser.getRole()
        );
    }
}
