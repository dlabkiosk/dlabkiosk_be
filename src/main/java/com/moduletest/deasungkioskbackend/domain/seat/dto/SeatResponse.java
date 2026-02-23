package com.moduletest.deasungkioskbackend.domain.seat.dto;

import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 응답 DTO")
public record SeatResponse(

    @Schema(description = "좌석 ID", example = "1")
    Long id,
    @Schema(description = "지점 ID", example = "1")
    Long storeId,
    @Schema(description = "좌석 라벨", example = "A-1")
    String seatLabel,
    @Schema(description = "좌석 타입 (INDIVIDUAL / GROUP)", example = "INDIVIDUAL")
    String seatType,
    @Schema(description = "X 좌표 (픽셀)", example = "100")
    Integer xPos,
    @Schema(description = "Y 좌표 (픽셀)", example = "100")
    Integer yPos,
    @Schema(description = "활성 여부", example = "true")
    Boolean active

) {

    public static SeatResponse fromEntity(Seat seat) {
        return new SeatResponse(
            seat.getId(),
            seat.getStore().getId(),
            seat.getSeatLabel(),
            seat.getSeatType().name(),
            seat.getXPos(),
            seat.getYPos(),
            seat.isActive()
        );
    }

}
