package com.moduletest.deasungkioskbackend.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

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
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        subject, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "EXPIRED_TOKEN");
            } catch (JwtException e) {
                request.setAttribute("exception", "INVALID_TOKEN");
            }
        }

        filterChain.doFilter(request, response);
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
