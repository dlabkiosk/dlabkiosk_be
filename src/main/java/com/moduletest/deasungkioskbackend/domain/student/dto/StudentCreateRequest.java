package com.moduletest.deasungkioskbackend.domain.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "학생 등록 요청")
public record StudentCreateRequest(
    @Schema(description = "소속 지점 ID", example = "1")
    @NotNull(message = "지점 ID는 필수 입력 항목입니다.")
    Long storeId,

    @Schema(description = "학생 이름", example = "김대성")
    @NotBlank(message = "학생 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    String name,

    @Schema(description = "전화번호 (시스템 전체에서 중복 불가)", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    String phone,

    @Schema(description = "학년", example = "고3")
    @NotBlank(message = "학년은 필수 입력 항목입니다.")
    @Size(max = 20, message = "학년은 20자를 초과할 수 없습니다.")
    String grade,

    @Schema(description = "RFID 카드 UID (선택, 나중에 등록 가능)", example = "A1B2C3D4")
    @Size(max = 50, message = "RFID UID는 50자를 초과할 수 없습니다.")
    String rfidUid,

    @Schema(description = "학번 (선택, 나중에 등록 가능)", example = "2024-001")
    @Size(max = 30, message = "학번은 30자를 초과할 수 없습니다.")
    String studentNumber
) { }
