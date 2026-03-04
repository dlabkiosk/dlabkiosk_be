package com.moduletest.deasungkioskbackend.domain.store.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreCreateRequest;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.store.service.StoreService;
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

@Tag(name = "[관리자] 지점 관리", description = "지점 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
public class StoreAdminController {

    private final StoreService storeService;

    @Operation(summary = "지점 목록 조회",
        description = "ADMIN은 전체 지점, MANAGER는 소속 지점만 조회한다.")
    @GetMapping
    public CommonResponse<List<StoreResponse>> findAllStores() {
        Long resolvedStoreId = SecurityUtil.resolveStoreId(null);
        if (resolvedStoreId != null) {
            StoreResponse store = storeService.findStoreById(resolvedStoreId);
            return CommonResponse.success(List.of(store));
        }
        List<StoreResponse> stores = storeService.findAllStores();
        return CommonResponse.success(stores);
    }

    @Operation(summary = "지점 단건 조회", description = "지점 ID로 단건 조회한다.")
    @GetMapping("/{storeId}")
    public CommonResponse<StoreResponse> findStoreById(@PathVariable Long storeId) {
        StoreResponse store = storeService.findStoreById(storeId);
        return CommonResponse.success(store);
    }

    @Operation(summary = "지점 등록", description = "새 지점을 등록한다. storeCode는 중복 불가(예: DS-001).")
    @PostMapping
    public CommonResponse<StoreResponse> createStore(@Valid @RequestBody StoreCreateRequest request) {
        StoreResponse store = storeService.createStore(request);
        return CommonResponse.success(store);
    }

    @Operation(summary = "지점 수정", description = "지점 정보를 전체 수정한다.")
    @PutMapping("/{storeId}")
    public CommonResponse<StoreResponse> updateStore(@PathVariable Long storeId,
        @Valid @RequestBody StoreUpdateRequest request) {
        StoreResponse store = storeService.updateStore(storeId, request);
        return CommonResponse.success(store);
    }

    @Operation(summary = "지점 삭제", description = "지점을 삭제한다.")
    @DeleteMapping("/{storeId}")
    public CommonResponse<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return CommonResponse.success(null);
    }
}
