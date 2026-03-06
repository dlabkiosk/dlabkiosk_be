package com.moduletest.deasungkioskbackend.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "좌석 생성 요청 DTO")
public record SeatCreateRequest(
    @Schema(description = "좌석 라벨", example = "A-1")
    @NotBlank(message = "좌석 라벨은 필수 입력 항목입니다.")
    @Size(max = 20, message = "좌석 라벨은 20자를 초과할 수 없습니다.")
    String seatLabel,

    @Schema(description = "좌석 타입 (INDIVIDUAL / GROUP)", example = "INDIVIDUAL")
    @NotBlank(message = "좌석 타입은 필수 입력 항목입니다.")
    String seatType,

    @Schema(description = "X 좌표 (픽셀)", example = "100")
    @NotNull(message = "X 좌표는 필수 입력 항목입니다.")
    Integer xPos,

    @Schema(description = "Y 좌표 (픽셀)", example = "100")
    @NotNull(message = "Y 좌표는 필수 입력 항목입니다.")
    Integer yPos
) { }
