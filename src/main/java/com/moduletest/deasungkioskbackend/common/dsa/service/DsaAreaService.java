package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.domain.seat.dto.AreaResponse;
import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public final class DsaAreaService {

    private static final String GET_STUDY_AREA_INFO_PATH = "/kiosk/getStudyAreaInfo";
    private static final String GET_STUDY_AREA_SEAT_INFO_PATH = "/kiosk/getStudyAreaSeatInfo";
    private static final String GET_STUDY_AREA_SEAT_STATE_PATH = "/kiosk/getStudyAreaSeatState";

    private final DsaApiClient dsaApiClient;
    private final RestTemplate dsaSeatRestTemplate;

    public DsaAreaService(
        DsaApiClient dsaApiClient,
        @Qualifier("dsaSeatRestTemplate") RestTemplate dsaSeatRestTemplate) {
        this.dsaApiClient = dsaApiClient;
        this.dsaSeatRestTemplate = dsaSeatRestTemplate;
    }

    /**
     * DSA 3.7 getStudyAreaInfo 호출.
     * 독서실 구역(강의실) 목록을 조회한다.
     */
    public List<AreaResponse> findAreas(Store store) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 구역 조회 불가. storeId: {}", store.getId());
            return List.of();
        }

        try {
            Map<String, Object> params = new HashMap<>();

            DsaResponse response = dsaApiClient.post(
                GET_STUDY_AREA_INFO_PATH, params, DsaResponse.class,
                store, dsaSeatRestTemplate);

            if (!response.isSuccess() || response.getData() == null) {
                log.warn("DSA getStudyAreaInfo 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return List.of();
            }

            List<AreaResponse> areas = new ArrayList<>();
            for (Map<String, Object> item : response.getData()) {
                String areaCd = String.valueOf(item.get("area_cd"));
                String areaNm = String.valueOf(item.get("area_nm"));
                areas.add(new AreaResponse(areaCd, areaNm));
            }

            log.info("DSA 구역 조회 완료: {}건. storeId: {}", areas.size(), store.getId());
            return areas;

        } catch (DsaApiException e) {
            log.error("DSA getStudyAreaInfo 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return List.of();
        } catch (Exception e) {
            log.error("DSA getStudyAreaInfo 예외. storeId: {}", store.getId(), e);
            return List.of();
        }
    }

    /**
     * DSA 3.8 getStudyAreaSeatInfo + 3.10 getStudyAreaSeatState 호출.
     * 구역별 좌석 레이아웃과 상태를 합쳐서 반환한다.
     */
    public List<SeatStatusResponse> findSeatStatusByArea(String areaCd, Store store) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 좌석 조회 불가. storeId: {}", store.getId());
            return List.of();
        }

        try {
            // 3.8 좌석 레이아웃
            Map<String, Object> seatParams = new HashMap<>();
            seatParams.put("area_cd", areaCd);

            DsaResponse seatResponse = dsaApiClient.post(
                GET_STUDY_AREA_SEAT_INFO_PATH, seatParams, DsaResponse.class,
                store, dsaSeatRestTemplate);

            if (!seatResponse.isSuccess() || seatResponse.getData() == null) {
                log.warn("DSA getStudyAreaSeatInfo 실패. storeId: {}", store.getId());
                return List.of();
            }

            // 3.10 좌석 상태
            Map<String, Object> stateParams = new HashMap<>();
            stateParams.put("area_cd", areaCd);

            DsaResponse stateResponse = dsaApiClient.post(
                GET_STUDY_AREA_SEAT_STATE_PATH, stateParams, DsaResponse.class,
                store, dsaSeatRestTemplate);

            Map<String, String> stateMap = new HashMap<>();
            if (stateResponse.isSuccess() && stateResponse.getData() != null) {
                for (Map<String, Object> item : stateResponse.getData()) {
                    stateMap.put(
                        String.valueOf(item.get("seat_cd")),
                        String.valueOf(item.get("state")));
                }
            }

            // 합치기
            List<SeatStatusResponse> result = new ArrayList<>();
            for (Map<String, Object> seat : seatResponse.getData()) {
                String seatCd = String.valueOf(seat.get("seat_cd"));
                String seatNm = seat.get("seat_nm") != null
                    ? String.valueOf(seat.get("seat_nm")) : "";
                int xPos = parseIntSafe(seat.get("xpos"), seat.get("x_pos"));
                int yPos = parseIntSafe(seat.get("ypos"), seat.get("y_pos"));
                String seatGn = seat.get("seat_gn") != null
                    ? String.valueOf(seat.get("seat_gn")) : "Y";
                String state = stateMap.getOrDefault(seatCd, "B");

                result.add(new SeatStatusResponse(
                    seatCd, seatNm, xPos, yPos, seatGn, state, false,
                    null, null));
            }

            log.info("DSA 좌석 조회 완료: {}건. areaCd: {}, storeId: {}",
                result.size(), areaCd, store.getId());
            return result;

        } catch (DsaApiException e) {
            log.error("DSA 좌석 조회 실패 - {}. storeId: {}", e.getMessage(), store.getId());
            return List.of();
        } catch (Exception e) {
            log.error("DSA 좌석 조회 예외. storeId: {}", store.getId(), e);
            return List.of();
        }
    }

    private int parseIntSafe(Object... values) {
        for (Object v : values) {
            if (v != null) {
                try {
                    return Integer.parseInt(String.valueOf(v));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        return 0;
    }
}
