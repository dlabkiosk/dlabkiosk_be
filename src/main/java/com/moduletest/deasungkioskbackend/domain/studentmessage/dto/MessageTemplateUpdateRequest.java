package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "메시지 템플릿 수정 요청")
public record MessageTemplateUpdateRequest(
    @Schema(description = "템플릿 문구", example = "식비가 미납입니다. 확인 부탁드립니다.")
    @NotBlank(message = "문구 내용은 필수입니다.")
    String content
) {

}
