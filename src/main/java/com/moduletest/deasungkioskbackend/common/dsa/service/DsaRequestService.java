package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DsaRequestService {

    private static final String GET_REQUEST_LIST_PATH = "/kiosk/getRequestListStd";
    private static final DateTimeFormatter MONTH_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM");

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.16 getRequestListStd 호출.
     * 당월 사유신청 전체 목록을 state 무관하게 반환한다.
     *
     * @return 신청 목록 (reg_cd, reg_dt, reg_gn). DSA 실패 시 빈 리스트
     */
    public List<ApprovedRequest> findApprovedRequests(String rfidUid, Store store) {
        if (!store.hasDsaCredentials()) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("month", LocalDate.now().format(MONTH_FORMAT));

            DsaResponse response = dsaApiClient.post(
                GET_REQUEST_LIST_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                return Collections.emptyList();
            }

            List<ApprovedRequest> requests = new ArrayList<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                requests.add(new ApprovedRequest(
                    getStringValue(item, "reg_cd"),
                    getStringValue(item, "reg_dt"),
                    getStringValue(item, "reg_gn")
                ));
            }

            log.info("DSA 출결 신청 {}건. rfidUid: {}, storeId: {}",
                requests.size(), rfidUid, store.getId());
            return requests;

        } catch (DsaApiException e) {
            log.error("DSA getRequestListStd 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("DSA getRequestListStd 예외. storeId: {}", store.getId(), e);
            return Collections.emptyList();
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    public record ApprovedRequest(
        String regCd,
        String regDt,
        String regGn
    ) {

    }
}
