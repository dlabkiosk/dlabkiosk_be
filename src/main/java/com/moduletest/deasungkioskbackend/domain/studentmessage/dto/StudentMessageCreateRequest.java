package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "학생별 메시지 생성 요청 (커스텀이면 content, 템플릿이면 templateId 중 하나만 필수)")
public record StudentMessageCreateRequest(
    @Schema(description = "학생 ID 목록", example = "[1, 2, 3]")
    @NotEmpty(message = "학생 ID는 최소 1개 이상 필요합니다.")
    List<Long> studentIds,

    @Schema(description = "커스텀 메시지 내용 (templateId 없을 때 필수)",
        example = "식비가 미납입니다. 확인 부탁드립니다.")
    String content,

    @Schema(description = "메시지 템플릿 ID (content 없을 때 필수). 같은 학생에게 같은 템플릿이 이미 등록되어 있으면 해당 학생은 무시한다.",
        example = "1")
    Long templateId
) {

}
