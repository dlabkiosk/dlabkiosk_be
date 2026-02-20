package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentCreateRequest;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentResponse;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentService;
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

@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class StudentAdminController {

    private final StudentService studentService;

    @GetMapping
    public CommonResponse<List<StudentResponse>> findAllStudents(
        @RequestParam(required = false) Long storeId) {
        List<StudentResponse> students = studentService.findAllStudents(storeId);
        return CommonResponse.success(students);
    }

    @GetMapping("/{studentId}")
    public CommonResponse<StudentResponse> findStudentById(@PathVariable Long studentId) {
        StudentResponse student = studentService.findStudentById(studentId);
        return CommonResponse.success(student);
    }

    @PostMapping
    public CommonResponse<StudentResponse> createStudent(
        @Valid @RequestBody StudentCreateRequest request) {
        StudentResponse student = studentService.createStudent(request);
        return CommonResponse.success(student);
    }

    @PutMapping("/{studentId}")
    public CommonResponse<StudentResponse> updateStudent(
        @PathVariable Long studentId,
        @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse student = studentService.updateStudent(studentId, request);
        return CommonResponse.success(student);
    }

    @DeleteMapping("/{studentId}")
    public CommonResponse<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return CommonResponse.success(null);
    }

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
