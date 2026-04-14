package com.moduletest.deasungkioskbackend.domain.noticecategory.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.noticecategory.dto.NoticeCategoryResponse;
import com.moduletest.deasungkioskbackend.domain.noticecategory.service.NoticeCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 공지 말머리", description = "공지 말머리 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/notice-categories")
@RequiredArgsConstructor
public class NoticeCategoryController {

    private final NoticeCategoryService noticeCategoryService;

    @Operation(summary = "지점 말머리 목록 조회",
        description = "현재 키오스크 지점의 공지 말머리를 displayOrder 오름차순으로 반환한다.")
    @GetMapping
    public CommonResponse<List<NoticeCategoryResponse>> findAll() {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        return CommonResponse.success(noticeCategoryService.findAllByStoreId(storeId));
    }
}
