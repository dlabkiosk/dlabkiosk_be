package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentCreateRequest;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 학생 관리", description = "학생 CRUD 및 QR 코드 다운로드 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class StudentAdminController {

    private final StudentService studentService;

    @Operation(summary = "학생 목록 조회",
        description = "MANAGER: 자기 지점 학생만 조회. ADMIN: 전체 학생 조회.")
    @GetMapping
    public CommonResponse<List<StudentResponse>> findAllStudents() {
        Long storeId = SecurityUtil.resolveStoreId(null);
        List<StudentResponse> students = studentService.findAllStudents(storeId);
        return CommonResponse.success(students);
    }

    @Operation(summary = "학생 단건 조회", description = "학생 ID로 단건 조회한다.")
    @GetMapping("/{studentId}")
    public CommonResponse<StudentResponse> findStudentById(@PathVariable Long studentId) {
        StudentResponse student = studentService.findStudentById(studentId);
        return CommonResponse.success(student);
    }

    @Operation(summary = "학생 등록",
        description = "MANAGER: 자기 지점에 자동 등록. "
            + "ADMIN: storeId 필수 — 등록할 지점 지정. "
            + "QR용 UUID가 자동 생성된다.")
    @PostMapping
    public CommonResponse<StudentResponse> createStudent(
        @RequestParam(required = false) Long storeId,
        @Valid @RequestBody StudentCreateRequest request) {
        Long resolvedStoreId = SecurityUtil.resolveStoreIdRequired(storeId);
        StudentResponse student = studentService.createStudent(request, resolvedStoreId);
        return CommonResponse.success(student);
    }

    @Operation(summary = "학생 정보 수정", description = "학생 정보를 수정한다. 소속 지점은 변경되지 않는다.")
    @PutMapping("/{studentId}")
    public CommonResponse<StudentResponse> updateStudent(
        @PathVariable Long studentId,
        @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse student = studentService.updateStudent(studentId, request);
        return CommonResponse.success(student);
    }

    @Operation(summary = "학생 삭제", description = "학생을 삭제한다.")
    @DeleteMapping("/{studentId}")
    public CommonResponse<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return CommonResponse.success(null);
    }

    @Operation(summary = "QR 코드 이미지 다운로드",
        description = "학생의 출석용 QR 코드를 PNG 이미지로 다운로드한다. 응답은 image/png 바이너리.")
    @GetMapping("/{studentId}/qr")
    public ResponseEntity<byte[]> downloadStudentQrCode(@PathVariable Long studentId) {
        byte[] qrPng = studentService.generateStudentQrCode(studentId);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"qr-" + studentId + ".png\"")
            .body(qrPng);
    }
}
