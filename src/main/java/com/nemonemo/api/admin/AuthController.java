// Created: 2026-04-07 23:02:59
package com.nemonemo.api.admin;

import com.nemonemo.api.admin.dto.LoginRequest;
import com.nemonemo.api.admin.dto.LoginResponse;
import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[관리자] 인증", description = "관리자 로그인 API")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;

    @Operation(summary = "관리자 로그인", description = "아이디/비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(adminService.login(request));
    }
}
