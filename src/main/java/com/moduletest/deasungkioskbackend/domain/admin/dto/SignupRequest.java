package com.moduletest.deasungkioskbackend.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 50, message = "아이디는 4~50자여야 합니다.")
    String loginId,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    String password,

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    String name
) {}