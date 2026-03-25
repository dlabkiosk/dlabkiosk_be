package com.moduletest.deasungkioskbackend.common.dsa.service;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaTokenRefreshRequest;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaTokenRequest;
import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaTokenResponse;
import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.common.util.Md5Util;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DsaTokenService {

    private static final String DSA_TOKEN_PREFIX = "dsa:token:";
    private static final String DSA_REFRESH_PREFIX = "dsa:refresh:";
    private static final long TOKEN_TTL_DAYS = 9;
    private static final long REFRESH_TTL_DAYS = 99;

    private final RestTemplate dsaRestTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public DsaTokenService(
        @Qualifier("dsaRestTemplate") RestTemplate dsaRestTemplate,
        RedisTemplate<String, String> redisTemplate) {
        this.dsaRestTemplate = dsaRestTemplate;
        this.redisTemplate = redisTemplate;
    }

    public String getToken(Store store) {
        String cachedToken = redisTemplate.opsForValue()
            .get(DSA_TOKEN_PREFIX + store.getId());
        if (cachedToken != null) {
            return cachedToken;
        }
        return requestNewToken(store);
    }

    public String requestNewToken(Store store) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String rawSecret = today + store.getDsaSecretId();
        String hashedSecret = Md5Util.md5(rawSecret);

        DsaTokenRequest request = DsaTokenRequest.of(
            store.getDsaAcadCd(), store.getDsaClientId(), hashedSecret);

        DsaTokenResponse response = dsaRestTemplate.postForObject(
            "/auth/token", request, DsaTokenResponse.class);

        if (response == null || !response.isSuccess()) {
            int code = response != null ? response.code() : -1;
            String msg = response != null ? response.message() : "응답 없음";
            throw new DsaApiException(code, msg);
        }

        saveTokensToRedis(store.getId(), response.token(), response.refreshToken());

        return response.token();
    }

    public String refreshToken(Long storeId, String clientId) {
        String refreshToken = redisTemplate.opsForValue()
            .get(DSA_REFRESH_PREFIX + storeId);

        if (refreshToken == null) {
            throw new DsaApiException(-1, "refreshToken 없음 - 재로그인 필요");
        }

        DsaTokenRefreshRequest request = new DsaTokenRefreshRequest(clientId, refreshToken);

        DsaTokenResponse response = dsaRestTemplate.postForObject(
            "/auth/refreshToken", request, DsaTokenResponse.class);

        if (response == null || !response.isSuccess()) {
            int code = response != null ? response.code() : -1;
            String msg = response != null ? response.message() : "응답 없음";
            throw new DsaApiException(code, msg);
        }

        redisTemplate.opsForValue().set(
            DSA_TOKEN_PREFIX + storeId, response.token(),
            TOKEN_TTL_DAYS, TimeUnit.DAYS);

        return response.token();
    }

    private void saveTokensToRedis(Long storeId, String token, String refreshToken) {
        redisTemplate.opsForValue().set(
            DSA_TOKEN_PREFIX + storeId, token,
            TOKEN_TTL_DAYS, TimeUnit.DAYS);
        if (refreshToken != null) {
            redisTemplate.opsForValue().set(
                DSA_REFRESH_PREFIX + storeId, refreshToken,
                REFRESH_TTL_DAYS, TimeUnit.DAYS);
        }
    }

    public void removeTokens(Long storeId) {
        redisTemplate.delete(DSA_TOKEN_PREFIX + storeId);
        redisTemplate.delete(DSA_REFRESH_PREFIX + storeId);
    }
}
