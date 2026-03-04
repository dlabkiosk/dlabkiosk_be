package com.moduletest.deasungkioskbackend.domain.admin.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserResponse;
import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;
import com.moduletest.deasungkioskbackend.domain.admin.exception.AdminException;
import com.moduletest.deasungkioskbackend.domain.admin.repository.AdminUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    public List<AdminUserResponse> findAllAdminUsers() {
        return adminUserRepository.findAllWithStore()
            .stream()
            .map(AdminUserResponse::fromEntity)
            .toList();
    }

    @Transactional
    public void deleteAdminUser(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));
        adminUserRepository.delete(adminUser);
    }

    @Transactional
    public AdminUserResponse promoteToAdmin(Long id) {
        AdminUser adminUser = adminUserRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));
        adminUser.promoteToAdmin();
        return AdminUserResponse.fromEntity(adminUser);
    }
}
