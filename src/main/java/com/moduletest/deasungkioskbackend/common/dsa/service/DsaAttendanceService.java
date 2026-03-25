package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.tag.entity.AttendAction;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DsaAttendanceService {

    private static final String SET_ATTEND_STD_PATH = "/kiosk/setAttendStd";
    private static final DateTimeFormatter TAG_DT_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.14 setAttendStd 호출.
     * DSA가 시간 기준으로 att_gn(S/T/A/D/C/R)을 자동 판별하여 응답한다.
     *
     * @return 판별된 AttendAction, DSA 호출 실패 시 null
     */
    public AttendAction sendAttendTag(String rfidUid, Store store) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 로컬 판별 사용. storeId: {}", store.getId());
            return null;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("tag_dt", LocalDateTime.now().format(TAG_DT_FORMAT));
            params.put("con_gn", "");

            DsaResponse response = dsaApiClient.post(
                SET_ATTEND_STD_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA setAttendStd 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return null;
            }

            String attGn = extractAttGn(response);
            if (attGn == null) {
                log.warn("DSA 응답에 att_gn 없음. storeId: {}", store.getId());
                return null;
            }

            AttendAction action = AttendAction.fromCode(attGn);
            log.info("DSA 출결 판별: {} ({}). rfidUid: {}, storeId: {}",
                action.name(), action.getLabel(), rfidUid, store.getId());
            return action;

        } catch (DsaApiException e) {
            log.error("DSA setAttendStd 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return null;
        } catch (Exception e) {
            log.error("DSA setAttendStd 예외. storeId: {}", store.getId(), e);
            return null;
        }
    }

    private String extractAttGn(DsaResponse response) {
        // extra 필드에서 먼저 확인
        Object attGn = response.getExtra().get("att_gn");
        if (attGn != null) {
            return attGn.toString().trim();
        }

        // data 리스트에서 확인
        if (response.getData() != null && !response.getData().isEmpty()) {
            Object dataAttGn = response.getData().get(0).get("att_gn");
            if (dataAttGn != null) {
                return dataAttGn.toString().trim();
            }
        }

        return null;
    }
}
