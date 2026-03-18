package com.moduletest.deasungkioskbackend.domain.dashboard.dto;

import com.moduletest.deasungkioskbackend.common.dsa.dto.DsaResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record DashboardAllResponse(
    DailyOperationSummary dailyOperation,
    List<MealTagRecord> mealTags,
    AttendanceSummary attendanceSummary,
    SeatLeaveSummary seatLeaveSummary,
    DsaResponse studyRanking,
    List<PendingApprovalRecord> pendingApprovals,
    List<SeatChangeRequestRecord> seatChangeRequests,
    List<NoticeSummaryRecord> notices
) {

    @Builder
    public record DailyOperationSummary(
        long registeredStudents,
        Long todayAttendance,
        Long mealRequests
    ) {
    }

    @Builder
    public record MealTagRecord(
        String name,
        String mealType,
        LocalDateTime taggedAt
    ) {
    }

    @Builder
    public record AttendanceSummary(
        Long present,
        Long earlyLeave,
        Long absent,
        long outing,
        Long late
    ) {
    }

    @Builder
    public record SeatLeaveSummary(
        long totalLeave,
        long waitingReturn
    ) {
    }

    @Builder
    public record PendingApprovalRecord(
        Long id,
        String requestType,
        String requestContent,
        String requesterName,
        LocalDateTime requestedAt
    ) {
    }

    @Builder
    public record SeatChangeRequestRecord(
        Long id,
        String studentName,
        String currentSeatLabel,
        String desiredSeat1Label,
        String desiredSeat2Label,
        String desiredSeat3Label,
        LocalDateTime requestedAt
    ) {
    }

    @Builder
    public record NoticeSummaryRecord(
        Long id,
        String title,
        LocalDateTime createdAt
    ) {
    }
}
