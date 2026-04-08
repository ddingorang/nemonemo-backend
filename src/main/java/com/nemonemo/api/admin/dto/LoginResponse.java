// Created: 2026-04-07 23:02:29
package com.nemonemo.api.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String name;
}
