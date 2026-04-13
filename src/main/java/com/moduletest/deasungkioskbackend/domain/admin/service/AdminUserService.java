package com.moduletest.deasungkioskbackend.domain.admin.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserCreateRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserResponse;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;
import com.moduletest.deasungkioskbackend.domain.admin.exception.AdminException;
import com.moduletest.deasungkioskbackend.domain.admin.repository.AdminUserRepository;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final Set<String> VALID_ROLES = Set.of("MANAGER", "ADMIN");

    private final AdminUserRepository adminUserRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminUserResponse createAdminUser(AdminUserCreateRequest request) {
        if (!VALID_ROLES.contains(request.role())) {
            throw new AdminException(ErrorCode.INVALID_ROLE);
        }

        if (adminUserRepository.findByLoginId(request.loginId()).isPresent()) {
            throw new AdminException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        Store store = storeRepository.findById(request.storeId())
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        AdminUser adminUser = AdminUser.builder()
            .loginId(request.loginId())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .role(request.role())
            .store(store)
            .build();
        adminUserRepository.save(adminUser);

        return AdminUserResponse.fromEntity(adminUser);
    }

    public AdminUserResponse findAdminUserById(Long id) {
        AdminUser adminUser = adminUserRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));
        return AdminUserResponse.fromEntity(adminUser);
    }

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
    public AdminUserResponse updateAdminUser(Long id, AdminUserUpdateRequest request) {
        AdminUser adminUser = adminUserRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));

        if (request.name() != null && !request.name().isBlank()) {
            adminUser.updateName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            adminUser.updatePassword(passwordEncoder.encode(request.password()));
        }
        if (request.storeId() != null) {
            Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
            adminUser.updateStore(store);
        }
        if (request.role() != null && !request.role().isBlank()) {
            if (!VALID_ROLES.contains(request.role())) {
                throw new AdminException(ErrorCode.INVALID_ROLE);
            }
            adminUser.changeRole(request.role());
        }

        return AdminUserResponse.fromEntity(adminUser);
    }

    @Transactional
    public AdminUserResponse changeRole(Long id, String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new AdminException(ErrorCode.INVALID_ROLE);
        }

        AdminUser adminUser = adminUserRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));
        adminUser.changeRole(role);
        return AdminUserResponse.fromEntity(adminUser);
    }
}
