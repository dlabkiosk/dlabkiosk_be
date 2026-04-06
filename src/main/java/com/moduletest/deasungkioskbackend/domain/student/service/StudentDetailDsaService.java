package com.moduletest.deasungkioskbackend.domain.student.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaAttendanceSummary;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaMealApplication;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaPointRecord;
import com.moduletest.deasungkioskbackend.domain.student.dto.DsaReceiptRecord;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class StudentDetailDsaService {

    private static final String MEAL_CACHE_KEY = "dsa:meal:";
    private static final String POINT_CACHE_KEY = "dsa:point:";
    private static final long MEAL_CACHE_TTL_MINUTES = 30;
    private static final long POINT_CACHE_TTL_MINUTES = 10;

    private final DsaApiClient dsaApiClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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
                "/kiosk/getAttendState", params, DsaResponse.class, store);

            if (!response.isSuccess()) {
                log.warn("DSA 출결 특이사항 조회 실패 - code: {}", response.getCode());
                return null;
            }

            Map<String, Object> extra = response.getExtra();
            if (extra == null) {
                log.warn("DSA 출결 특이사항 응답에 extra 없음. storeId: {}", store.getId());
                return null;
            }

            return DsaAttendanceSummary.builder()
                .absenceCount(parseIntSafe(extra.get("absence_cnt")))
                .earlyLeaveCount(parseIntSafe(extra.get("early_cnt")))
                .outingCount(parseIntSafe(extra.get("out_cnt")))
                .build();

        } catch (Exception e) {
            log.warn("DSA 출결 특이사항 조회 예외: {}", e.getMessage());
            return null;
        }
    }

    /**
     * DSA 3.28 상벌점 내역.
     * 당월 기준, Redis 캐시 우선 조회 → 미스 시 DSA 전체 조회 후 학생별 캐싱.
     */
    public List<DsaPointRecord> findPoints(String studentNumber, Store store) {
        if (!store.hasDsaCredentials() || studentNumber == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        String month = today.toString().substring(0, 7);
        String cacheKey = POINT_CACHE_KEY + store.getId() + ":" + month + ":" + studentNumber;

        List<DsaPointRecord> cached = readFromCache(
            cacheKey, new TypeReference<List<DsaPointRecord>>() { });
        if (cached != null) {
            return cached;
        }

        try {
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

            Map<String, List<DsaPointRecord>> byStudent = new HashMap<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                String stdNo = getStringValue(item, "std_no");
                if (stdNo == null) {
                    continue;
                }
                byStudent.computeIfAbsent(stdNo, k -> new ArrayList<>())
                    .add(DsaPointRecord.builder()
                        .pointDate(getStringValue(item, "point_dt"))
                        .reason(getStringValue(item, "reason"))
                        .point(parseIntSafe(item.get("point")))
                        .build());
            }

            String keyPrefix = POINT_CACHE_KEY + store.getId() + ":" + month + ":";
            for (Map.Entry<String, List<DsaPointRecord>> entry : byStudent.entrySet()) {
                writeToCache(keyPrefix + entry.getKey(), entry.getValue(), POINT_CACHE_TTL_MINUTES);
            }

            return byStudent.getOrDefault(studentNumber, Collections.emptyList());
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
     * 당월 기준, Redis 캐시 우선 조회 → 미스 시 DSA 전체 조회 후 학생별 캐싱.
     */
    public List<DsaMealApplication> findMealApplications(String studentNumber, Store store) {
        if (!store.hasDsaCredentials() || studentNumber == null) {
            return null;
        }

        String month = LocalDate.now().toString().substring(0, 7);
        String cacheKey = MEAL_CACHE_KEY + store.getId() + ":" + month + ":" + studentNumber;

        List<DsaMealApplication> cached = readFromCache(
            cacheKey, new TypeReference<List<DsaMealApplication>>() { });
        if (cached != null) {
            return cached;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("month", month);

            DsaResponse response = dsaApiClient.post(
                "/kiosk/getMealApplyStdInfo", params, DsaResponse.class, store);

            if (!response.isSuccess() || response.getDataAsList() == null) {
                log.warn("DSA 급식 신청 조회 실패 - code: {}", response.getCode());
                return null;
            }

            Map<String, List<DsaMealApplication>> byStudent = new HashMap<>();
            for (Map<String, Object> item : response.getDataAsList()) {
                String stdNo = getStringValue(item, "std_no");
                if (stdNo == null) {
                    continue;
                }
                byStudent.computeIfAbsent(stdNo, k -> new ArrayList<>())
                    .add(DsaMealApplication.builder()
                        .day(getStringValue(item, "day"))
                        .mealType(getStringValue(item, "meal_gb"))
                        .build());
            }

            String keyPrefix = MEAL_CACHE_KEY + store.getId() + ":" + month + ":";
            for (Map.Entry<String, List<DsaMealApplication>> entry : byStudent.entrySet()) {
                writeToCache(keyPrefix + entry.getKey(), entry.getValue(), MEAL_CACHE_TTL_MINUTES);
            }

            return byStudent.getOrDefault(studentNumber, Collections.emptyList());
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

    private <T> T readFromCache(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, typeRef);
            }
        } catch (JsonProcessingException e) {
            log.warn("Redis 캐시 역직렬화 실패 (key: {}): {}", key, e.getMessage());
        }
        return null;
    }

    private void writeToCache(String key, Object value, long ttlMinutes) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("Redis 캐시 직렬화 실패 (key: {}): {}", key, e.getMessage());
        }
    }
}
