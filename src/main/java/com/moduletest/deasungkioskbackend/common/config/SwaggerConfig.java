package com.moduletest.deasungkioskbackend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
            new Info()
                .title("대성 키오스크 API")
                .version("v1")
                .description("대성 키오스크 백엔드 API")
        );
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("1. 관리자 API")
            .pathsToMatch("/api/v1/admin/**", "/api/admin/**")
            .build();
    }

    @Bean
    public GroupedOpenApi kioskApi() {
        return GroupedOpenApi.builder()
            .group("2. 키오스크 API")
            .pathsToMatch("/api/v1/kiosk/**", "/api/v1/stores/**")
            .build();
    }
}
