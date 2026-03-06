package com.moduletest.deasungkioskbackend.domain.studentmessage.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.service.StudentMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 학생 공지 메시지",
    description = "태깅 시 학생에게 노출되는 메시지 조회 (KIOSK 인증 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/student-messages")
@RequiredArgsConstructor
public class StudentMessageKioskController {

    private final StudentMessageService studentMessageService;

    @Operation(summary = "학생 공지 메시지 조회",
        description = "태깅된 학생에게 등록된 활성 메시지를 전체 조회한다. "
            + "우선순위 없이 등록된 모든 문구가 노출된다.")
    @GetMapping
    public CommonResponse<List<StudentMessageResponse>> findActiveMessages(
        @RequestParam Long studentId) {
        return CommonResponse.success(
            studentMessageService.findAllActiveByStudentId(studentId));
    }
}
