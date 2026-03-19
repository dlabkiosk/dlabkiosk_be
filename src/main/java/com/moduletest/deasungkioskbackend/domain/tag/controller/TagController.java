package com.moduletest.deasungkioskbackend.domain.tag.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagConfirmRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse;
import com.moduletest.deasungkioskbackend.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 통합 태그",
    description = "학생증 태그 한 번으로 등원/하원/외출/복귀/조퇴/급식 자동 처리")
@RestController
@RequestMapping("/api/v1/kiosk/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "통합 태그",
        description = "학생증(RFID/QR)을 태그하면 상태 기반으로 출결 유형을 자동 판별한다. "
            + "식사시간(점심 12:10~13:10, 저녁 18:00~19:00)에는 급식 정보도 함께 반환한다.")
    @PostMapping
    public CommonResponse<TagResponse> processTag(
        @Valid @RequestBody TagRequest request) {
        Long storeId = getStoreIdFromToken();
        return CommonResponse.success(tagService.processTag(request, storeId));
    }

    @Operation(summary = "출결 확인",
        description = "승인된 외출/조퇴 신청이 있을 때, 학생이 확인 후 호출한다. "
            + "D(외출) 또는 C(조퇴)만 허용된다.")
    @PostMapping("/confirm")
    public CommonResponse<TagResponse> confirmTag(
        @Valid @RequestBody TagConfirmRequest request) {
        Long storeId = getStoreIdFromToken();
        return CommonResponse.success(tagService.confirmTag(request, storeId));
    }

    @Operation(summary = "급식 태그 확인",
        description = "식사시간에 급식 태그를 확인한다. "
            + "신청 내역이 없으면 안내 메시지, 있으면 태그 완료 처리. "
            + "[주의] DSA 급식 신청 조회(3.30) 아직 미연동 — 현재 DSA 인증정보 없으면 전부 신청한 것으로 처리됨.")
    @PostMapping("/meal-confirm")
    public CommonResponse<TagResponse> confirmMealTag(
        @RequestParam @NotBlank String identifier) {
        Long storeId = getStoreIdFromToken();
        return CommonResponse.success(tagService.confirmMealTag(identifier, storeId));
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
