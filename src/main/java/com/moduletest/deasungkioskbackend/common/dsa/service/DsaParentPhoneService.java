package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.common.util.PhoneNormalizer;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DsaParentPhoneService {

    private static final String GET_PARENT_HP_LIST_PATH = "/kiosk/getParentHpList";

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.25 getParentHpList 호출.
     * 학부모 전화번호를 조회하여 "부: 010-xxxx / 모: 010-xxxx" 형태로 반환한다.
     */
    public String findParentPhone(String rfidUid, Store store) {
        if (!store.hasDsaCredentials() || rfidUid == null) {
            return null;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);

            DsaResponse response = dsaApiClient.post(
                GET_PARENT_HP_LIST_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null
                || response.getDataAsList().isEmpty()) {
                return null;
            }

            StringJoiner joiner = new StringJoiner(" / ");
            for (Map<String, Object> item : response.getDataAsList()) {
                String pgb = String.valueOf(item.get("p_gb"));
                String php = PhoneNormalizer.normalizeFullwidth(
                    String.valueOf(item.get("p_hp")));
                String label = formatParentLabel(pgb);
                joiner.add(label + ": " + php);
            }

            return joiner.toString();

        } catch (DsaApiException e) {
            log.warn("DSA getParentHpList 호출 실패. storeId: {}, rfidUid: {} - {}",
                store.getId(), rfidUid, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("DSA getParentHpList 예외. storeId: {}, rfidUid: {}",
                store.getId(), rfidUid, e);
            return null;
        }
    }

    private String formatParentLabel(String pgb) {
        if (pgb == null) {
            return "기타";
        }
        return switch (pgb.toUpperCase()) {
            case "F" -> "부";
            case "M" -> "모";
            default -> "기타";
        };
    }
}
