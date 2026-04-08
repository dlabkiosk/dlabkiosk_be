package com.moduletest.deasungkioskbackend.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "학생 QR 일괄 다운로드 요청")
public record StudentQrBulkRequest(
    @Schema(description = "학생 ID 리스트", example = "[1, 2, 3]")
    @NotEmpty(message = "studentIds는 비어있을 수 없습니다")
    List<Long> studentIds
) {
}
