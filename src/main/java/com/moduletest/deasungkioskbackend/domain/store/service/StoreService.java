package com.moduletest.deasungkioskbackend.domain.store.service;

import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreCreateRequest;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreResponse;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;



    public List<StoreResponse> findAllStores() {
        return storeRepository.findAll()
            .stream()
            .map(StoreResponse::fromEntity)
            .toList();
    }

    public StoreResponse findStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
        return StoreResponse.fromEntity(store);
    }

    public StoreResponse findStoreByCode(String storeCode) {
        Store store = storeRepository.findByStoreCode(storeCode)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
        return StoreResponse.fromEntity(store);
    }

    @Transactional
    public StoreResponse createStore(StoreCreateRequest request) {
        if (storeRepository.existsByStoreCode(request.storeCode())) {
            throw new StoreException(ErrorCode.DUPLICATE_STORE_CODE);
        }

        Store store = Store.builder()
            .storeName(request.storeName())
            .storeCode(request.storeCode())
            .address(request.address())
            .phone(request.phone())
            .active(true)
            .kioskPin(request.kioskPin())
            .build();
        Store savedStore = storeRepository.save(store);
        return StoreResponse.fromEntity(savedStore);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, StoreUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        store.updateInfo(request.storeName(), request.address(), request.phone(), request.active());

        if (request.kioskPin() != null) {
            store.updateKioskPin(request.kioskPin());
        }

        return StoreResponse.fromEntity(store);
    }

    @Transactional
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
        storeRepository.delete(store);
    }

}
