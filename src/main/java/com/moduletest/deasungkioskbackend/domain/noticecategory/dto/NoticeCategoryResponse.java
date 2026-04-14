package com.moduletest.deasungkioskbackend.domain.noticecategory.dto;

import com.moduletest.deasungkioskbackend.domain.noticecategory.entity.NoticeCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지 말머리 응답")
public record NoticeCategoryResponse(
    @Schema(description = "말머리 ID", example = "1")
    Long id,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "말머리 이름", example = "긴급")
    String name,
    @Schema(description = "정렬 순서 (오름차순)", example = "1")
    int displayOrder
) {

    public static NoticeCategoryResponse fromEntity(NoticeCategory category) {
        return new NoticeCategoryResponse(
            category.getId(),
            category.getStore().getId(),
            category.getName(),
            category.getDisplayOrder()
        );
    }
}
