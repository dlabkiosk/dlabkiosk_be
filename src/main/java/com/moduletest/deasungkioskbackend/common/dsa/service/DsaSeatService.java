package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DsaSeatService {

    private static final String SET_SEAT_CHG_PROC_PATH = "/kiosk/setSeatChgProc";

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.23 setSeatChgProc 호출.
     * 학생의 좌석 변경을 DSA에 동기화한다.
     *
     * @return 성공 여부
     */
    public boolean sendSeatChange(String rfidUid, String seatCd, Store store) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 좌석 변경 DSA 동기화 스킵. storeId: {}",
                store.getId());
            return true;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("seat_cd", seatCd);

            DsaResponse response = dsaApiClient.post(
                SET_SEAT_CHG_PROC_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA setSeatChgProc 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return false;
            }

            log.info("DSA 좌석 변경 성공. rfidUid: {}, seatCd: {}, storeId: {}",
                rfidUid, seatCd, store.getId());
            return true;

        } catch (Exception e) {
            log.error("DSA setSeatChgProc 예외. storeId: {}", store.getId(), e);
            return false;
        }
    }
}
