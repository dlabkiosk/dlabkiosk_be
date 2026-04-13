package com.moduletest.deasungkioskbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 등록 요청 (ADMIN 전용)")
public record AdminUserCreateRequest(
    @Schema(description = "로그인 ID (4~50자)", example = "manager01")
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 50, message = "아이디는 4~50자여야 합니다.")
    String loginId,

    @Schema(description = "비밀번호 (8자 이상)", example = "password1234")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    String password,

    @Schema(description = "관리자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    String name,

    @Schema(description = "소속 지점 ID", example = "1")
    @NotNull(message = "지점 ID는 필수 입력 항목입니다.")
    Long storeId,

    @Schema(description = "역할 (MANAGER 또는 ADMIN)", example = "MANAGER")
    @NotBlank(message = "역할은 필수 입력 항목입니다.")
    String role
) {

}
