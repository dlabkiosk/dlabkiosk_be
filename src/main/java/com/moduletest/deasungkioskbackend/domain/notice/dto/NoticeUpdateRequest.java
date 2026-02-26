package com.moduletest.deasungkioskbackend.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지사항 수정 요청")
public record NoticeUpdateRequest(
    @Schema(description = "제목", example = "2월 운영시간 변경 안내 (수정)")
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    String title,

    @Schema(description = "내용", example = "수정된 내용입니다.")
    @NotBlank(message = "내용은 필수입니다")
    String content,

    @Schema(description = "상단 고정 여부", example = "false")
    Boolean pinned,

    @Schema(description = "활성화 여부", example = "true")
    Boolean active
) {

}
