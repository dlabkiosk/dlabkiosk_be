package com.moduletest.deasungkioskbackend.domain.noticecategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "공지 말머리 순서 일괄 변경 요청")
public record NoticeCategoryOrderRequest(
    @Schema(description = "원하는 순서대로 정렬된 말머리 ID 목록", example = "[3, 1, 2]")
    @NotEmpty(message = "정렬할 말머리 ID 목록은 비어 있을 수 없습니다")
    List<Long> orderedIds
) { }
