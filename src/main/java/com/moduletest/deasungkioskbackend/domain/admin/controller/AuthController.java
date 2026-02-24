package com.moduletest.deasungkioskbackend.domain.admin.controller;


import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.util.CookieUtil;
import com.moduletest.deasungkioskbackend.domain.admin.dto.LoginRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.SignupRequest;
import com.moduletest.deasungkioskbackend.domain.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Operation(summary = "관리자 로그아웃",
        description = "Redis에서 토큰을 삭제하고 쿠키를 초기화한다.")
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = Long.valueOf(authentication.getName());
            authService.logout(userId);
        }
        CookieUtil.clearAccessToken(response);
        CookieUtil.clearRefreshToken(response);
        return CommonResponse.success(null);
    }
}
