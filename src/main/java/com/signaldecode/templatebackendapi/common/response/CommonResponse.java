package com.signaldecode.templatebackendapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 공통 응답 포맷
 * <p>
 * 성공 응답 예시:
 * <pre>
 * {
 *   "success": true,
 *   "data": {
 *     "id": 1,
 *     "name": "홍길동"
 *   }
 * }
 * </pre>
 * <p>
 * 실패 응답 예시:
 * <pre>
 * {
 *   "success": false,
 *   "error": {
 *     "code": "MEMBER_001",
 *     "message": "회원을 찾을 수 없습니다."
 *   }
 * }
 * </pre>
 * <p>
 * Controller 사용 예시:
 * <pre>
 * // 성공 응답 (데이터 포함)
 * return ResponseEntity.ok(CommonResponse.success(memberResponse));
 *
 * // 성공 응답 (데이터 없음)
 * return ResponseEntity.ok(CommonResponse.success());
 *
 * // 에러는 GlobalExceptionHandler가 자동 처리
 * </pre>
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, null, null);
    }

    /**
     * 실패 응답 (코드, 메시지)
     */
    public static <T> CommonResponse<T> error(String code, String message) {
        return new CommonResponse<>(false, null, new ErrorResponse(code, message));
    }

    /**
     * 실패 응답 (HTTP 상태, 코드, 메시지)
     * 주로 GlobalExceptionHandler에서 사용
     */
    public static <T> CommonResponse<T> error(int status, String code, String message) {
        return new CommonResponse<>(false, null, new ErrorResponse(code, message));
    }

    /**
     * 에러 응답 상세
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private final String code;
        private final String message;
    }
}
