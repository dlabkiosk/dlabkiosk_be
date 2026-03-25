package com.moduletest.deasungkioskbackend.domain.tag.dto;

import com.moduletest.deasungkioskbackend.common.service.InputMethod;
import com.moduletest.deasungkioskbackend.domain.tag.entity.AttendAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "통합 태그 확인 요청. 외출/조퇴 등 사유신청이 있을 때 학생이 확인 후 호출한다.")
public record TagConfirmRequest(
    @Schema(description = "학생 식별값 (RFID UID 또는 QR UUID)", example = "A1B2C3D4")
    @NotBlank(message = "학생 식별값은 필수입니다")
    String identifier,

    @Schema(description = "입력 방식\n- RFID: 카드/QR\n- SEAT_LABEL: 좌석번호\n- PHONE_LAST4: 전화번호 뒷자리")
    InputMethod inputMethod,

    @Schema(description = "실행할 액션 (D: 외출, C: 조퇴)", example = "C")
    @NotNull(message = "액션은 필수입니다")
    AttendAction action
) {

}
