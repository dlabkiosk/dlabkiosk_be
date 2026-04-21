package com.moduletest.deasungkioskbackend.common.config;

import com.moduletest.deasungkioskbackend.common.dsa.client.DsaApiClient;
import com.moduletest.deasungkioskbackend.common.dsa.service.DsaTokenService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DsaApiConfig {

    @Value("${dsa.api.base-url}")
    private String baseUrl;

    @Value("${dsa.api.connect-timeout}")
    private int connectTimeout;

    @Value("${dsa.api.read-timeout}")
    private int readTimeout;

    @Value("${dsa.api.sync.connect-timeout}")
    private int syncConnectTimeout;

    @Value("${dsa.api.sync.read-timeout}")
    private int syncReadTimeout;

    @Bean(name = "dsaRestTemplate")
    public RestTemplate dsaRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri(baseUrl)
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .readTimeout(Duration.ofMillis(readTimeout))
            .build();
    }

    @Bean(name = "dsaSyncRestTemplate")
    public RestTemplate dsaSyncRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri(baseUrl)
            .connectTimeout(Duration.ofMillis(syncConnectTimeout))
            .readTimeout(Duration.ofMillis(syncReadTimeout))
            .build();
    }

    @Bean(name = "dsaApiClient")
    public DsaApiClient dsaApiClient(
        @Qualifier("dsaRestTemplate") RestTemplate dsaRestTemplate,
        DsaTokenService dsaTokenService
    ) {
        return new DsaApiClient(dsaRestTemplate, dsaTokenService);
    }

    @Bean(name = "dsaSyncApiClient")
    public DsaApiClient dsaSyncApiClient(
        @Qualifier("dsaSyncRestTemplate") RestTemplate dsaSyncRestTemplate,
        DsaTokenService dsaTokenService
    ) {
        return new DsaApiClient(dsaSyncRestTemplate, dsaTokenService);
    }
}
