package com.moduletest.deasungkioskbackend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

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
}
