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
     * 당월 승인된(state=S) 사유신청 목록을 반환한다.
     *
     * @return 승인된 신청 목록 (reg_cd, reg_dt, reg_gn). DSA 실패 시 빈 리스트
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

            List<ApprovedRequest> approved = new ArrayList<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                String state = getStringValue(item, "state");
                if ("S".equals(state)) {
                    approved.add(new ApprovedRequest(
                        getStringValue(item, "reg_cd"),
                        getStringValue(item, "reg_dt"),
                        getStringValue(item, "reg_gn")
                    ));
                }
            }

            log.info("DSA 승인된 신청 {}건. rfidUid: {}, storeId: {}",
                approved.size(), rfidUid, store.getId());
            return approved;

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
