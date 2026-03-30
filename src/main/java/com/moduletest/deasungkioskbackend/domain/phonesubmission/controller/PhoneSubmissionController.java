package com.moduletest.deasungkioskbackend.domain.phonesubmission.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionPeriodResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionRequest;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.phonesubmission.service.PhoneSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 휴대폰 미소지", description = "휴대폰 미소지 신청 (KIOSK 인증 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/phone-submissions")
@RequiredArgsConstructor
public class PhoneSubmissionController {

    private final PhoneSubmissionService phoneSubmissionService;

    @Operation(summary = "내 휴대폰 미소지 신청 기간 조회",
        description = "학생의 활성 신청 기간 목록을 반환한다. 달력에서 이미 신청된 날짜 블러 처리용.\n\n"
            + "inputMethod 입력 방식:\n"
            + "- RFID: 카드/QR 태깅으로 학생 식별\n"
            + "- PHONE_LAST4: 전화번호 뒷자리(4자리)로 학생 식별\n"
            + "- 미입력 시: RFID → 전화번호 8자리 → 전화번호 뒷자리 순서로 자동 판별")
    @GetMapping("/my")
    public CommonResponse<List<PhoneSubmissionPeriodResponse>> getMySubmissionPeriods(
        @RequestParam String identifier,
        @RequestParam(required = false) InputMethod inputMethod) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            phoneSubmissionService.findActivePeriodsForStudent(
                identifier, storeId, inputMethod));
    }

    @Operation(summary = "휴대폰 미소지 신청",
        description = "전화번호 뒷자리로 학생을 식별하여 휴대폰 미소지를 신청한다. "
            + "DAILY(당일), PERIOD(기간 설정), NO_PHONE(휴대폰 미보유) 유형을 선택할 수 있다. "
            + "기간이 겹치는 중복 신청 시 거부된다.\n\n"
            + "inputMethod 입력 방식:\n"
            + "- RFID: 카드/QR 태깅으로 학생 식별\n"
            + "- PHONE_LAST4: 전화번호 뒷자리(4자리)로 학생 식별\n"
            + "- 미입력 시: RFID → 전화번호 8자리 → 전화번호 뒷자리 순서로 자동 판별")
    @PostMapping
    public CommonResponse<PhoneSubmissionResponse> submitPhoneNonPossession(
        @Valid @RequestBody PhoneSubmissionRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            phoneSubmissionService.submitPhoneNonPossession(request, storeId));
    }
}
