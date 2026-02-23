package com.moduletest.deasungkioskbackend.domain.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentUpdateRequest(
    @NotNull(message = "지점 ID는 필수 입력 항목입니다.")
    Long storeId,

    @NotBlank(message = "학생 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    String name,

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    String phone,

    @NotBlank(message = "학년은 필수 입력 항목입니다.")
    @Size(max = 20, message = "학년은 20자를 초과할 수 없습니다.")
    String grade
) {
}
