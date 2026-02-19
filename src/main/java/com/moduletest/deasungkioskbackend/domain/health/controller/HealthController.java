package com.moduletest.deasungkioskbackend.domain.health.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Health", description = "서버 상태 확인")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 동작 중인지 확인합니다.")
    @GetMapping
    public CommonResponse<Map<String, Object>> checkHealth() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        );
        return CommonResponse.success(data);
    }
}
