package com.safewhale.auth.controller;

import com.safewhale.auth.service.AuthService;
import com.safewhale.common.response.ApiResponse;
import com.safewhale.common.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/google")
    ApiResponse<JwtTokenProvider.TokenPair> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ApiResponse.ok(authService.loginWithGoogle(request.idToken()));
    }

    @PostMapping("/auth/refresh")
    ApiResponse<JwtTokenProvider.TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/admin/auth/login")
    ApiResponse<JwtTokenProvider.TokenPair> admin(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(authService.loginAdmin(request.loginId(), request.password()));
    }

    record GoogleLoginRequest(@NotBlank String idToken) {}
    record RefreshRequest(@NotBlank String refreshToken) {}
    record AdminLoginRequest(@NotBlank String loginId, @NotBlank String password) {}
}
