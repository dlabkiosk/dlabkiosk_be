package com.moduletest.deasungkioskbackend.domain.store.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreCreateRequest;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreService;
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

@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
public class StoreAdminController {

    private final StoreService storeService;

    @GetMapping
    public CommonResponse<List<StoreResponse>> findAllStores() {
        List<StoreResponse> stores = storeService.findAllStores();
        return CommonResponse.success(stores);
    }

    @GetMapping("/{storeId}")
    public CommonResponse<StoreResponse> findStoreById(@PathVariable Long storeId) {
        StoreResponse store = storeService.findStoreById(storeId);
        return CommonResponse.success(store);
    }

    @PostMapping
    public CommonResponse<StoreResponse> createStore(@Valid @RequestBody StoreCreateRequest request) {
        StoreResponse store = storeService.createStore(request);
        return CommonResponse.success(store);
    }

    @PutMapping("/{storeId}")
    public CommonResponse<StoreResponse> updateStore(@PathVariable Long storeId,
        @Valid @RequestBody StoreUpdateRequest request) {
        StoreResponse store = storeService.updateStore(storeId, request);
        return CommonResponse.success(store);
    }

    @DeleteMapping("/{storeId}")
    public CommonResponse<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return CommonResponse.success(null);
    }

}
