package com.moduletest.deasungkioskbackend.domain.noticecategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지 말머리 생성 요청")
public record NoticeCategoryCreateRequest(
    @Schema(description = "말머리 이름", example = "긴급")
    @NotBlank(message = "말머리 이름은 필수입니다")
    @Size(max = 30, message = "말머리 이름은 30자를 초과할 수 없습니다")
    String name
) { }
