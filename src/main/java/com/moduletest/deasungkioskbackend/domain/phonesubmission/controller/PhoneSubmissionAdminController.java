package com.moduletest.deasungkioskbackend.domain.phonesubmission.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.service.PhoneSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 휴대폰 미소지", description = "휴대폰 미소지 현황 조회 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/phone-submissions")
@RequiredArgsConstructor
public class PhoneSubmissionAdminController {

    private final PhoneSubmissionService phoneSubmissionService;

    @Operation(summary = "당일 미소지 현황 조회",
        description = "지점의 당일 휴대폰 미소지 신청 내역을 조회한다.")
    @GetMapping
    public CommonResponse<List<PhoneSubmissionResponse>> findAllPhoneSubmissionsToday(
        @RequestParam(required = false) Long storeId) {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);
        return CommonResponse.success(
            phoneSubmissionService.findAllByStoreIdToday(resolvedStoreId));
    }
}
