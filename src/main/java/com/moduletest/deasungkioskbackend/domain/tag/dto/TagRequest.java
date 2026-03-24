package com.moduletest.deasungkioskbackend.domain.tag.dto;

import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "통합 태그 요청. 학생증(RFID/QR) 태그 한 번으로 등원/하원/외출/복귀/조퇴를 자동 처리한다.")
public record TagRequest(
    @Schema(description = "학생 식별값 (RFID UID 또는 QR UUID)", example = "A1B2C3D4")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier,

    @Schema(description = "입력 방식\n- RFID: 카드/QR\n- SEAT_LABEL: 좌석번호\n- PHONE_LAST4: 전화번호 뒷자리")
    InputMethod inputMethod
) {

}
