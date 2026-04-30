package com.moduletest.deasungkioskbackend.domain.tag.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagConfirmRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagRequest;
import com.moduletest.deasungkioskbackend.domain.tag.dto.TagResponse;
import com.moduletest.deasungkioskbackend.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 통합 태그",
    description = "학생증 태그 한 번으로 등원/하원/외출/복귀/조퇴/급식 자동 처리")
@RestController
@RequestMapping("/api/v1/kiosk/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "통합 태그",
        description = "학생증(RFID/QR)을 태그하면 상태 기반으로 출결 유형을 자동 판별한다. "
            + "식사시간(점심 12:10~13:10, 저녁 18:00~19:00)에는 급식 정보도 함께 반환한다.\n\n"
            + "inputMethod 입력 방식:\n"
            + "- RFID: 카드/QR UID로 학생 식별\n"
            + "- PHONE: 전화번호 8자리로 학생 식별\n"
            + "- PHONE_LAST4: 전화번호 뒷자리 4자리로 학생 식별\n"
            + "- 미입력 시: RFID → 전화번호 8자리(PHONE) 순서로 자동 판별\n\n"
            + "응답 AttendAction 액션 타입:\n"
            + "- S: 등원\n"
            + "- T: 하원\n"
            + "- A: 지각\n"
            + "- D: 외출\n"
            + "- N: 사유외출\n"
            + "- C: 조퇴\n"
            + "- R: 복귀")
    @PostMapping
    public CommonResponse<TagResponse> processTag(
        @Valid @RequestBody TagRequest request) {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        return CommonResponse.success(tagService.processTag(request, storeId));
    }

    @Operation(summary = "출결 확인",
        description = "pendingActions에서 학생이 선택한 액션으로 호출한다. "
            + "D/N(외출/사유외출), C(조퇴), T(하원, 자율등원일·공휴일 등에서 사용자가 명시 선택) 허용.\n\n"
            + "inputMethod 입력 방식:\n"
            + "- RFID: 카드/QR UID로 학생 식별\n"
            + "- PHONE: 전화번호 8자리로 학생 식별\n"
            + "- PHONE_LAST4: 전화번호 뒷자리 4자리로 학생 식별\n"
            + "- 미입력 시: RFID → 전화번호 8자리(PHONE) 순서로 자동 판별\n\n"
            + "action 값:\n"
            + "- D: 외출\n"
            + "- N: 사유외출\n"
            + "- C: 조퇴\n"
            + "- T: 하원")
    @PostMapping("/confirm")
    public CommonResponse<TagResponse> confirmTag(
        @Valid @RequestBody TagConfirmRequest request) {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        return CommonResponse.success(tagService.confirmTag(request, storeId));
    }

    @Operation(summary = "급식 태그 확인",
        description = "식사시간에 급식 태그를 확인한다. "
            + "신청 내역이 없으면 안내 메시지, 있으면 태그 완료 처리. "
            + "[주의] DSA 급식 신청 조회(3.30) 아직 미연동 — 현재 DSA 인증정보 없으면 전부 신청한 것으로 처리됨.\n\n"
            + "inputMethod 입력 방식:\n"
            + "- RFID: 카드/QR UID로 학생 식별\n"
            + "- PHONE: 전화번호 8자리로 학생 식별\n"
            + "- PHONE_LAST4: 전화번호 뒷자리 4자리로 학생 식별\n"
            + "- 미입력 시: RFID → 전화번호 8자리(PHONE) 순서로 자동 판별")
    @PostMapping("/meal-confirm")
    public CommonResponse<TagResponse> confirmMealTag(
        @Valid @RequestBody TagRequest request) {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        return CommonResponse.success(
            tagService.confirmMealTag(
                request.identifier(), storeId, request.inputMethod()));
    }

}
