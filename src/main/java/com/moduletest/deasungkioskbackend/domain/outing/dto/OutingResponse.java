package com.moduletest.deasungkioskbackend.domain.outing.dto;

import com.moduletest.deasungkioskbackend.domain.outing.entity.Outing;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "외출 응답")
public record OutingResponse(
    @Schema(description = "외출 ID", example = "1")
    Long id,
    @Schema(description = "학생 ID", example = "3")
    Long studentId,
    @Schema(description = "학생 이름", example = "김민준")
    String studentName,
    @Schema(description = "외출 사유", example = "점심 식사")
    String reason,
    @Schema(description = "외출 시작 시간")
    LocalDateTime startedAt,
    @Schema(description = "외출 종료 시간 (복귀 전이면 null)")
    LocalDateTime endedAt
) {

    public static OutingResponse fromEntity(Outing outing) {
        return new OutingResponse(
            outing.getId(),
            outing.getStudent().getId(),
            outing.getStudent().getName(),
            outing.getReason(),
            outing.getStartedAt(),
            outing.getEndedAt()
        );
    }
}
