package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "학생별 메시지 생성 요청")
public record StudentMessageCreateRequest(
    @Schema(description = "학생 ID", example = "1")
    @NotNull(message = "학생 ID는 필수입니다.")
    Long studentId,

    @Schema(description = "메시지 내용", example = "식비가 미납입니다. 확인 부탁드립니다.")
    @NotBlank(message = "메시지 내용은 필수입니다.")
    String content
) {

}
