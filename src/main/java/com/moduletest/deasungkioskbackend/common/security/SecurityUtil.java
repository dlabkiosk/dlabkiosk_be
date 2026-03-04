package com.moduletest.deasungkioskbackend.common.security;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
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
