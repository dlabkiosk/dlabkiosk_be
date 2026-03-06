package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메시지 템플릿 생성 요청")
public record MessageTemplateCreateRequest(
    @Schema(description = "지점 ID", example = "1")
    @NotNull(message = "지점 ID는 필수입니다.")
    Long storeId,

    @Schema(description = "템플릿 문구", example = "식비가 미납입니다. 확인 부탁드립니다.")
    @NotBlank(message = "문구 내용은 필수입니다.")
    String content
) {

}
