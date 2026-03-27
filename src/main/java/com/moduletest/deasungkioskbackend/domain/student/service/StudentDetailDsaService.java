package com.moduletest.deasungkioskbackend.domain.student.service;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaAttendanceSummary;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaMealApplication;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaPointRecord;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaReceiptRecord;
import java.time.LocalDate;
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
public final class StudentDetailDsaService {

    private final DsaApiClient dsaApiClient;

    /**
     * DSA 3.26 출결 특이사항 (결석/조퇴/외출 횟수).
     * 당월 기준 조회.
     */
    public DsaAttendanceSummary findAttendanceSummary(String rfidUid, Store store) {
        if (!store.hasDsaCredentials() || rfidUid == null) {
            return null;
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);

            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);
            params.put("st_dt", monthStart.toString());
            params.put("ed_dt", today.toString());

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getParentHpList", params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA 출결 특이사항 조회 실패 - code: {}", response.getCode());
                return null;
            }

            return DsaAttendanceSummary.builder()
                .absenceCount(parseIntSafe(response.getExtra().get("absence_cnt")))
                .earlyLeaveCount(parseIntSafe(response.getExtra().get("early_cnt")))
                .outingCount(parseIntSafe(response.getExtra().get("out_cnt")))
                .build();

        } catch (Exception e) {
            log.warn("DSA 출결 특이사항 조회 예외: {}", e.getMessage());
            return null;
        }
    }

    /**
     * DSA 3.28 상벌점 내역.
     * 당월 기준, 해당 학생 학번으로 필터.
     */
    public List<DsaPointRecord> findPoints(String studentNumber, Store store) {
        if (!store.hasDsaCredentials()) {
            return null;
        }

        try {
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);

            Map<String, Object> params = new HashMap<>();
            params.put("st_dt", monthStart.toString());
            params.put("ed_dt", today.toString());

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getPointStdList", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                log.warn("DSA 상벌점 조회 실패 - code: {}", response.getCode());
                return null;
            }

            List<DsaPointRecord> records = new ArrayList<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                String stdNo = getStringValue(item, "std_no");
                if (studentNumber != null && !studentNumber.equals(stdNo)) {
                    continue;
                }

                records.add(DsaPointRecord.builder()
                    .pointDate(getStringValue(item, "point_dt"))
                    .reason(getStringValue(item, "reason"))
                    .point(parseIntSafe(item.get("point")))
                    .build());
            }

            return records;
        } catch (Exception e) {
            log.warn("DSA 상벌점 조회 예외: {}", e.getMessage());
            return null;
        }
    }

    /**
     * DSA 3.29 수납 현황.
     * 학생별 rfid_no 기준 단건 조회.
     */
    public List<DsaReceiptRecord> findReceipts(String rfidUid, Store store) {
        if (!store.hasDsaCredentials() || rfidUid == null) {
            return null;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("rfid_no", rfidUid);

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getReceiptInfo", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                log.warn("DSA 수납 조회 실패 - code: {}", response.getCode());
                return null;
            }

            List<DsaReceiptRecord> records = new ArrayList<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                records.add(DsaReceiptRecord.builder()
                    .receiptName(getStringValue(item, "rcv_nm"))
                    .suppliedAmount(getStringValue(item, "supp_amt"))
                    .receivedAmount(getStringValue(item, "rec_amt"))
                    .unpaidAmount(getStringValue(item, "mi_amt"))
                    .build());
            }

            return records;
        } catch (Exception e) {
            log.warn("DSA 수납 조회 예외: {}", e.getMessage());
            return null;
        }
    }

    /**
     * DSA 3.30 급식 신청 내역.
     * 당월 기준, 해당 학생 학번으로 필터.
     */
    public List<DsaMealApplication> findMealApplications(String studentNumber, Store store) {
        if (!store.hasDsaCredentials()) {
            return null;
        }

        try {
            String month = LocalDate.now().toString().substring(0, 7);

            Map<String, Object> params = new HashMap<>();
            params.put("month", month);

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getMealApplyStdInfo", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                log.warn("DSA 급식 신청 조회 실패 - code: {}", response.getCode());
                return null;
            }

            List<DsaMealApplication> records = new ArrayList<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                String stdNo = getStringValue(item, "std_no");
                if (studentNumber != null && !studentNumber.equals(stdNo)) {
                    continue;
                }

                records.add(DsaMealApplication.builder()
                    .day(getStringValue(item, "day"))
                    .mealType(getStringValue(item, "meal_gb"))
                    .build());
            }

            return records;
        } catch (Exception e) {
            log.warn("DSA 급식 신청 조회 예외: {}", e.getMessage());
            return null;
        }
    }

    private int parseIntSafe(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}
