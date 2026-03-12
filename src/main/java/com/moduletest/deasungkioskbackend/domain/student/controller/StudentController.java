package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentKioskResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
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

    @Operation(summary = "학생 검색 (키오스크)",
        description = "identifier(RFID UID 또는 QR UUID) 또는 학번으로 학생을 검색한다. "
            + "좌석 변경 신청 정보가 있으면 함께 반환한다.")
    @GetMapping("/search")
    public CommonResponse<StudentKioskResponse> searchStudent(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String studentNumber) {
        StudentKioskResponse student = studentService.searchStudentForKiosk(
            identifier, studentNumber);
        return CommonResponse.success(student);
    }

    @Operation(summary = "좌석번호로 학생 조회",
        description = "좌석번호(라벨)를 입력하면 해당 좌석에 배정된 학생 정보를 반환한다. "
            + "좌석이탈/휴대폰 미소지 신청 전 확인 화면용.")
    @GetMapping("/by-seat")
    public CommonResponse<StudentResponse> findStudentBySeatLabel(
            @RequestParam String seatLabel) {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(
            studentService.findBySeatLabel(seatLabel, storeId));
    }
}
