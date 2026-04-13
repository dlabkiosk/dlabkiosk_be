package com.moduletest.deasungkioskbackend.domain.admin.service;


import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.security.TokenRedisService;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserResponse;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserUpdateMeRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.LoginRequest;
import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;
import com.moduletest.deasungkioskbackend.domain.admin.exception.AdminException;
import com.moduletest.deasungkioskbackend.domain.admin.repository.AdminUserRepository;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final StoreRepository storeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenRedisService tokenRedisService;

    // @Transactional
    // public void signup(SignupRequest request) {
    //     if (adminUserRepository.findByLoginId(request.loginId()).isPresent()) {
    //         throw new AdminException(ErrorCode.DUPLICATE_LOGIN_ID);
    //     }
    //
    //     Store store = storeRepository.findById(request.storeId())
    //         .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    //
    //     AdminUser adminUser = AdminUser.builder()
    //         .loginId(request.loginId())
    //         .password(passwordEncoder.encode(request.password()))
    //         .name(request.name())
    //         .role("MANAGER")
    //         .store(store)
    //         .build();
    //     adminUserRepository.save(adminUser);
    // }

    public String[] login(LoginRequest loginRequest) {
        AdminUser adminUser = adminUserRepository.findByLoginIdWithStore(loginRequest.userId())
            .orElseThrow(() -> new AdminException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.password(), adminUser.getPassword())) {
            throw new AdminException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
            String.valueOf(adminUser.getId()),
            adminUser.getLoginId(),
            adminUser.getRole(),
            adminUser.getStore().getId()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
            String.valueOf(adminUser.getId())
        );

        tokenRedisService.saveAdminAccessToken(
            adminUser.getId(), accessToken, jwtTokenProvider.getAccessExpiration());
        tokenRedisService.saveAdminRefreshToken(
            adminUser.getId(), refreshToken, jwtTokenProvider.getRefreshExpiration());

        return new String[] {accessToken, refreshToken};
    }

    public String refresh(String refreshToken) {
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        if (!tokenRedisService.isRefreshTokenValid(userId, refreshToken)) {
            throw new AdminException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AdminUser adminUser = adminUserRepository.findByIdWithStore(userId)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(
            String.valueOf(adminUser.getId()),
            adminUser.getLoginId(),
            adminUser.getRole(),
            adminUser.getStore().getId()
        );

        tokenRedisService.saveAdminAccessToken(
            adminUser.getId(), newAccessToken, jwtTokenProvider.getAccessExpiration());

        return newAccessToken;
    }

    public AdminUserResponse findCurrentUser(Long userId) {
        AdminUser adminUser = adminUserRepository.findByIdWithStore(userId)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));
        return AdminUserResponse.fromEntity(adminUser);
    }

    @Transactional
    public AdminUserResponse updateMe(Long userId, AdminUserUpdateMeRequest request) {
        AdminUser adminUser = adminUserRepository.findByIdWithStore(userId)
            .orElseThrow(() -> new AdminException(ErrorCode.ADMIN_NOT_FOUND));

        if (request.name() != null && !request.name().isBlank()) {
            adminUser.updateName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            adminUser.updatePassword(passwordEncoder.encode(request.password()));
        }

        return AdminUserResponse.fromEntity(adminUser);
    }

    public void logout(Long userId, String accessToken, String refreshToken) {
        if (accessToken != null) {
            tokenRedisService.removeAdminToken(userId, accessToken);
        }
        if (refreshToken != null) {
            tokenRedisService.removeAdminRefreshToken(userId, refreshToken);
        }
    }
}
