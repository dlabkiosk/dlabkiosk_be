package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaStudentData;
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
public final class DsaStudentService {

    private static final String GET_STD_INFO_LIST_PATH = "/kiosk/getStdInfoList";
    private static final String GET_STD_INFO_PATH = "/kiosk/getStdInfo";

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.20 getStdInfoList 호출.
     * 지점의 전체 학생 목록을 조회한다.
     */
    public List<DsaStudentData> findAllStudents(Store store) {
        if (!store.hasDsaCredentials()) {
            log.debug("DSA 인증정보 없음 - 학생 조회 불가. storeId: {}", store.getId());
            return List.of();
        }

        try {
            Map<String, Object> params = new HashMap<>();

            DsaResponse response = dsaApiClient.post(
                GET_STD_INFO_LIST_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getData() == null) {
                log.warn("DSA getStdInfoList 실패 - code: {}, message: {}. storeId: {}",
                    response.getCode(), response.getMessage(), store.getId());
                return List.of();
            }

            List<DsaStudentData> students = new ArrayList<>();
            for (Map<String, Object> item : response.getData()) {
                log.debug("DSA 학생 원본 데이터: {}", item);
                DsaStudentData data = DsaStudentData.fromMap(item);
                log.info("DSA 학생 파싱 결과 - name: {}, seatCd: {}", data.stdNm(), data.seatCd());
                if (data.rfidNo() != null && data.stdNm() != null) {
                    students.add(data);
                } else {
                    log.warn("DSA 학생 데이터 누락 (rfidNo 또는 stdNm 없음): {}. storeId: {}",
                        item, store.getId());
                }
            }

            log.info("DSA 학생 조회 완료: {}건. storeId: {}", students.size(), store.getId());
            return students;

        } catch (DsaApiException e) {
            log.error("DSA getStdInfoList 호출 실패 - {}. storeId: {}",
                e.getMessage(), store.getId());
            return List.of();
        } catch (Exception e) {
            log.error("DSA getStdInfoList 예외. storeId: {}", store.getId(), e);
            return List.of();
        }
    }

    /**
     * DSA 3.24 getStdInfo 호출.
     * rfidNo로 학생 상세(전화번호 포함)를 조회한다.
     * p_hp 필드에서 전화번호 뒷 8자리를 추출하여 반환한다.
     */
    public String findStudentPhone(String rfidNo, Store store) {
        if (!store.hasDsaCredentials() || rfidNo == null) {
            return null;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidNo);

            DsaResponse response = dsaApiClient.post(
                GET_STD_INFO_PATH, params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getData() == null
                || response.getData().isEmpty()) {
                return null;
            }

            Map<String, Object> data = response.getData().get(0);
            Object hp = data.get("hp");
            if (hp == null) {
                hp = data.get("p_hp");
            }
            if (hp == null) {
                return null;
            }
            String phone = String.valueOf(hp).replaceAll("[^0-9]", "");
            if (phone.length() >= 8) {
                return phone.substring(phone.length() - 8);
            }
            return phone.isEmpty() ? null : phone;

        } catch (Exception e) {
            log.warn("DSA getStdInfo 전화번호 조회 실패 - rfidNo: {}", rfidNo, e);
            return null;
        }
    }
}
