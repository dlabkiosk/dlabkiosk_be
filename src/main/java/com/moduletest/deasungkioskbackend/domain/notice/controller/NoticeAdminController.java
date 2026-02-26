package com.moduletest.deasungkioskbackend.domain.notice.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeCreateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeResponse;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeUpdateRequest;
import com.moduletest.deasungkioskbackend.domain.notice.service.NoticeService;
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


@Tag(name = "[관리자] 공지사항", description = "공지사항 CRUD (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class NoticeAdminController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회",
        description = "전체 또는 지점별 공지사항을 조회한다. storeId 미입력 시 전체 조회.")
    @GetMapping
    public CommonResponse<List<NoticeResponse>> findAllNotices(
        @RequestParam(required = false) Long storeId) {
        List<NoticeResponse> notices;
        if (storeId != null) {
            notices = noticeService.findAllNoticesByStoreId(storeId);
        } else {
            notices = noticeService.findAllNotices();
        }
        return CommonResponse.success(notices);
    }

    @Operation(summary = "공지사항 단건 조회", description = "공지사항 ID로 단건 조회한다.")
    @GetMapping("/{noticeId}")
    public CommonResponse<NoticeResponse> findNoticeById(@PathVariable Long noticeId) {
        NoticeResponse notice = noticeService.findNoticeById(noticeId);
        return CommonResponse.success(notice);
    }

    @Operation(summary = "공지사항 등록", description = "새 공지사항을 등록한다.")
    @PostMapping
    public CommonResponse<NoticeResponse> createNotice(
        @Valid @RequestBody NoticeCreateRequest request) {
        NoticeResponse notice = noticeService.createNotice(request);
        return CommonResponse.success(notice);
    }

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정한다.")
    @PutMapping("/{noticeId}")
    public CommonResponse<NoticeResponse> updateNotice(@PathVariable Long noticeId,
        @Valid @RequestBody NoticeUpdateRequest request) {
        NoticeResponse notice = noticeService.updateNotice(noticeId, request);
        return CommonResponse.success(notice);
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제한다.")
    @DeleteMapping("/{noticeId}")
    public CommonResponse<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return CommonResponse.success(null);
    }
}
