package com.moduletest.deasungkioskbackend.domain.seat.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatRedisService {

    private static final String SEAT_STATUS_KEY_PREFIX = "seat:status:";
    private static final String AVAILABLE = "AVAILABLE";


    private final RedisTemplate<String, String> redisTemplate;


    public boolean tryOccupySeat(Long storeId, Long seatId, Long studentId, String studentName) {
        String key = buildKey(storeId);
        String field = seatId.toString();
        String value = "IN_USE:" + studentId + ":" + studentName;

        return redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }


    public void releaseSeat(Long storeId, Long seatId) {
        String key = buildKey(storeId);
        String field = seatId.toString();
        redisTemplate.opsForHash().delete(key, field);
    }

    public void setSeatAvailable(Long storeId, Long seatId) {
        String key = buildKey(storeId);
        String field = seatId.toString();
        redisTemplate.opsForHash().put(key, field, AVAILABLE);
    }

    public Map<Object, Object> getSeatStatusMap(Long storeId) {
        String key = buildKey(storeId);
        return redisTemplate.opsForHash()
            .entries(key);
    }

    public void markSeatOuting(Long storeId, Long seatId, Long studentId, String studentName) {
        String key = buildKey(storeId);
        String field = seatId.toString();
        String value = "OUTING:" + studentId + ":" + studentName;
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void markSeatInUse(Long storeId, Long seatId, Long studentId, String studentName) {
        String key = buildKey(storeId);
        String field = seatId.toString();
        String value = "IN_USE:" + studentId + ":" + studentName;
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void warmUpSeat(Long storeId, Long seatId, String value) {
        String key = buildKey(storeId);
        redisTemplate.opsForHash()
            .put(key, seatId.toString(), value);
    }


    private String buildKey(Long storeId) {
        return SEAT_STATUS_KEY_PREFIX + storeId;
    }

}
