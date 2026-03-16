package com.moduletest.deasungkioskbackend.domain.phonesubmission.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.service.PhoneSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 휴대폰 미소지", description = "휴대폰 미소지 신청 (KIOSK 인증 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/phone-submissions")
@RequiredArgsConstructor
public class PhoneSubmissionController {

    private final PhoneSubmissionService phoneSubmissionService;

    @Operation(summary = "휴대폰 미소지 신청",
        description = "전화번호 뒷자리로 학생을 식별하여 휴대폰 미소지를 신청한다. "
            + "DAILY(당일), PERIOD(기간 설정), NO_PHONE(휴대폰 미보유) 유형을 선택할 수 있다. "
            + "기간이 겹치는 중복 신청 시 거부된다.")
    @PostMapping
    public CommonResponse<PhoneSubmissionResponse> submitPhoneNonPossession(
        @Valid @RequestBody PhoneSubmissionRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            phoneSubmissionService.submitPhoneNonPossession(request, storeId));
    }
}
