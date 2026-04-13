package com.moduletest.deasungkioskbackend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "내 정보 수정 요청")
public record AdminUserUpdateMeRequest(
    @Schema(description = "변경할 이름 (null이면 변경 안 함)", example = "홍길동")
    String name,

    @Schema(description = "변경할 비밀번호 (null이면 변경 안 함)", example = "newPassword1234")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    String password
) {

}
