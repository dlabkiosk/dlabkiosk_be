package com.moduletest.deasungkioskbackend.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtTokenProvider {


    private final SecretKey secretKey;
    @Getter
    private final long accessExpiration;
    @Getter
    private final long refreshExpiration;

    public JwtTokenProvider(
        @Value("${JWT_SECRET}") String secret,
        @Value("${JWT_ACCESS_EXPIRATION}") long accessExpiration,
        @Value("${JWT_REFRESH_EXPIRATION}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }


    public String createAccessToken(String userId, String loginId, String role, Long storeId) {
        Date now = new Date();

        return Jwts.builder()
            .subject(userId)
            .claim("loginId", loginId)
            .claim("role", role)
            .claim("storeId", storeId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessExpiration))
            .signWith(secretKey)
            .compact();
    }

    public String createKioskToken(Long storeId, String storeCode) {
        return Jwts.builder()
            .subject(String.valueOf(storeId))
            .claim("storeCode", storeCode)
            .claim("role", "KIOSK")
            .issuedAt(new Date())
            .signWith(secretKey)
            .compact();
    }


    public String createRefreshToken(String userId) {
        Date now = new Date();

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshExpiration))
            .signWith(secretKey)
            .compact();

    }


    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

}
