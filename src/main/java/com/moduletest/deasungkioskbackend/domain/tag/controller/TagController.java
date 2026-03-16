package com.moduletest.deasungkioskbackend.domain.tag.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagConfirmRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse;
import com.moduletest.deasungkioskbackend.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 통합 태그", description = "학생증 태그 한 번으로 등원/하원/외출/복귀/조퇴 자동 처리")
@RestController
@RequestMapping("/api/v1/kiosk/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "통합 태그",
        description = "학생증(RFID/QR)을 태그하면 DSA가 시간 기준으로 출결 유형을 자동 판별한다. "
            + "DSA 미연동 시 로컬 상태 기반으로 판별한다. "
            + "S(등원), T(하원), A(지각), D(외출), C(조퇴), R(복귀)")
    @PostMapping
    public CommonResponse<TagResponse> processTag(
        @Valid @RequestBody TagRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(tagService.processTag(request, storeId));
    }

    @Operation(summary = "통합 태그 확인",
        description = "승인된 외출/조퇴 신청이 있을 때, 학생이 확인 후 호출한다. "
            + "D(외출) 또는 C(조퇴)만 허용된다.")
    @PostMapping("/confirm")
    public CommonResponse<TagResponse> confirmTag(
        @Valid @RequestBody TagConfirmRequest request) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(tagService.confirmTag(request, storeId));
    }
}
