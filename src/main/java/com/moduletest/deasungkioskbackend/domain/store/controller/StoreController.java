package com.moduletest.deasungkioskbackend.domain.store.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreResponse;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/{storeCode}")
    public CommonResponse<StoreResponse> findStoreByCode(@PathVariable String storeCode) {
        StoreResponse store = storeService.findStoreByCode(storeCode);
        return CommonResponse.success(store);
    }
}
