package com.moduletest.deasungkioskbackend.common.dsa.client;

import com.moduletest.deasungkioskbackend.common.dsa.exception.DsaApiException;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaTokenService;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class DsaApiClient {

    private final RestTemplate dsaRestTemplate;
    private final DsaTokenService dsaTokenService;

    public DsaApiClient(
        @Qualifier("dsaRestTemplate") RestTemplate dsaRestTemplate,
        DsaTokenService dsaTokenService) {
        this.dsaRestTemplate = dsaRestTemplate;
        this.dsaTokenService = dsaTokenService;
    }

    public <T> T post(String path, Map<String, Object> params,
                      Class<T> responseType, Store store) {
        return post(path, params, responseType, store, dsaRestTemplate);
    }

    public <T> T post(String path, Map<String, Object> params,
                      Class<T> responseType, Store store, RestTemplate restTemplate) {
        String token = dsaTokenService.getToken(store);
        params.put("token", token);

        try {
            return restTemplate.postForObject(path, params, responseType);
        } catch (DsaApiException e) {
            if (e.getDsaCode() == 910) {
                return retryWithRefreshedToken(
                    path, params, responseType, store, restTemplate);
            }
            throw e;
        } catch (Exception e) {
            log.error("DSA API 호출 실패 - path: {}, storeId: {}", path, store.getId(), e);
            throw new DsaApiException(-1, "DSA API 호출 실패: " + e.getMessage());
        }
    }

    private <T> T retryWithRefreshedToken(String path, Map<String, Object> params,
                                          Class<T> responseType, Store store,
                                          RestTemplate restTemplate) {
        log.info("DSA 토큰 만료 - 갱신 후 재시도. storeId: {}", store.getId());
        try {
            String newToken = dsaTokenService.refreshToken(
                store.getId(), store.getDsaClientId());
            params.put("token", newToken);
            return restTemplate.postForObject(path, params, responseType);
        } catch (DsaApiException refreshEx) {
            log.warn("DSA 토큰 갱신 실패 - 재발급 시도. storeId: {}", store.getId());
            String newToken = dsaTokenService.requestNewToken(store);
            params.put("token", newToken);
            return restTemplate.postForObject(path, params, responseType);
        }
    }
}
