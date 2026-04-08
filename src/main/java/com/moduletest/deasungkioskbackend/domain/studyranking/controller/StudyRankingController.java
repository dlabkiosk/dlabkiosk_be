package com.moduletest.deasungkioskbackend.domain.studyranking.controller;

import com.moduletest.deasungkioskbackend.common.dsa.dto.StudyRankingResponse;
import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.studyranking.dto.AllStoreRankingResponse;
import com.moduletest.deasungkioskbackend.domain.studyranking.service.StudyRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 순공랭킹", description = "전주 순공시간 랭킹 조회")
@RestController
@RequestMapping("/api/v1/kiosk/study-rankings")
@RequiredArgsConstructor
public final class StudyRankingController {

    private final StudyRankingService studyRankingService;

    @Operation(summary = "지점 순공시간 랭킹 조회",
        description = "해당 지점의 전주 순공시간 랭킹, 1위, 평균 공부시간을 조회한다. "
            + "첫 조회 시 DSA에서 가져와 다음 월요일까지 캐싱.")
    @GetMapping
    public CommonResponse<StudyRankingResponse> getStudyRanking() {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        StudyRankingResponse response = studyRankingService.getStudyRanking(storeId);
        return CommonResponse.success(response);
    }

    @Operation(summary = "전 지점 통합 순공시간 랭킹 조회",
        description = "전 지점 학생의 전주 순공시간을 합산하여 랭킹을 조회한다. "
            + "지점명 포함. 첫 조회 시 DSA에서 가져와 다음 월요일까지 캐싱.")
    @GetMapping("/all")
    public CommonResponse<AllStoreRankingResponse> getAllStoreRanking() {
        AllStoreRankingResponse response = studyRankingService.getAllStoreRanking();
        return CommonResponse.success(response);
    }

}
