package com.moduletest.deasungkioskbackend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("V001", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),


    // Auth
    ADMIN_NOT_FOUND("A001", "관리자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("A002", "잘못된 사용자 이름 또는 비밀번호입니다", HttpStatus.UNAUTHORIZED),
    DUPLICATE_LOGIN_ID("A005", "이미 사용 중인 아이디입니다", HttpStatus.CONFLICT),
    EXPIRED_TOKEN("A003", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("A004", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED("A006", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("A007", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN("A008", "유효하지 않은 리프레시 토큰입니다", HttpStatus.UNAUTHORIZED),


    // Store
    STORE_NOT_FOUND("ST001", "지점을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STORE_CODE("ST002", "이미 사용 중인 지점 코드입니다", HttpStatus.CONFLICT),
    STORE_ID_REQUIRED("ST003", "ADMIN은 지점 ID를 지정해야 합니다", HttpStatus.BAD_REQUEST),


    // Student
    STUDENT_NOT_FOUND("STU001", "학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STUDENT_PHONE("STU002", "이미 등록된 전화번호입니다", HttpStatus.CONFLICT),
    QR_CODE_GENERATION_FAILED("STU003", "QR 코드 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STUDENT_NOT_FOUND_BY_STUDENT_NUMBER("STU004", "학번에 해당하는 학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STUDENT_NUMBER("STU005", "이미 사용 중인 학번입니다", HttpStatus.CONFLICT),


    // Attendance
    STUDENT_NOT_FOUND_BY_PHONE("ATT006", "전화번호에 해당하는 학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_STUDENT_IDENTIFIER("ATT007", "학생 인식 정보가 필요합니다 (identifier/학번/전화번호 중 하나)",
        HttpStatus.BAD_REQUEST),
    ALREADY_CHECKED_IN("ATT002", "이미 등원 처리된 학생입니다", HttpStatus.CONFLICT),
    NOT_CHECKED_IN("ATT003", "등원 기록이 없습니다", HttpStatus.BAD_REQUEST),


    // Seat
    SEAT_NOT_FOUND("SE001", "좌석을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    SEAT_ALREADY_IN_USE("SE002", "이미 사용 중인 좌석입니다", HttpStatus.CONFLICT),
    SEAT_NOT_IN_USE("SE003", "사용 중이 아닌 좌석입니다", HttpStatus.BAD_REQUEST),
    STUDENT_ALREADY_HAS_SEAT("SE004", "이미 좌석을 사용 중인 학생입니다", HttpStatus.CONFLICT),
    SEAT_REDIS_CONFLICT("SE005", "좌석 선점에 실패했습니다. 다시 시도해주세요", HttpStatus.CONFLICT),
    NO_ASSIGNED_SEAT("SE006", "배정된 좌석이 없습니다", HttpStatus.BAD_REQUEST),


    // Outing
    NOT_CHECKED_IN_FOR_OUTING("OT001", "등원 상태가 아니므로 외출할 수 없습니다", HttpStatus.BAD_REQUEST),
    ALREADY_ON_OUTING("OT002", "이미 외출 중입니다", HttpStatus.CONFLICT),
    NOT_ON_OUTING("OT003", "진행 중인 외출이 없습니다", HttpStatus.BAD_REQUEST),


    // Kiosk
    KIOSK_INVALID_CREDENTIALS("K001", "지점 코드 또는 PIN이 올바르지 않습니다", HttpStatus.UNAUTHORIZED),
    KIOSK_STORE_INACTIVE("K002", "비활성화된 지점입니다", HttpStatus.FORBIDDEN),
    STUDENT_NOT_IN_THIS_STORE("K003", "해당 지점에 소속되지 않은 학생입니다", HttpStatus.FORBIDDEN),


    // Notice
    NOTICE_NOT_FOUND("NTC001", "공지사항을 찾을 수 없습니다", HttpStatus.NOT_FOUND),


    // Advertisement
    ADVERTISEMENT_NOT_FOUND("AD001", "광고를 찾을 수 없습니다", HttpStatus.NOT_FOUND),


    // Exam Schedule
    EXAM_SCHEDULE_NOT_FOUND("ES001", "시험 일정을 찾을 수 없습니다", HttpStatus.NOT_FOUND),


    // file upload
    FILE_UPLOAD_FAILED("FU001", "파일 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FD001", "파일 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    SEAT_LEAVE_REASON_NOT_FOUND("SLR001", "좌석이탈 사유를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ALREADY_ON_SEAT_LEAVE("SL001", "이미 좌석이탈 중입니다", HttpStatus.CONFLICT),
    NOT_ON_SEAT_LEAVE("SL002", "진행 중인 좌석이탈이 없습니다", HttpStatus.BAD_REQUEST),
    NO_ACTIVE_SEAT("SL003", "사용 중인 좌석이 없습니다", HttpStatus.BAD_REQUEST),


    // Phone Submission
    ALREADY_SUBMITTED_PHONE("PS001", "이미 해당 기간에 휴대폰 미소지 신청이 있습니다", HttpStatus.CONFLICT),
    INVALID_PHONE_SUBMISSION_PERIOD("PS002", "시작일은 종료일보다 이전이어야 합니다", HttpStatus.BAD_REQUEST),
    PHONE_SUBMISSION_NOT_FOUND("PS003", "해당 휴대폰 미소지 신청을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Seat Label
    STUDENT_NOT_FOUND_BY_SEAT_LABEL("SL004", "해당 좌석에 배정된 학생이 없습니다", HttpStatus.NOT_FOUND),

    // Phone Last4
    STUDENT_NOT_FOUND_BY_PHONE_LAST4("STU006", "해당 전화번호 뒷자리로 학생을 찾을 수 없습니다",
        HttpStatus.NOT_FOUND),
    MULTIPLE_STUDENTS_FOUND_BY_PHONE_LAST4("STU007",
        "동일한 전화번호 뒷자리를 가진 학생이 여러 명입니다. UUID으로 검색해주세요",
        HttpStatus.CONFLICT),
    STUDENT_NOT_IN_STORE("STU008", "해당 지점에 소속된 학생이 아닙니다", HttpStatus.FORBIDDEN),

    // Student Message
    STUDENT_MESSAGE_NOT_FOUND("SM001", "학생 메시지를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MESSAGE_TEMPLATE_NOT_FOUND("MT001", "메시지 템플릿을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Seat Change Request
    SEAT_CHANGE_REQUEST_NOT_FOUND("SCR001", "좌석 변경 신청을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ALREADY_PENDING_SEAT_CHANGE("SCR002", "이미 대기 중인 좌석 변경 신청이 있습니다", HttpStatus.CONFLICT),
    DESIRED_SEAT_ALREADY_ASSIGNED("SCR003", "희망 좌석이 이미 다른 학생에게 배정되어 있습니다", HttpStatus.CONFLICT),
    SEAT_CHANGE_ALREADY_PROCESSED("SCR004", "이미 처리된 좌석 변경 신청입니다", HttpStatus.BAD_REQUEST),
    SEAT_NOT_IN_THIS_STORE("SCR005", "해당 지점의 좌석이 아닙니다", HttpStatus.BAD_REQUEST),
    DESIRED_SEAT_IS_CURRENT("SCR006", "현재 배정된 좌석과 동일한 좌석입니다", HttpStatus.BAD_REQUEST),
    SEAT_NOT_IN_REQUEST("SCR007", "해당 좌석은 이 신청의 희망 순위에 포함되어 있지 않습니다", HttpStatus.BAD_REQUEST),
    DSA_SEAT_CHANGE_FAILED("SCR008", "DSA 좌석 변경 처리에 실패했습니다", HttpStatus.BAD_GATEWAY),

    // Outing approval
    OUTING_NOT_APPROVED("OT004", "승인된 외출/조퇴 신청이 없습니다", HttpStatus.FORBIDDEN),
    EARLY_LEAVE_RE_ATTEND_FAILED("OT005",
        "재등원 처리에 실패했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_GATEWAY),
    DSA_SYNC_FAILED("OT006",
        "DSA 출결 처리에 실패했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_GATEWAY),
    DSA_REJECTED("OT007",
        "승인 내역이 없습니다. 선생님께 문의해주세요.", HttpStatus.FORBIDDEN),

    // Meal
    NOT_MEAL_TIME("ML001", "현재 식사시간이 아닙니다", HttpStatus.BAD_REQUEST),
    ALREADY_MEAL_TAGGED("ML002", "이미 급식 태그가 완료되었습니다", HttpStatus.CONFLICT),

    // Meal Menu
    MEAL_MENU_NOT_FOUND("MM001", "식단을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // Sync
    SYNC_DSA_CREDENTIALS_MISSING("SYNC001", "DSA 인증정보가 없는 지점입니다", HttpStatus.BAD_REQUEST),
    SYNC_FAILED("SYNC002", "학생 동기화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    ;


    private final String code;
    private final String message;
    private final HttpStatus status;
}
