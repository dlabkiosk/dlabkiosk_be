package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DsaMealService {

    private static final String GET_MEAL_APPLY_YN_PATH = "/kiosk/getMealApplyYN";

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.30 getMealApplyYN 호출.
     * 해당 학생의 금일 급식 신청 여부를 확인한다.
     *
     * @param rfidUid  학생 RFID UID
     * @param mealType 식사 유형 (LUNCH/DINNER)
     * @param store    지점
     * @return 급식 신청 여부 (true: 신청, false: 미신청). DSA 호출 실패 시 true (관대하게 처리)
     */
    public boolean isMealApplied(String rfidUid, MealType mealType, Store store) {
        // TODO: DSA 개발기 연동 확인 후 제거
        if (true) {
            log.debug("DSA 급식 조회 비활성화 - 기본값(true) 사용. storeId: {}", store.getId());
            return true;
        }

        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 급식 신청 여부 기본값(true) 사용. storeId: {}",
                store.getId());
            return true;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("meal_gb", mealType.getDsaMealGb());

            DsaResponse response = dsaApiClient.post(
                GET_MEAL_APPLY_YN_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA getMealApplyYN 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return true;
            }

            Object mealYn = response.getExtra().get("meal_yn");
            if (mealYn == null) {
                log.warn("DSA 응답에 meal_yn 없음. storeId: {}", store.getId());
                return true;
            }

            boolean applied = "Y".equalsIgnoreCase(mealYn.toString().trim());
            log.info("DSA 급식 신청 확인: {} ({}). rfidUid: {}, storeId: {}",
                applied ? "신청" : "미신청", mealType.getLabel(), rfidUid, store.getId());
            return applied;

        } catch (DsaApiException e) {
            log.error("DSA getMealApplyYN 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return true;
        } catch (Exception e) {
            log.error("DSA getMealApplyYN 예외. storeId: {}", store.getId(), e);
            return true;
        }
    }
}
