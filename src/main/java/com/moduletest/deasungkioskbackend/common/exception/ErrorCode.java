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


    // Student
    STUDENT_NOT_FOUND("STU001", "학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STUDENT_PHONE("STU002", "이미 등록된 전화번호입니다", HttpStatus.CONFLICT),
    QR_CODE_GENERATION_FAILED("STU003", "QR 코드 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),


    // Attendance
    STUDENT_NOT_FOUND_BY_QR("ATT001", "QR 코드에 해당하는 학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    STUDENT_NOT_FOUND_BY_RFID("ATT004", "RFID 카드에 해당하는 학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_CHECK_IN_REQUEST("ATT005", "QR UUID 또는 RFID UID 중 하나는 필수입니다", HttpStatus.BAD_REQUEST),
    ALREADY_CHECKED_IN("ATT002", "이미 등원 처리된 학생입니다", HttpStatus.CONFLICT),
    NOT_CHECKED_IN("ATT003", "등원 기록이 없습니다", HttpStatus.BAD_REQUEST),


    // Seat
    SEAT_NOT_FOUND("SE001", "좌석을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    SEAT_ALREADY_IN_USE("SE002", "이미 사용 중인 좌석입니다", HttpStatus.CONFLICT),
    SEAT_NOT_IN_USE("SE003", "사용 중이 아닌 좌석입니다", HttpStatus.BAD_REQUEST),
    STUDENT_ALREADY_HAS_SEAT("SE004", "이미 좌석을 사용 중인 학생입니다", HttpStatus.CONFLICT),
    SEAT_REDIS_CONFLICT("SE005", "좌석 선점에 실패했습니다. 다시 시도해주세요", HttpStatus.CONFLICT),


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
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
