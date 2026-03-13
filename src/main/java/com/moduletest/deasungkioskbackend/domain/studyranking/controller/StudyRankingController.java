package com.moduletest.deasungkioskbackend.domain.studyranking.controller;

import com.moduletest.deasungkioskbackend.common.dsa.dto.StudyRankingResponse;
import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.domain.studyranking.service.StudyRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 순공랭킹", description = "전주 순공시간 랭킹 조회")
@RestController
@RequestMapping("/api/v1/kiosk/study-rankings")
@RequiredArgsConstructor
public final class StudyRankingController {

    private final StudyRankingService studyRankingService;

    @Operation(summary = "전주 순공시간 랭킹 조회", description = "전주 순공시간 랭킹, 순공시간 1위, 평균 공부시간 조회")
    @GetMapping
    public CommonResponse<StudyRankingResponse> getStudyRanking() {
        Long storeId = getStoreIdFromToken();
        StudyRankingResponse response = studyRankingService.getStudyRanking(storeId);
        return CommonResponse.success(response);
    }

    private Long getStoreIdFromToken() {
        return Long.valueOf(
            SecurityContextHolder.getContext()
                .getAuthentication()
                .getName());
    }
}
