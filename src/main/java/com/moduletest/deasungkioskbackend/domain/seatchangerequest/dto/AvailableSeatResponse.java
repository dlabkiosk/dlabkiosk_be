package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신청 가능한 좌석 응답")
public record AvailableSeatResponse(

    @Schema(description = "좌석 ID", example = "5")
    Long seatId,

    @Schema(description = "DSA 좌석 코드", example = "30343")
    String seatCd,

    @Schema(description = "좌석 라벨", example = "1")
    String seatLabel,

    @Schema(description = "좌석 타입", example = "INDIVIDUAL")
    String seatType,

    @Schema(description = "현재 배정된 학생 이름 (빈 좌석이면 null)", example = "김민준")
    String assignedStudentName,

    @Schema(description = "빈 좌석 여부", example = "true")
    boolean available
) {

    public static AvailableSeatResponse of(Seat seat, String assignedStudentName) {
        return new AvailableSeatResponse(
            seat.getId(),
            seat.getSeatCd(),
            seat.getSeatLabel(),
            seat.getSeatType().name(),
            assignedStudentName,
            assignedStudentName == null
        );
    }
}
