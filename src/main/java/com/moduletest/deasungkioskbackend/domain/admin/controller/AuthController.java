package com.moduletest.deasungkioskbackend.domain.admin.controller;


import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.JwtTokenProvider;
import com.moduletest.deasungkioskbackend.common.util.CookieUtil;
import com.moduletest.deasungkioskbackend.domain.admin.dto.LoginRequest;
import com.moduletest.deasungkioskbackend.domain.admin.dto.SignupRequest;
import com.moduletest.deasungkioskbackend.domain.admin.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/signup")
    public CommonResponse<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return CommonResponse.success(null);
    }

    @PostMapping("/login")
    public CommonResponse<Void> login(@Valid @RequestBody LoginRequest loginRequest,
                                      HttpServletResponse response) {
        String[] tokens = authService.login(loginRequest);
        CookieUtil.addAccessToken(response, tokens[0], jwtTokenProvider.getAccessExpiration());
        CookieUtil.addRefreshToken(response, tokens[1], jwtTokenProvider.getRefreshExpiration());
        return CommonResponse.success(null);
    }
}
