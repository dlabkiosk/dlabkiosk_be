package com.moduletest.deasungkioskbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 로그인 요청")
public record LoginRequest(
    @Schema(description = "관리자 로그인 ID", example = "test1234")
    @NotBlank(message = "아이디는 필수 입력 항목입니다.") String userId,
    @Schema(description = "비밀번호", example = "test1234")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.") String password
) { }
