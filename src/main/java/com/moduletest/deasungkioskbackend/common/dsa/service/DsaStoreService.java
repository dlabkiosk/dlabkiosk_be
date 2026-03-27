package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStoreData;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DsaStoreService {

    private static final String GET_DLAB_LIST_PATH = "/kiosk/getDlabList";

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.19 getDlabList 호출.
     * 전체 지점 목록을 조회한다.
     * 아무 지점의 토큰이나 사용 가능하므로, 임의의 Store를 전달받는다.
     */
    public List<DsaStoreData> findAllStores(Store store) {
        try {
            Map<String, Object> params = new HashMap<>();

            DsaResponse response = dsaApiClient.post(
                GET_DLAB_LIST_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getData() == null) {
                log.warn("DSA getDlabList 실패 - code: {}, message: {}",
                    response.getCode(), response.getMessage());
                return List.of();
            }

            List<DsaStoreData> stores = new ArrayList<>();
            for (Map<String, Object> item : response.getData()) {
                DsaStoreData data = DsaStoreData.fromMap(item);
                if (data.acadCd() != null && data.acadNm() != null) {
                    stores.add(data);
                } else {
                    log.warn("DSA 지점 데이터 누락 (acadCd 또는 acadNm 없음): {}", item);
                }
            }

            log.info("DSA 지점 조회 완료: {}건", stores.size());
            return stores;

        } catch (DsaApiException e) {
            log.error("DSA getDlabList 호출 실패 - {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("DSA getDlabList 예외", e);
            return List.of();
        }
    }
}
