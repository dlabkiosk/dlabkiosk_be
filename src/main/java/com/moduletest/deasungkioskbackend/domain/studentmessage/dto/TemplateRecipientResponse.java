package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "템플릿 수신 학생 응답")
public record TemplateRecipientResponse(
    @Schema(description = "학생 메시지 ID", example = "1")
    Long messageId,
    @Schema(description = "학생 ID", example = "1")
    Long studentId,
    @Schema(description = "이름", example = "이민희")
    String name,
    @Schema(description = "학번", example = "20250001")
    String studentNumber,
    @Schema(description = "배정 좌석 번호 (없으면 null)", example = "A-12")
    String seatLabel,
    @Schema(description = "활성 여부", example = "true")
    boolean active,
    @Schema(description = "발송 일시")
    LocalDateTime createdAt
) {

    public static TemplateRecipientResponse fromEntity(StudentMessage message) {
        return new TemplateRecipientResponse(
            message.getId(),
            message.getStudent().getId(),
            message.getStudent().getName(),
            message.getStudent().getStudentNumber(),
            message.getStudent().getAssignedSeat() != null
                ? message.getStudent().getAssignedSeat().getSeatLabel() : null,
            message.isActive(),
            message.getCreatedAt()
        );
    }
}
