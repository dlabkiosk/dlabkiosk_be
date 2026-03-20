package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "학생별 메시지 응답")
public record StudentMessageResponse(
    @Schema(description = "메시지 ID", example = "1")
    Long id,
    @Schema(description = "학생 ID", example = "1")
    Long studentId,
    @Schema(description = "학생 이름", example = "이민희")
    String studentName,
    @Schema(description = "메시지 내용", example = "식비가 미납입니다.")
    String content,
    @Schema(description = "활성 여부", example = "true")
    boolean active,
    @Schema(description = "생성일시")
    LocalDateTime createdAt
) {

    public static StudentMessageResponse fromEntity(StudentMessage message) {
        return new StudentMessageResponse(
            message.getId(),
            message.getStudent().getId(),
            message.getStudent().getName(),
            message.getContent(),
            message.isActive(),
            message.getCreatedAt()
        );
    }
}
