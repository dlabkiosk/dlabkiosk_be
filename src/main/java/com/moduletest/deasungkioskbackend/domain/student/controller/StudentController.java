package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentKioskResponse;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 학생 조회", description = "학생 본인 정보 검색 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학생 검색",
        description = "카드/QR/학번/좌석번호/폰뒷자리 중 하나로 학생을 검색한다. "
            + "좌석 변경 신청 정보가 있으면 함께 반환한다.")
    @GetMapping("/search")
    public CommonResponse<StudentKioskResponse> searchStudent(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(required = false) String seatLabel,
            @RequestParam(required = false) String phoneLast4) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        StudentKioskResponse student = studentService.searchStudentForKiosk(
            identifier, studentNumber, seatLabel, phoneLast4, storeId);
        return CommonResponse.success(student);
    }
}
