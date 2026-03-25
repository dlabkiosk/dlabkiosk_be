package com.moduletest.deasungkioskbackend.domain.student.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.student.dto.StudentSyncResult;
import com.moduletest.deasungkioskbackend.domain.student.service.StudentSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 학생 동기화")
@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public final class StudentSyncAdminController {

    private final StudentSyncService studentSyncService;

    @Operation(summary = "학생 동기화 (DSA → DB)",
        description = "DSA 3.20 getStdInfoList를 호출하여 학생 데이터를 동기화합니다. "
            + "storeId 미지정 시 ADMIN은 전체 지점, MANAGER는 자기 지점만 동기화합니다.")
    @PostMapping("/students")
    public CommonResponse<List<StudentSyncResult>> syncStudents(
        @RequestParam(required = false) Long storeId) {

        Long resolvedStoreId = SecurityUtil.resolveStoreId(storeId);

        if (resolvedStoreId != null) {
            StudentSyncResult result = studentSyncService.synchronizeByStoreId(resolvedStoreId);
            return CommonResponse.success(List.of(result));
        }

        List<StudentSyncResult> results = studentSyncService.synchronizeAllStores();
        return CommonResponse.success(results);
    }
}
