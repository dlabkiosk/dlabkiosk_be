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


    // Store
    STORE_NOT_FOUND("ST001", "지점을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STORE_CODE("ST002", "이미 사용 중인 지점 코드입니다", HttpStatus.CONFLICT),


    // Student
    STUDENT_NOT_FOUND("STU001", "학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_STUDENT_PHONE("STU002", "이미 등록된 전화번호입니다", HttpStatus.CONFLICT),
    QR_CODE_GENERATION_FAILED("STU003", "QR 코드 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),


    // Attendance
    STUDENT_NOT_FOUND_BY_QR("ATT001", "QR 코드에 해당하는 학생을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ALREADY_CHECKED_IN("ATT002", "이미 등원 처리된 학생입니다", HttpStatus.CONFLICT),
    NOT_CHECKED_IN("ATT003", "등원 기록이 없습니다", HttpStatus.BAD_REQUEST),


    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
