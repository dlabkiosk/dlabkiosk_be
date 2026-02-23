package com.moduletest.deasungkioskbackend.domain.store.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreResponse;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 지점", description = "키오스크에서 지점 정보를 조회하는 공개 API (인증 불필요)")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "지점 코드로 조회",
        description = "키오스크 기기에 설정된 지점 코드(예: DS-001)로 지점 정보를 조회한다.")
    @GetMapping("/{storeCode}")
    public CommonResponse<StoreResponse> findStoreByCode(@PathVariable String storeCode) {
        StoreResponse store = storeService.findStoreByCode(storeCode);
        return CommonResponse.success(store);
    }
}
