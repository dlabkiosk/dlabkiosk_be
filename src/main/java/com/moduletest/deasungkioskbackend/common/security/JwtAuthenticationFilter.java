package com.moduletest.deasungkioskbackend.common.security;

import com.moduletest.deasungkioskbackend.domain.kiosk.log.KioskAuthEventType;
import com.moduletest.deasungkioskbackend.domain.kiosk.log.KioskAuthLogService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String KIOSK_PATH_PREFIX = "/api/v1/kiosk";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final KioskAuthLogService kioskAuthLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String subject = claims.getSubject();
                String role = claims.get("role", String.class);

                if (!isTokenStoredInRedis(subject, role, token)) {
                    request.setAttribute("exception", "INVALID_TOKEN");
                    logKioskRejection(request, "REDIS_MISS", subject, claims);
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        subject, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                Map<String, Object> details = new HashMap<>();
                details.put("role", role);
                Long storeId = claims.get("storeId", Long.class);
                if (storeId != null) {
                    details.put("storeId", storeId);
                }
                authentication.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "EXPIRED_TOKEN");
                logKioskRejection(request, "EXPIRED_TOKEN",
                    e.getClaims() != null ? e.getClaims().getSubject() : null,
                    e.getClaims());
            } catch (JwtException e) {
                request.setAttribute("exception", "INVALID_TOKEN");
                logKioskRejection(request, "INVALID_TOKEN", null, null);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void logKioskRejection(HttpServletRequest request, String reason,
                                   String subject, Claims claims) {
        if (!request.getRequestURI().startsWith(KIOSK_PATH_PREFIX)) {
            return;
        }
        Long storeId = parseLong(subject);
        String storeCode = claims != null ? claims.get("storeCode", String.class) : null;
        kioskAuthLogService.log(KioskAuthEventType.AUTH_REJECTED,
            storeId, storeCode, reason, resolveClientIp(request),
            request.getHeader("User-Agent"));
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isTokenStoredInRedis(String subject, String role, String token) {
        try {
            Long id = Long.valueOf(subject);
            if ("KIOSK".equals(role)) {
                return tokenRedisService.isKioskTokenValid(id, token);
            }
            return tokenRedisService.isAdminTokenValid(id, token);
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
