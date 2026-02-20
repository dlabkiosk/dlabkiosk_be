package com.moduletest.deasungkioskbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Schema(example = "test1234")
    @NotBlank(message = "아이디는 필수 입력 항목입니다.") String userId,
    @Schema(example = "test1234")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.") String password
) {}
