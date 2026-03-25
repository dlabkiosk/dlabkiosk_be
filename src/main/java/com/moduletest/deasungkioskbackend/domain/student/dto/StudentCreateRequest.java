package com.moduletest.deasungkioskbackend.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "학생 등록 요청")
public record StudentCreateRequest(
    @Schema(description = "학생 이름", example = "김대성")
    @NotBlank(message = "학생 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    String name,

    @Schema(description = "RFID UID (QR UUID 또는 RFID 카드 UID)", example = "A1B2C3D4")
    @NotBlank(message = "RFID UID는 필수 입력 항목입니다.")
    @Size(max = 50, message = "RFID UID는 50자를 초과할 수 없습니다.")
    String rfidUid,

    @Schema(description = "학번 (선택)", example = "2024-001")
    @Size(max = 30, message = "학번은 30자를 초과할 수 없습니다.")
    String studentNumber,

    @Schema(description = "전화번호 뒷자리 4자리 (선택)", example = "1234")
    @Size(max = 4, message = "전화번호 뒷자리는 4자를 초과할 수 없습니다.")
    String phoneLast4,

    @Schema(description = "배정 좌석 ID (선택)", example = "1")
    Long seatId
) { }
