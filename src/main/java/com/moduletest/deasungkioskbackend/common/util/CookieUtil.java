package com.moduletest.deasungkioskbackend.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public final class CookieUtil {

    private CookieUtil() {
    }

    public static void addAccessToken(HttpServletResponse response, String token, long maxAgeMillis) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMillis(maxAgeMillis))
            .sameSite("Strict")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void addRefreshToken(HttpServletResponse response, String token, long maxAgeMillis) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(false)
            .path("/api/admin/auth")
            .maxAge(Duration.ofMillis(maxAgeMillis))
            .sameSite("Strict")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearAccessToken(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearRefreshToken(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false)
            .path("/api/admin/auth")
            .maxAge(0)
            .sameSite("Strict")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}