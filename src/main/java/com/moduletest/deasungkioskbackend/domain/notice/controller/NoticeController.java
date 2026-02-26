package com.moduletest.deasungkioskbackend.domain.notice.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.notice.dto.NoticeResponse;
import com.moduletest.deasungkioskbackend.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 공지사항", description = "공지사항 조회 (키오스크 로그인 필요)")
@RestController
@RequestMapping("/api/v1/kiosk/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "지점 공지사항 조회",
        description = "해당 지점의 활성화된 공지사항을 조회한다. 고정 공지가 먼저, 이후 최신순 정렬.")
    @GetMapping
    public CommonResponse<List<NoticeResponse>> findNoticesByStore() {
        Long storeId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        List<NoticeResponse> notices = noticeService.findActiveNoticesByStoreId(storeId);
        return CommonResponse.success(notices);
    }
}
