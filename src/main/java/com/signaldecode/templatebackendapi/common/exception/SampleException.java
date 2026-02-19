package com.signaldecode.templatebackendapi.common.exception;

/**
 * Sample 도메인 예외 클래스 (사용 예시)
 * <p>
 * 이 클래스는 BusinessException을 어떻게 확장하는지 보여주는 예시입니다.
 * 실제 프로젝트에서는 MemberException, AuthException 등으로 만들어 사용하세요.
 */
public class SampleException extends BusinessException {

    public SampleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SampleException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SampleException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    // ==========================================
    // Static Factory Methods (편의 메서드)
    // ==========================================
    // 이렇게 만들면 Service에서 간단하게 사용할 수 있습니다.
    // 예: throw SampleException.internalServerError();

    public static SampleException internalServerError() {
        return new SampleException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public static SampleException invalidInput() {
        return new SampleException(ErrorCode.INVALID_INPUT_VALUE);
    }

    public static SampleException invalidInput(String customMessage) {
        return new SampleException(ErrorCode.INVALID_INPUT_VALUE, customMessage);
    }

    // ==========================================
    // 실제 프로젝트 예시
    // ==========================================
    /*

    // MemberException.java
    public class MemberException extends BusinessException {

        public MemberException(ErrorCode errorCode) {
            super(errorCode);
        }

        public static MemberException notFound() {
            return new MemberException(ErrorCode.MEMBER_001);
        }

        public static MemberException duplicateEmail() {
            return new MemberException(ErrorCode.MEMBER_002);
        }

        public static MemberException duplicateNickname() {
            return new MemberException(ErrorCode.MEMBER_003);
        }
    }

    // AuthException.java
    public class AuthException extends BusinessException {

        public AuthException(ErrorCode errorCode) {
            super(errorCode);
        }

        public static AuthException invalidToken() {
            return new AuthException(ErrorCode.AUTH_001);
        }

        public static AuthException expiredToken() {
            return new AuthException(ErrorCode.AUTH_002);
        }

        public static AuthException forbidden() {
            return new AuthException(ErrorCode.AUTH_003);
        }
    }

    */
}
