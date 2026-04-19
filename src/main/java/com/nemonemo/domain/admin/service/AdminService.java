// Created: 2026-04-07 23:02:36
package com.nemonemo.domain.admin.service;

import com.nemonemo.api.admin.dto.LoginRequest;
import com.nemonemo.api.admin.dto.LoginResponse;
import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.common.security.JwtProvider;
import com.nemonemo.domain.admin.entity.Admin;
import com.nemonemo.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 아이디/비밀번호 검증 후 JWT 토큰 발급
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return LoginResponse.builder()
                .accessToken(jwtProvider.generateToken(admin.getUsername()))
                .name(admin.getName())
                .build();
    }
}
