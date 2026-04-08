package com.moduletest.deasungkioskbackend.common.security;

import io.jsonwebtoken.JwtException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String ADMIN_TOKEN_PREFIX = "auth:admin:";
    private static final String ADMIN_REFRESH_PREFIX = "auth:refresh:";
    private static final String KIOSK_TOKEN_PREFIX = "auth:kiosk:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void saveAdminAccessToken(Long userId, String token, long expirationMillis) {
        String key = ADMIN_TOKEN_PREFIX + userId;
        evictExpiredTokens(key);
        redisTemplate.opsForSet().add(key, token);
        redisTemplate.expire(key, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public void saveAdminRefreshToken(Long userId, String token, long expirationMillis) {
        String key = ADMIN_REFRESH_PREFIX + userId;
        evictExpiredTokens(key);
        redisTemplate.opsForSet().add(key, token);
        redisTemplate.expire(key, expirationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Set 안의 만료된 JWT를 골라내 제거한다.
     * Set은 멤버 단위 TTL을 못 주므로, 저장 시점에 청소한다.
     */
    private void evictExpiredTokens(String key) {
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        Date now = new Date();
        Set<String> expired = new HashSet<>();
        for (String token : tokens) {
            try {
                Date exp = jwtTokenProvider.parseClaims(token).getExpiration();
                if (exp == null || exp.before(now)) {
                    expired.add(token);
                }
            } catch (JwtException | IllegalArgumentException e) {
                expired.add(token);
            }
        }
        if (!expired.isEmpty()) {
            redisTemplate.opsForSet().remove(key, expired.toArray());
            log.debug("expired token evicted - key: {}, count: {}", key, expired.size());
        }
    }

    public void saveKioskToken(Long storeId, String token, long expirationMillis) {
        String key = KIOSK_TOKEN_PREFIX + storeId;
        redisTemplate.opsForSet().add(key, token);
        redisTemplate.expire(key, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAdminTokenValid(Long userId, String token) {
        String key = ADMIN_TOKEN_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, token));
    }

    public boolean isRefreshTokenValid(Long userId, String token) {
        String key = ADMIN_REFRESH_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, token));
    }

    public boolean isKioskTokenValid(Long storeId, String token) {
        String key = KIOSK_TOKEN_PREFIX + storeId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, token));
    }

    public void removeAdminToken(Long userId, String token) {
        redisTemplate.opsForSet().remove(ADMIN_TOKEN_PREFIX + userId, token);
    }

    public void removeAdminRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForSet().remove(ADMIN_REFRESH_PREFIX + userId, refreshToken);
    }

    public void removeAllAdminTokens(Long userId) {
        redisTemplate.delete(ADMIN_TOKEN_PREFIX + userId);
        redisTemplate.delete(ADMIN_REFRESH_PREFIX + userId);
    }

    public void removeKioskToken(Long storeId, String token) {
        String key = KIOSK_TOKEN_PREFIX + storeId;
        redisTemplate.opsForSet().remove(key, token);
    }

    public void removeAllKioskTokens(Long storeId) {
        redisTemplate.delete(KIOSK_TOKEN_PREFIX + storeId);
    }
}
