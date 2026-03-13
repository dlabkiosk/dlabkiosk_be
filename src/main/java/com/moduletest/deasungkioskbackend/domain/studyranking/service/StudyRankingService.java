package com.moduletest.deasungkioskbackend.domain.studyranking.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.dto.StudyRankingResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRankingService {

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;


    public StudyRankingResponse getStudyRanking(Long storeId) {

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        return StudyRankingResponse.builder()
            .firstPlace(callDsaSafe("/kiosk/getFirstLastWeekStudyTimeStd", store))
            .averageStudyTime(callDsaSafe("/kiosk/getAvgLastWeekStudyTime", store))
            .rankingList(callDsaSafe("/kiosk/getLastWeekStudyTimeList", store))
            .build();

    }

    private DsaResponse callDsaSafe(String path, Store store) {
        try {
            return dsaApiClient.post(path, buildParams(), DsaResponse.class, store);
        } catch (Exception e) {

            return null;
        }
    }

    private Map<String, Object> buildParams() {
        return new HashMap<>();
    }

}
