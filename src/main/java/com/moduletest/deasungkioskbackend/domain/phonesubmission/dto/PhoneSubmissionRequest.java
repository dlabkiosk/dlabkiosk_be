package com.moduletest.deasungkioskbackend.domain.phonesubmission.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "휴대폰 미소지 신청 요청")
public record PhoneSubmissionRequest(
    @Schema(description = "학생 식별값 (QR UUID 또는 RFID UID)",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier
) {

}
