package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
        description = "학번, 전화번호, QR UUID, RFID UID 중 하나로 학생을 검색한다. "
            + "4가지 중 하나만 전송하고 나머지는 비워야 한다.")
    @GetMapping("/search")
    public CommonResponse<StudentResponse> searchStudent(
        @RequestParam(required = false) String qrUuid,
        @RequestParam(required = false) String rfidUid,
        @RequestParam(required = false) String studentNumber,
        @RequestParam(required = false) String phone) {
        StudentResponse student = studentService.searchStudent(
            qrUuid, rfidUid, studentNumber, phone);
        return CommonResponse.success(student);
    }
}
