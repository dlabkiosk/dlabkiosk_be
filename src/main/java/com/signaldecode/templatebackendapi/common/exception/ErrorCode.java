package com.signaldecode.templatebackendapi.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의 템플릿
 * <p>
 * 사용법:
 * 1. 이 파일을 새 프로젝트에 복사
 * 2. 패키지명 변경
 * 3. 필요한 에러 코드를 추가
 * <p>
 * 네이밍 규칙:
 * - [도메인]_[번호] 형식 사용 (예: MEMBER_001, AUTH_001)
 * - 같은 도메인은 연속 번호 사용
 * - 코드 중복 금지
 * <p>
 * HttpStatus 매핑 가이드:
 * - 400 BAD_REQUEST: 잘못된 요청, 검증 실패
 * - 401 UNAUTHORIZED: 인증 필요
 * - 403 FORBIDDEN: 권한 없음
 * - 404 NOT_FOUND: 리소스 없음
 * - 409 CONFLICT: 충돌, 중복
 * - 429 TOO_MANY_REQUESTS: 요청 제한 초과
 * - 500 INTERNAL_SERVER_ERROR: 서버 에러
 * - 503 SERVICE_UNAVAILABLE: 서비스 불가
 */
@Getter
public enum ErrorCode {

    // ==========================================
    // 기본 공통 에러 (필수 - 삭제하지 마세요)
    // ==========================================
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 여기부터 프로젝트별 에러 코드 추가
    // ==========================================

    // 예시 1: 회원 관련 에러
    // MEMBER_001("MEMBER_001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    // MEMBER_002("MEMBER_002", "이미 사용중인 이메일입니다.", HttpStatus.CONFLICT),
    // MEMBER_003("MEMBER_003", "이미 사용중인 닉네임입니다.", HttpStatus.CONFLICT),

    // 예시 2: 인증 관련 에러
    // AUTH_001("AUTH_001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    // AUTH_002("AUTH_002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    // AUTH_003("AUTH_003", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 예시 3: 게시물 관련 에러
    // POST_001("POST_001", "게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    // POST_002("POST_002", "이미 삭제된 게시물입니다.", HttpStatus.BAD_REQUEST),

    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
