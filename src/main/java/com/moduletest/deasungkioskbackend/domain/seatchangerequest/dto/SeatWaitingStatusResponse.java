package com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "좌석별 대기 현황 응답")
public record SeatWaitingStatusResponse(

    @Schema(description = "좌석 ID", example = "1")
    Long seatId,

    @Schema(description = "좌석 라벨", example = "A-1")
    String seatLabel,

    @Schema(description = "좌석 타입", example = "INDIVIDUAL")
    String seatType,

    @Schema(description = "현재 배정된 학생 이름 (빈 좌석이면 null)", example = "김민준")
    String assignedStudentName,

    @Schema(description = "대기자 수", example = "2")
    int waitingCount,

    @Schema(description = "대기자 리스트 (신청 시간순)")
    List<WaitingStudent> waitingList
) {

    @Schema(description = "대기 학생 정보")
    public record WaitingStudent(

        @Schema(description = "신청 ID", example = "3")
        Long requestId,

        @Schema(description = "학생 이름", example = "이서영")
        String studentName,

        @Schema(description = "학번", example = "20250101")
        String studentNumber,

        @Schema(description = "반", example = "A반")
        String className,

        @Schema(description = "해당 좌석 신청 순위 (1~3)", example = "1")
        int priority,

        @Schema(description = "신청 시간")
        LocalDateTime createdAt
    ) {

    }
}
