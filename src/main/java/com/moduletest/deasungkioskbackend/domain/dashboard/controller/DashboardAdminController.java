package com.moduletest.deasungkioskbackend.domain.dashboard.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.dashboard.dto.DashboardAllResponse;
import com.moduletest.deasungkioskbackend.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 대시보드", description = "대시보드 데이터 조회 (관리자)")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 전체 조회")
    @GetMapping("/all")
    public CommonResponse<DashboardAllResponse> getAll(
        @RequestParam(required = false) Long storeId) {
        return CommonResponse.success(
            dashboardService.getAll(resolveStoreId(storeId)));
    }

    private Long resolveStoreId(Long storeId) {
        return SecurityUtil.resolveStoreIdRequired(storeId);
    }
}
