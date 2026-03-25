package com.moduletest.deasungkioskbackend.domain.tag.dto;

import com.moduletest.deasungkioskbackend.domain.meal.entity.MealType;
import com.moduletest.deasungkioskbackend.domain.tag.entity.AttendAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Schema(description = "통합 태그 응답")
@Builder
public record TagResponse(
    @Schema(description = "처리 완료 여부. false면 pendingActions 확인 필요", example = "true")
    boolean processed,

    @Schema(description = "처리된 출결 액션 (processed=true 시)", example = "S")
    AttendAction action,

    @Schema(description = "액션 한글명", example = "등원")
    String actionLabel,

    @Schema(description = "학생 ID", example = "1")
    Long studentId,

    @Schema(description = "학생 이름", example = "김민준")
    String studentName,

    @Schema(description = "학번", example = "2024-001")
    String studentNumber,

    @Schema(description = "배정 좌석 라벨", example = "A-1")
    String seatLabel,

    @Schema(description = "등원 시각 (등원/지각 시)")
    LocalDateTime checkInAt,

    @Schema(description = "하원 시각 (하원/조퇴 시)")
    LocalDateTime checkOutAt,

    @Schema(description = "순공시간 분 (하원/조퇴 시)", example = "480")
    Long studyTimeMinutes,

    @Schema(description = "학생 공지 메시지 (등원/지각 시)")
    List<String> messages,

    @Schema(description = "확인 필요한 액션 목록 (processed=false 시)")
    List<PendingAction> pendingActions,

    @Schema(description = "DSA 연동 여부", example = "true")
    boolean dsaSynced,

    @Schema(description = "급식 정보 (식사시간일 때만)")
    MealInfo mealInfo
) {

    @Schema(description = "확인 대기 중인 액션")
    public record PendingAction(
        @Schema(description = "액션 코드", example = "C")
        AttendAction action,

        @Schema(description = "확인 메시지", example = "조퇴 하시겠습니까?")
        String message,

        @Schema(description = "DSA 신청코드", example = "REQ001")
        String regCd
    ) {

    }

    @Schema(description = "급식 정보")
    public record MealInfo(
        @Schema(description = "식사 유형", example = "LUNCH")
        MealType mealType,

        @Schema(description = "식사 유형 한글명", example = "점심식사")
        String mealLabel,

        @Schema(description = "급식 신청 여부", example = "true")
        boolean applied,

        @Schema(description = "이미 태그 완료 여부", example = "false")
        boolean alreadyTagged,

        @Schema(description = "급식 안내 메시지", example = "확인되었습니다.")
        String message
    ) {

    }
}
