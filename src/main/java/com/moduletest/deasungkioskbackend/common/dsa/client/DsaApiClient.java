package com.moduletest.deasungkioskbackend.common.dsa.client;

import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaTokenService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class DsaApiClient {

    private final RestTemplate dsaRestTemplate;
    private final DsaTokenService dsaTokenService;

    public <T> T post(String path, Map<String, Object> params,
                      Class<T> responseType, Store store) {
        String token = dsaTokenService.getToken(store);
        params.put("token", token);

        try {
            return dsaRestTemplate.postForObject(path, params, responseType);
        } catch (DsaApiException e) {
            if (e.getDsaCode() == 910) {
                return retryWithRefreshedToken(path, params, responseType, store);
            }
            throw e;
        } catch (Exception e) {
            log.error("DSA API 호출 실패 - path: {}, storeId: {}", path, store.getId(), e);
            throw new DsaApiException(-1, "DSA API 호출 실패: " + e.getMessage());
        }
    }

    private <T> T retryWithRefreshedToken(String path, Map<String, Object> params,
                                          Class<T> responseType, Store store) {
        log.info("DSA 토큰 만료 - 갱신 후 재시도. storeId: {}", store.getId());
        try {
            String newToken = dsaTokenService.refreshToken(
                store.getId(), store.getDsaClientId());
            params.put("token", newToken);
            return dsaRestTemplate.postForObject(path, params, responseType);
        } catch (DsaApiException refreshEx) {
            log.warn("DSA 토큰 갱신 실패 - 재발급 시도. storeId: {}", store.getId());
            String newToken = dsaTokenService.requestNewToken(store);
            params.put("token", newToken);
            return dsaRestTemplate.postForObject(path, params, responseType);
        }
    }
}
