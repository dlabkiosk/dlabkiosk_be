package com.moduletest.deasungkioskbackend.domain.advertisement.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.advertisement.dto.AdvertisementResponse;
import com.moduletest.deasungkioskbackend.domain.advertisement.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 광고", description = "키오스크 광고 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "활성 광고 목록 조회",
        description = "해당 지점의 활성 광고를 순서대로 반환한다.")
    @GetMapping
    public CommonResponse<List<AdvertisementResponse>> findActiveAdvertisements() {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            advertisementService.findActiveAdvertisementsByStoreId(storeId));
    }
}
