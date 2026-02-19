package com.signaldecode.templatebackendapi.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 최상위 클래스
 * <p>
 * 사용법:
 * 1. 도메인별 Exception 클래스를 만들고 이 클래스를 상속받습니다
 * 2. static factory method로 편의 메서드를 제공합니다
 * <p>
 * 예시:
 * <pre>
 * public class MemberException extends BusinessException {
 *
 *     public MemberException(ErrorCode errorCode) {
 *         super(errorCode);
 *     }
 *
 *     public MemberException(ErrorCode errorCode, String customMessage) {
 *         super(errorCode, customMessage);
 *     }
 *
 *     // 편의 메서드
 *     public static MemberException notFound() {
 *         return new MemberException(ErrorCode.MEMBER_001);
 *     }
 *
 *     public static MemberException duplicateEmail() {
 *         return new MemberException(ErrorCode.MEMBER_002);
 *     }
 * }
 * </pre>
 * <p>
 * Service에서 사용:
 * <pre>
 * Member member = memberRepository.findById(id)
 *     .orElseThrow(MemberException::notFound);
 *
 * if (memberRepository.existsByEmail(email)) {
 *     throw MemberException.duplicateEmail();
 * }
 * </pre>
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 기본 생성자
     * ErrorCode의 메시지를 그대로 사용합니다.
     */
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 원인 예외를 포함하는 생성자
     * 예외 체이닝이 필요할 때 사용합니다.
     */
    protected BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 커스텀 메시지를 사용하는 생성자
     * ErrorCode의 기본 메시지 대신 다른 메시지를 사용할 때 사용합니다.
     */
    protected BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
