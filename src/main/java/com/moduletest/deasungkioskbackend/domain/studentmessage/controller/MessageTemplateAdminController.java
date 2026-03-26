package com.moduletest.deasungkioskbackend.domain.studentmessage.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateCreateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.MessageTemplateUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.service.MessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 메시지 템플릿",
    description = "자주 사용하는 문구 템플릿 관리 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/message-templates")
@RequiredArgsConstructor
public class MessageTemplateAdminController {

    private final MessageTemplateService messageTemplateService;

    @Operation(summary = "메시지 템플릿 목록 조회",
        description = "MANAGER: 자기 지점 템플릿. ADMIN: 전체 지점 템플릿.")
    @GetMapping
    public CommonResponse<List<MessageTemplateResponse>> findAllTemplates() {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            messageTemplateService.findAllTemplates(storeId));
    }

    @Operation(summary = "메시지 템플릿 등록",
        description = "MANAGER: 자기 지점에 자동 등록. ADMIN: storeId 필수 — 등록할 지점 지정.")
    @PostMapping
    public CommonResponse<MessageTemplateResponse> createTemplate(
        @Valid @RequestBody MessageTemplateCreateRequest request) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(request.storeId());
        return CommonResponse.success(
            messageTemplateService.createTemplate(request, resolvedStoreId));
    }

    @Operation(summary = "메시지 템플릿 수정")
    @PutMapping("/{id}")
    public CommonResponse<MessageTemplateResponse> updateTemplate(
        @PathVariable Long id,
        @Valid @RequestBody MessageTemplateUpdateRequest request) {
        return CommonResponse.success(
            messageTemplateService.updateTemplate(id, request));
    }

    @Operation(summary = "메시지 템플릿 삭제")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteTemplate(@PathVariable Long id) {
        messageTemplateService.deleteTemplate(id);
        return CommonResponse.success(null);
    }
}
