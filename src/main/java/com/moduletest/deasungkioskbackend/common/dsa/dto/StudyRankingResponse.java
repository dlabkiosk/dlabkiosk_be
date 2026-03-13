package com.moduletest.deasungkioskbackend.common.dsa.dto;

import lombok.Builder;

@Builder
public record StudyRankingResponse(
    DsaResponse firstPlace,
    DsaResponse averageStudyTime,
    DsaResponse rankingList
) {

}
