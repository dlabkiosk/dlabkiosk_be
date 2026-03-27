package com.moduletest.deasungkioskbackend.domain.store.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStoreData;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaStoreService;
import com.moduletest.deasungkioskbackend.domain.store.dto.StoreSyncResult;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreSyncService {

    private final DsaStoreService dsaStoreService;
    private final StoreRepository storeRepository;

    @Transactional
    public StoreSyncResult synchronize() {
        Store anyStore = findAnyStoreWithCredentials();
        if (anyStore == null) {
            return new StoreSyncResult(0, 0, 0, 0,
                List.of("DSA 인증정보가 있는 지점이 없습니다"));
        }

        List<DsaStoreData> dsaStores = dsaStoreService.findAllStores(anyStore);
        if (dsaStores.isEmpty()) {
            log.info("DSA 지점 데이터 없음");
            return new StoreSyncResult(0, 0, 0, 0, List.of());
        }

        Map<String, Store> byAcadCd = storeRepository.findAll().stream()
            .filter(s -> s.getDsaAcadCd() != null)
            .collect(Collectors.toMap(Store::getDsaAcadCd, Function.identity(),
                (existing, duplicate) -> existing));

        int created = 0;
        int updated = 0;
        int unchanged = 0;
        List<String> errors = new ArrayList<>();

        for (DsaStoreData dsaStore : dsaStores) {
            try {
                Store matched = byAcadCd.get(dsaStore.acadCd());

                if (matched != null) {
                    if (!matched.getStoreName().equals(dsaStore.acadNm())) {
                        matched.syncFromDsa(dsaStore.acadNm());
                        updated++;
                    } else {
                        unchanged++;
                    }
                } else {
                    Store newStore = Store.builder()
                        .storeName(dsaStore.acadNm())
                        .storeCode("DSA-" + dsaStore.acadCd())
                        .active(true)
                        .dsaAcadCd(dsaStore.acadCd())
                        .dsaSynced(true)
                        .build();
                    storeRepository.save(newStore);
                    created++;
                }
            } catch (Exception e) {
                String errorMsg = String.format("지점 동기화 실패 [%s / %s]: %s",
                    dsaStore.acadNm(), dsaStore.acadCd(), e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg, e);
            }
        }

        log.info("지점 동기화 결과 - 전체: {}, 생성: {}, 수정: {}, 변경없음: {}",
            dsaStores.size(), created, updated, unchanged);

        return new StoreSyncResult(dsaStores.size(), created, updated, unchanged, errors);
    }

    private Store findAnyStoreWithCredentials() {
        List<Store> stores = storeRepository.findAllWithDsaCredentials();
        return stores.isEmpty() ? null : stores.get(0);
    }
}
