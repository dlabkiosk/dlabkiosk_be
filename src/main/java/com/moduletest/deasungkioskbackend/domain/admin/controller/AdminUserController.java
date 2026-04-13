package com.moduletest.deasungkioskbackend.domain.admin.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserCreateRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserResponse;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.admin.exception.AdminException;
import com.moduletest.deasungkioskbackend.domain.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 관리자 관리", description = "관리자 목록 조회, 삭제, 역할 승격 (ADMIN 전용)")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "관리자 등록",
        description = "관리자 계정을 등록한다. 역할(MANAGER/ADMIN)과 소속 지점을 지정한다. ADMIN 전용.")
    @PostMapping
    public CommonResponse<AdminUserResponse> createAdminUser(
            @Valid @RequestBody AdminUserCreateRequest request) {
        requireAdmin();
        return CommonResponse.success(adminUserService.createAdminUser(request));
    }

    @Operation(summary = "관리자 목록 조회",
        description = "전체 관리자 목록을 조회한다. ADMIN 전용.")
    @GetMapping
    public CommonResponse<List<AdminUserResponse>> findAllAdminUsers() {
        requireAdmin();
        return CommonResponse.success(adminUserService.findAllAdminUsers());
    }

    @Operation(summary = "관리자 상세 조회",
        description = "관리자 ID로 상세 정보를 조회한다. ADMIN 전용.")
    @GetMapping("/{id}")
    public CommonResponse<AdminUserResponse> findAdminUserById(@PathVariable Long id) {
        requireAdmin();
        return CommonResponse.success(adminUserService.findAdminUserById(id));
    }

    @Operation(summary = "관리자 정보 수정",
        description = "관리자의 이름, 비밀번호, 소속 지점, 역할을 수정한다. null인 필드는 변경하지 않는다. ADMIN 전용.")
    @PutMapping("/{id}")
    public CommonResponse<AdminUserResponse> updateAdminUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        requireAdmin();
        return CommonResponse.success(adminUserService.updateAdminUser(id, request));
    }

    @Operation(summary = "관리자 삭제",
        description = "관리자를 삭제한다. ADMIN 전용.")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteAdminUser(@PathVariable Long id) {
        requireAdmin();
        adminUserService.deleteAdminUser(id);
        return CommonResponse.success(null);
    }

    @Operation(summary = "관리자 역할 변경",
        description = "역할을 변경한다 (MANAGER ↔ ADMIN). ADMIN 전용.")
    @PatchMapping("/{id}/role")
    public CommonResponse<AdminUserResponse> changeRole(
            @PathVariable Long id,
            @RequestParam String role) {
        requireAdmin();
        return CommonResponse.success(adminUserService.changeRole(id, role));
    }

    private void requireAdmin() {
        if (!SecurityUtil.isAdmin()) {
            throw new AdminException(ErrorCode.ACCESS_DENIED);
        }
    }
}
