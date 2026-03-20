package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import com.moduletest.deasungkioskbackend.domain.seat.dto.SeatStatusResponse;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "구역별 좌석 목록 응답")
public record AreaAvailableSeatResponse(

    @Schema(description = "구역 코드", example = "111")
    String areaCd,

    @Schema(description = "구역명", example = "A구역")
    String areaNm,

    @Schema(description = "해당 구역의 좌석 목록")
    List<SeatInfo> seats
) {

    @Schema(description = "좌석 정보")
    public record SeatInfo(

        @Schema(description = "좌석 ID (우리 DB)", example = "5")
        Long seatId,

        @Schema(description = "DSA 좌석 코드", example = "1122")
        String seatCd,

        @Schema(description = "좌석명", example = "A-1")
        String seatNm,

        @Schema(description = "X 좌표", example = "1")
        int xPos,

        @Schema(description = "Y 좌표", example = "1")
        int yPos,

        @Schema(description = "좌석 구분\n- Y: 사용 좌석\n- N: 미사용 좌석\n- E: 통로", example = "Y")
        String seatGn
    ) {

        public static SeatInfo of(Seat seat, SeatStatusResponse dsaSeat) {
            return new SeatInfo(
                seat.getId(),
                dsaSeat.seatCd(),
                dsaSeat.seatNm(),
                dsaSeat.xPos(),
                dsaSeat.yPos(),
                dsaSeat.seatGn()
            );
        }
    }
}
