package com.moduletest.deasungkioskbackend.domain.studentmessage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "학생 메시지 일괄 삭제 요청")
public record StudentMessageBulkDeleteRequest(
    @Schema(description = "삭제할 메시지 ID 리스트", example = "[1, 2, 3]")
    @NotEmpty(message = "messageIds는 비어있을 수 없습니다")
    List<Long> messageIds
) {
}
