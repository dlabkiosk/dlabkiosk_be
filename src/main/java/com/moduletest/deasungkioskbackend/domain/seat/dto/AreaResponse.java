package com.moduletest.deasungkioskbackend.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구역(강의실) 응답")
public record AreaResponse(
    @Schema(description = "구역 코드", example = "111")
    String areaCd,
    @Schema(description = "구역명", example = "A구역")
    String areaNm
) {

}
