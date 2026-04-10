package com.moduletest.deasungkioskbackend.domain.kiosk.controller;

import static com.moduletest.deasungkioskbackend.common.util.CookieUtil.addAccessToken;
import static com.moduletest.deasungkioskbackend.common.util.CookieUtil.clearAccessToken;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginRequest;
import com.moduletest.deasungkioskbackend.domain.kiosk.dto.KioskLoginResponse;
import com.moduletest.deasungkioskbackend.domain.kiosk.service.KioskAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[키오스크] 인증", description = "키오스크 지점 로그인 (인증 불필요)")
@RestController
@RequestMapping("/api/v1/kiosk/auth")
@RequiredArgsConstructor
public class KioskAuthController {

    private final KioskAuthService kioskAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "키오스크 로그인",
        description = "지점 코드와 PIN으로 로그인한다. 성공 시 JWT 쿠키가 설정되고 지점 정보를 반환한다.")
    @PostMapping("/login")
    public CommonResponse<KioskLoginResponse> login(
        @Valid @RequestBody KioskLoginRequest request,
        HttpServletResponse response) {
        KioskAuthService.KioskLoginResult result = kioskAuthService.login(request);
        addAccessToken(response, result.token(), jwtTokenProvider.getKioskExpiration());
        return CommonResponse.success(result.storeInfo());
    }

    @Operation(summary = "키오스크 내 정보 조회",
        description = "현재 로그인된 키오스크의 지점 정보를 반환한다. 페이지 새로고침 시 상태 복구에 사용한다.")
    @GetMapping("/me")
    public CommonResponse<KioskLoginResponse> me() {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        return CommonResponse.success(kioskAuthService.findCurrentStore(storeId));
    }

    @Operation(summary = "키오스크 로그아웃",
        description = "Redis에서 해당 키오스크의 토큰만 삭제하고 쿠키를 초기화한다. 다른 키오스크의 세션에는 영향 없음.")
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Long storeId = SecurityUtil.getStoreIdFromToken();
        String token = resolveTokenFromCookie(request);
        kioskAuthService.logout(storeId, token);
        clearAccessToken(response);
        return CommonResponse.success(null);
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
