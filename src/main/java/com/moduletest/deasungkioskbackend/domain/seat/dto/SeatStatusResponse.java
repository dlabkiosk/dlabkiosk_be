package com.moduletest.deasungkioskbackend.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 현황 응답 (폴링용)")
public record SeatStatusResponse(
    @Schema(description = "좌석 ID", example = "1")
    Long seatId,
    @Schema(description = "좌석 라벨", example = "A-1")
    String seatLabel,
    @Schema(description = "좌석 타입", example = "INDIVIDUAL")
    String seatType,
    @Schema(description = "X 좌표 (픽셀)", example = "100")
    int xPos,
    @Schema(description = "Y 좌표 (픽셀)", example = "100")
    int yPos,
    @Schema(description = "사용 가능 여부", example = "true")
    boolean available,
    @Schema(description = "사용 중인 학생이 외출 중인지 여부", example = "false")
    boolean outing,
    @Schema(description = "사용 중인 학생 ID (비어있으면 null)", example = "3")
    Long studentId,
    @Schema(description = "사용 중인 학생 이름 (비어있으면 null)", example = "김민준")
    String studentName
) {

}
