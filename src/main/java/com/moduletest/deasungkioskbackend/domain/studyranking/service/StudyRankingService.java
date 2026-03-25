package com.moduletest.deasungkioskbackend.domain.studyranking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import com.moduletest.deasungkioskbackend.common.dsa.dto.StudyRankingResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.store.exception.StoreException;
import com.moduletest.deasungkioskbackend.domain.store.repository.StoreRepository;
import com.moduletest.deasungkioskbackend.domain.studyranking.dto.AllStoreRankingResponse;
import com.moduletest.deasungkioskbackend.domain.studyranking.dto.AllStoreRankingResponse.RankingEntry;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRankingService {

    private static final String STORE_RANKING_KEY = "study-ranking:store:";
    private static final String ALL_RANKING_KEY = "study-ranking:all";
    private static final Pattern STUDY_TIME_PATTERN = Pattern.compile("(\\d+)시간\\s*(\\d+)분");

    private final DsaApiClient dsaApiClient;
    private final StoreRepository storeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public StudyRankingResponse getStudyRanking(Long storeId) {
        String cacheKey = STORE_RANKING_KEY + storeId;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StudyRankingResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("Redis 캐시 역직렬화 실패 (key={})", cacheKey, e);
            }
        }

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        StudyRankingResponse response = StudyRankingResponse.builder()
            .firstPlace(callDsaSafe("/kiosk/getFirstLastWeekStudyTimeStd", store))
            .averageStudyTime(callDsaSafe("/kiosk/getAvgLastWeekStudyTime", store))
            .rankingList(callDsaSafe("/kiosk/getLastWeekStudyTimeList", store))
            .build();

        cacheToRedis(cacheKey, response);
        return response;
    }

    public AllStoreRankingResponse getAllStoreRanking() {
        String cached = redisTemplate.opsForValue().get(ALL_RANKING_KEY);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AllStoreRankingResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("Redis 캐시 역직렬화 실패 (key={})", ALL_RANKING_KEY, e);
            }
        }

        List<Store> stores = storeRepository.findAll();
        List<RankingEntry> allEntries = new ArrayList<>();

        for (Store store : stores) {
            if (!store.hasDsaCredentials()) {
                continue;
            }

            DsaResponse rankingList = callDsaSafe(
                "/kiosk/getLastWeekStudyTimeList", store);

            if (rankingList == null || rankingList.getData() == null) {
                continue;
            }

            for (Map<String, Object> item : rankingList.getData()) {
                String studentName = String.valueOf(item.getOrDefault("std_nm", ""));
                String studyTime = String.valueOf(item.getOrDefault("att_tm", "0시간 0분"));

                allEntries.add(RankingEntry.builder()
                    .studentName(studentName)
                    .storeName(store.getStoreName())
                    .studyTime(studyTime)
                    .studyTimeMinutes(parseStudyTimeToMinutes(studyTime))
                    .build());
            }
        }

        allEntries.sort(Comparator.comparingLong(RankingEntry::studyTimeMinutes).reversed());

        List<RankingEntry> ranked = new ArrayList<>();
        int limit = Math.min(allEntries.size(), 10);
        for (int i = 0; i < limit; i++) {
            RankingEntry entry = allEntries.get(i);
            ranked.add(RankingEntry.builder()
                .rank(i + 1)
                .studentName(entry.studentName())
                .storeName(entry.storeName())
                .studyTime(entry.studyTime())
                .studyTimeMinutes(entry.studyTimeMinutes())
                .build());
        }

        AllStoreRankingResponse response = AllStoreRankingResponse.builder()
            .rankings(ranked)
            .build();

        cacheToRedis(ALL_RANKING_KEY, response);
        return response;
    }

    private long parseStudyTimeToMinutes(String studyTime) {
        Matcher matcher = STUDY_TIME_PATTERN.matcher(studyTime);
        if (matcher.find()) {
            long hours = Long.parseLong(matcher.group(1));
            long minutes = Long.parseLong(matcher.group(2));
            return hours * 60 + minutes;
        }
        return 0;
    }

    private void cacheToRedis(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            long ttlSeconds = calculateTtlUntilNextMonday();
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("Redis 캐시 직렬화 실패 (key={})", key, e);
        }
    }

    private long calculateTtlUntilNextMonday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.toLocalDate()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay();
        return Duration.between(now, nextMonday).getSeconds();
    }

    private DsaResponse callDsaSafe(String path, Store store) {
        try {
            return dsaApiClient.post(path, buildParams(), DsaResponse.class, store);
        } catch (Exception e) {
            log.warn("DSA API 호출 실패 [{}]: {}", path, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildParams() {
        return new HashMap<>();
    }
}
