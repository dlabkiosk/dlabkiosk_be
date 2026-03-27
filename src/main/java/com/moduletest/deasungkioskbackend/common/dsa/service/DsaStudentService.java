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
                DsaStudentData data = DsaStudentData.fromMap(item);
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
}
