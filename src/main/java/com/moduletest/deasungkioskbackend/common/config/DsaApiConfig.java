package com.moduletest.deasungkioskbackend.common.config;

import java.time.Duration;
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

    @Bean(name = "dsaRestTemplate")
    public RestTemplate dsaRestTemplate(RestTemplateBuilder builder) {
        return builder
            .rootUri(baseUrl)
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .readTimeout(Duration.ofMillis(readTimeout))
            .build();
    }
}
