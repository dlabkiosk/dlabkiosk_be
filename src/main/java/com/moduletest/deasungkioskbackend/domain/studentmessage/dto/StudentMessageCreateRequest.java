package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "학생별 메시지 생성 요청")
public record StudentMessageCreateRequest(
    @Schema(description = "학생 ID 목록", example = "[1, 2, 3]")
    @NotEmpty(message = "학생 ID는 최소 1개 이상 필요합니다.")
    List<Long> studentIds,

    @Schema(description = "메시지 내용", example = "식비가 미납입니다. 확인 부탁드립니다.")
    @NotBlank(message = "메시지 내용은 필수입니다.")
    String content
) {

}
