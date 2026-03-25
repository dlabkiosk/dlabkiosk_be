package com.moduletest.deasungkioskbackend.domain.admin.controller;


import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.util.CookieUtil;
import com.moduletest.deasungkioskbackend.domain.admin.dto.AdminUserResponse;
import com.moduletest.deasungkioskbackend.domain.admin.dto.LoginRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.SignupRequest;
import com.moduletest.deasungkioskbackend.domain.admin.exception.AdminException;
import com.moduletest.deasungkioskbackend.domain.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 인증", description = "관리자 회원가입 및 로그인 (JWT 쿠키 발급)")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "관리자 회원가입", description = "loginId, password, name으로 관리자 계정을 생성한다.")
    @PostMapping("/signup")
    public CommonResponse<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return CommonResponse.success(null);
    }

    @Operation(summary = "관리자 로그인",
        description = "로그인 성공 시 accessToken, refreshToken을 HttpOnly 쿠키로 설정한다.")
    @PostMapping("/login")
    public CommonResponse<Void> login(@Valid @RequestBody LoginRequest loginRequest,
                                      HttpServletResponse response) {
        String[] tokens = authService.login(loginRequest);
        CookieUtil.addAccessToken(response, tokens[0], jwtTokenProvider.getAccessExpiration());
        CookieUtil.addRefreshToken(response, tokens[1], jwtTokenProvider.getRefreshExpiration());
        return CommonResponse.success(null);
    }

    @Operation(summary = "관리자 토큰 재발급",
        description = "refreshToken 쿠키로 accessToken을 재발급한다. accessToken 만료 시 호출한다.")
    @PostMapping("/refresh")
    public CommonResponse<Void> refresh(HttpServletRequest request,
                                        HttpServletResponse response) {
        String refreshToken = CookieUtil.extractCookie(request, "refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AdminException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = authService.refresh(refreshToken);
        CookieUtil.addAccessToken(response, newAccessToken, jwtTokenProvider.getAccessExpiration());
        return CommonResponse.success(null);
    }

    @Operation(summary = "내 정보 조회",
        description = "현재 로그인된 관리자의 정보를 반환한다.")
    @GetMapping("/me")
    public CommonResponse<AdminUserResponse> me() {
        Long userId = Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getName());
        return CommonResponse.success(authService.findCurrentUser(userId));
    }

    @Operation(summary = "관리자 로그아웃",
        description = "Redis에서 현재 세션의 토큰만 삭제하고 쿠키를 초기화한다. 다른 기기의 세션에는 영향 없음.")
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = Long.valueOf(authentication.getName());
            String accessToken = CookieUtil.extractCookie(request, "accessToken");
            String refreshToken = CookieUtil.extractCookie(request, "refreshToken");
            authService.logout(userId, accessToken, refreshToken);
        }
        CookieUtil.clearAccessToken(response);
        CookieUtil.clearRefreshToken(response);
        return CommonResponse.success(null);
    }
}
