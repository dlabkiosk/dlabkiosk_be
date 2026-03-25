package com.moduletest.deasungkioskbackend.domain.studytime.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record StudyTimeStudentResponse(
    String studentName,
    String studentNumber,
    String seatLabel,
    int totalMinutes,
    String totalStudyTime,
    List<DailyStudyTime> dailyStudyTimes
) {

    @Builder
    public record DailyStudyTime(
        LocalDate date,
        int studyTimeMinutes,
        String studyTime
    ) {
    }
}
