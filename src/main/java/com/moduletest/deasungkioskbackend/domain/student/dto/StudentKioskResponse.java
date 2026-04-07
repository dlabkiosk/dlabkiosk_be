package com.moduletest.deasungkioskbackend.domain.student.dto;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.dto.PhoneSubmissionResponse;
import com.moduletest.deasungkioskbackend.domain.seatchangerequest.dto.SeatChangeRequestResponse;
import com.moduletest.deasungkioskbackend.domain.seatleave.dto.SeatLeaveResponse;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "키오스크 학생 조회 응답 (신청 내역 포함)")
public record StudentKioskResponse(

    @Schema(description = "학생 ID", example = "1")
    Long id,

    @Schema(description = "학생 이름", example = "김민준")
    String name,

    @Schema(description = "학번", example = "2024-001")
    String studentNumber,

    @Schema(description = "전화번호 뒷자리 4자리", example = "1234")
    String phoneLast4,

    @Schema(description = "배정 좌석 라벨", example = "A-1")
    String assignedSeatLabel,

    @Schema(description = "당월 휴대폰 미소지 신청 내역")
    List<PhoneSubmissionResponse> phoneSubmissions,

    @Schema(description = "당월 좌석 변경 신청 내역")
    List<SeatChangeRequestResponse> seatChangeRequests,

    @Schema(description = "당월 좌석 이탈 내역")
    List<SeatLeaveResponse> seatLeaves,

    @Schema(description = "당월 급식 신청 내역 (DSA 3.30)")
    List<DsaMealApplication> mealApplications,

    @Schema(description = "수납 현황 (DSA 3.29)")
    List<DsaReceiptRecord> receipts,

    @Schema(description = "당월 출결 특이사항 (DSA 3.26 결석/조퇴/외출 + 3.15 지각)")
    DsaAttendanceSummary attendanceSummary,

    @Schema(description = "당월 상벌점 내역 (DSA 3.28)")
    List<DsaPointRecord> points
) {

    public static StudentKioskResponse of(Student student,
                                           List<PhoneSubmissionResponse> phoneSubmissions,
                                           List<SeatChangeRequestResponse> seatChangeRequests,
                                           List<SeatLeaveResponse> seatLeaves,
                                           List<DsaMealApplication> mealApplications,
                                           List<DsaReceiptRecord> receipts,
                                           DsaAttendanceSummary attendanceSummary,
                                           List<DsaPointRecord> points) {
        String seatLabel = null;
        if (student.getAssignedSeat() != null) {
            seatLabel = student.getAssignedSeat().getSeatLabel();
        }

        return new StudentKioskResponse(
            student.getId(),
            student.getName(),
            student.getStudentNumber(),
            student.getPhoneLast4(),
            seatLabel,
            phoneSubmissions,
            seatChangeRequests,
            seatLeaves,
            mealApplications,
            receipts,
            attendanceSummary,
            points
        );
    }
}
