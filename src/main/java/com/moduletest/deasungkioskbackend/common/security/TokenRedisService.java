package com.moduletest.deasungkioskbackend.common.security;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String ADMIN_TOKEN_PREFIX = "auth:admin:";
    private static final String ADMIN_REFRESH_PREFIX = "auth:refresh:";
    private static final String KIOSK_TOKEN_PREFIX = "auth:kiosk:";

    private final RedisTemplate<String, String> redisTemplate;

    public void saveAdminAccessToken(Long userId, String token, long expirationMillis) {
        String key = ADMIN_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public void saveAdminRefreshToken(Long userId, String token, long expirationMillis) {
        String key = ADMIN_REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public void saveKioskToken(Long storeId, String token, long expirationMillis) {
        String key = KIOSK_TOKEN_PREFIX + storeId;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAdminTokenValid(Long userId, String token) {
        String key = ADMIN_TOKEN_PREFIX + userId;
        String stored = redisTemplate.opsForValue().get(key);
        return token.equals(stored);
    }

    public boolean isKioskTokenValid(Long storeId, String token) {
        String key = KIOSK_TOKEN_PREFIX + storeId;
        String stored = redisTemplate.opsForValue().get(key);
        return token.equals(stored);
    }

    public void removeAdminTokens(Long userId) {
        redisTemplate.delete(ADMIN_TOKEN_PREFIX + userId);
        redisTemplate.delete(ADMIN_REFRESH_PREFIX + userId);
    }

    public void removeKioskToken(Long storeId) {
        redisTemplate.delete(KIOSK_TOKEN_PREFIX + storeId);
    }
}
