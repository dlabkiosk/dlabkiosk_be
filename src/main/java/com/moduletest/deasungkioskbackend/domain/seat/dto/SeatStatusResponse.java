package com.moduletest.deasungkioskbackend.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 현황 응답")
public record SeatStatusResponse(
    @Schema(description = "DSA 좌석 코드", example = "1122")
    String seatCd,
    @Schema(description = "좌석명", example = "A-1")
    String seatNm,
    @Schema(description = "X 좌표", example = "1")
    int xPos,
    @Schema(description = "Y 좌표", example = "1")
    int yPos,
    @Schema(description = "좌석 구분\n- Y: 사용 좌석\n- N: 미사용 좌석\n- E: 통로", example = "Y")
    String seatGn,
    @Schema(description = "좌석 상태\n- S: 등원\n- D: 외출\n- N: 미출석\n- B: 공석\n- A: 좌석이탈", example = "S")
    String state,
    @Schema(description = "좌석이탈 여부 (우리 시스템 기준)", example = "false")
    boolean away,
    @Schema(description = "배정 학생명", example = "홍길동")
    String studentName,
    @Schema(description = "배정 학생 학번", example = "20001")
    String studentNumber,
    @Schema(description = "이탈 사유명 (좌석이탈 시)", example = "화장실")
    String leaveReasonName
) {

}
