package com.moduletest.deasungkioskbackend.domain.seatleave.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "좌석이탈 사유 등록 요청")
public record SeatLeaveReasonCreateRequest(
    @Schema(description = "사유명", example = "화장실")
    @NotBlank(message = "사유명은 필수입니다.")
    String reasonName,

    @Schema(description = "정렬 순서", example = "1")
    int displayOrder,

    @Schema(description = "사용 여부", example = "true")
    boolean active
) { }
