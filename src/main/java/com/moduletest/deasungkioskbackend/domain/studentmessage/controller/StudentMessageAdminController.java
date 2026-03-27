package com.moduletest.deasungkioskbackend.domain.studentmessage.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageCreateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageResponse;
import com.moduletest.deasungkioskbackend.domain.studentmessage.dto.StudentMessageUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.studentmessage.service.StudentMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 학생별 공지 메시지",
    description = "학생별 개별 메시지 관리 (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/student-messages")
@RequiredArgsConstructor
public class StudentMessageAdminController {

    private final StudentMessageService studentMessageService;

    @Operation(summary = "학생별 메시지 목록 조회",
        description = "특정 학생에게 등록된 메시지를 전체 조회한다. "
            + "MANAGER는 자기 지점 학생만 조회 가능.")
    @GetMapping
    public CommonResponse<List<StudentMessageResponse>> findAllMessages(
        @RequestParam Long studentId) {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            studentMessageService.findAllByStudentId(studentId, storeId));
    }

    @Operation(summary = "학생별 메시지 등록",
        description = "특정 학생에게 공지 메시지를 등록한다. "
            + "태깅 시 해당 학생에게 노출된다. "
            + "MANAGER는 자기 지점 학생에게만 등록 가능.")
    @PostMapping
    public CommonResponse<List<StudentMessageResponse>> createMessage(
        @Valid @RequestBody StudentMessageCreateRequest request) {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            studentMessageService.createMessage(request, storeId));
    }

    @Operation(summary = "학생별 메시지 수정",
        description = "메시지 내용 및 활성 여부를 수정한다. "
            + "MANAGER는 자기 지점 메시지만 수정 가능.")
    @PutMapping("/{id}")
    public CommonResponse<StudentMessageResponse> updateMessage(
        @PathVariable Long id,
        @Valid @RequestBody StudentMessageUpdateRequest request) {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            studentMessageService.updateMessage(id, request, storeId));
    }

    @Operation(summary = "학생별 메시지 삭제",
        description = "MANAGER는 자기 지점 메시지만 삭제 가능.")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteMessage(@PathVariable Long id) {
        Long storeId = SecurityUtil.resolveStoreId(null);
        studentMessageService.deleteMessage(id, storeId);
        return CommonResponse.success(null);
    }
}
