package com.moduletest.deasungkioskbackend.domain.studytime.service;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyTimeRedisService {

    private static final String DEDUCTION_KEY_PREFIX = "deduction:";
    private static final String OUTING_FIELD_PREFIX = "outing:";
    private static final String LEAVE_FIELD_PREFIX = "leave:";


    private final RedisTemplate<String, String> redisTemplate;


    /**
     * 외출 차감 시간 누적 (분 단위)
     */
    public void addOutingDeduction(Long storeId, Long studentId,
                                   LocalDateTime startedAt, LocalDateTime endedAt) {
        long minutes = calculateMinutes(startedAt, endedAt);
        if (minutes == 0) {
            return;
        }
        String key = buildKey(storeId);
        redisTemplate.opsForHash().increment(key, OUTING_FIELD_PREFIX + studentId, minutes);
        expireAtMidnight(key);
    }

    /**
     * 외출 차감 시간 롤백 (분 단위).
     * 기존 값이 롤백 분보다 작으면 0으로 설정한다.
     * 어드민이 완료된 외출을 취소/수정할 때 사용.
     */
    public void subtractOutingDeduction(Long storeId, Long studentId,
                                        LocalDateTime startedAt, LocalDateTime endedAt) {
        long minutes = calculateMinutes(startedAt, endedAt);
        if (minutes == 0) {
            return;
        }
        String key = buildKey(storeId);
        String field = OUTING_FIELD_PREFIX + studentId;
        long current = getFieldValue(key, field);
        long next = Math.max(current - minutes, 0L);
        redisTemplate.opsForHash().put(key, field, String.valueOf(next));
        expireAtMidnight(key);
    }

    /**
     * 좌석이탈 차감 시간 누적 (분 단위)
     */
    public void addSeatLeaveDeduction(Long storeId, Long studentId,
                                      LocalDateTime startedAt, LocalDateTime endedAt) {
        long minutes = calculateMinutes(startedAt, endedAt);
        if (minutes == 0) {
            return;
        }
        String key = buildKey(storeId);
        redisTemplate.opsForHash().increment(key, LEAVE_FIELD_PREFIX + studentId, minutes);
        expireAtMidnight(key);
    }

    private long calculateMinutes(LocalDateTime startedAt, LocalDateTime endedAt) {
        return Math.max(Duration.between(startedAt, endedAt).toMinutes(), 0L);
    }


    /**
     * 학생의 당일 총 차감 시간 조회 (분 단위)
     */
    public long getTotalDeduction(Long storeId, Long studentId) {
        String key = buildKey(storeId);
        long outingMinutes = getFieldValue(key, OUTING_FIELD_PREFIX + studentId);
        long leaveMinutes = getFieldValue(key, LEAVE_FIELD_PREFIX + studentId);
        return outingMinutes + leaveMinutes;
    }

    /**
     * 학생의 당일 외출/이탈 각각 조회 (분 단위)
     */
    public long getOutingDeduction(Long storeId, Long studentId) {
        return getFieldValue(buildKey(storeId), OUTING_FIELD_PREFIX + studentId);
    }

    public long getSeatLeaveDeduction(Long storeId, Long studentId) {
        return getFieldValue(buildKey(storeId), LEAVE_FIELD_PREFIX + studentId);
    }

    /**
     * 지점 전체 학생의 당일 차감 시간 조회 반환: Map<studentId, totalDeductionMinutes>
     */
    public Map<Long, Long> getAllDeductions(Long storeId) {
        String key = buildKey(storeId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        Map<Long, Long> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            long value = Long.parseLong(entry.getValue().toString());
            Long studentId = extractStudentId(field);
            if (studentId != null) {
                result.merge(studentId, value, Long::sum);
            }
        }
        return result;
    }

    private String buildKey(Long storeId) {
        return DEDUCTION_KEY_PREFIX + storeId + ":" + LocalDate.now();
    }

    private long getFieldValue(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private Long extractStudentId(String field) {
        String[] parts = field.split(":");
        if (parts.length == 2) {
            return Long.parseLong(parts[1]);
        }
        return null;
    }

    private void expireAtMidnight(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            LocalDateTime midnight = LocalDate.now()
                .plusDays(1)
                .atStartOfDay();
            long secondsUntilMidnight = Duration.between(LocalDateTime.now(), midnight)
                .getSeconds();
            redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }
}
