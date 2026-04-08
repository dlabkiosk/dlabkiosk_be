package com.moduletest.deasungkioskbackend.common.security;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        return parseSubjectAsLong();
    }

    /**
     * 키오스크 토큰의 subject(storeId)를 Long으로 반환한다.
     * 어드민 토큰에서는 userId가 반환되므로 사용처 주의.
     */
    public static Long getStoreIdFromToken() {
        return parseSubjectAsLong();
    }

    private static Long parseSubjectAsLong() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public static String getCurrentRole() {
        return getDetail("role", String.class);
    }

    public static Long getCurrentStoreId() {
        return getDetail("storeId", Long.class);
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    public static Long resolveStoreId(Long requestedStoreId) {
        if (isAdmin()) {
            return requestedStoreId;
        }
        return getCurrentStoreId();
    }

    public static Long resolveStoreIdRequired(Long requestedStoreId) {
        Long resolved = resolveStoreId(requestedStoreId);
        if (resolved == null) {
            throw new BusinessException(ErrorCode.STORE_ID_REQUIRED);
        }
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDetail(String key, Class<T> type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Object value = details.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
}
