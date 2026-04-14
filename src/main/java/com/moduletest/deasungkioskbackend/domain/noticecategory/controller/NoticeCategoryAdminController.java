package com.moduletest.deasungkioskbackend.domain.noticecategory.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryCreateRequest;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryOrderRequest;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryResponse;
import com.moduletest.deasungkioskbackend.domain.noticecategory.service.NoticeCategoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 공지 말머리", description = "공지 말머리 관리 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/notice-categories")
@RequiredArgsConstructor
public class NoticeCategoryAdminController {

    private final NoticeCategoryService noticeCategoryService;

    @Operation(summary = "말머리 목록 조회",
        description = "displayOrder 오름차순. MANAGER는 자기 지점, ADMIN은 storeId 필수.")
    @GetMapping
    public CommonResponse<List<NoticeCategoryResponse>> findAll(
        @RequestParam(required = false) Long storeId) {
        Long resolved = SecurityUtil.resolveStoreIdRequired(storeId);
        return CommonResponse.success(noticeCategoryService.findAllByStoreId(resolved));
    }

    @Operation(summary = "말머리 추가",
        description = "마지막 순서로 추가된다. 같은 지점 내 중복 이름 불가.")
    @PostMapping
    public CommonResponse<NoticeCategoryResponse> create(
        @RequestParam(required = false) Long storeId,
        @Valid @RequestBody NoticeCategoryCreateRequest request) {
        Long resolved = SecurityUtil.resolveStoreIdRequired(storeId);
        return CommonResponse.success(noticeCategoryService.create(request, resolved));
    }

    @Operation(summary = "말머리 삭제",
        description = "사용 중인 말머리는 삭제할 수 없다 (NTC004).")
    @DeleteMapping("/{categoryId}")
    public CommonResponse<Void> delete(@PathVariable Long categoryId) {
        noticeCategoryService.delete(categoryId);
        return CommonResponse.success(null);
    }

    @Operation(summary = "말머리 순서 일괄 변경",
        description = "orderedIds에 지점 내 모든 말머리 ID를 원하는 순서대로 보내야 한다."
            + " 누락/중복/외부 ID 포함 시 V001.")
    @PutMapping("/order")
    public CommonResponse<List<NoticeCategoryResponse>> reorder(
        @RequestParam(required = false) Long storeId,
        @Valid @RequestBody NoticeCategoryOrderRequest request) {
        Long resolved = SecurityUtil.resolveStoreIdRequired(storeId);
        return CommonResponse.success(noticeCategoryService.reorder(request, resolved));
    }
}
