// Created: 2026-04-08 22:49:16
package com.nemonemo.api.admin;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.dashboard.dto.ContractStatsResponse;
import com.nemonemo.domain.dashboard.dto.DashboardResponse;
import com.nemonemo.domain.dashboard.service.DashboardService;
import com.nemonemo.domain.dashboard.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "[관리자] 대시보드", description = "관리자 대시보드 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final StatisticsService statisticsService;

    @Operation(summary = "대시보드 현황 조회")
    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.ok(dashboardService.getDashboard());
    }

    @Operation(summary = "월별 계약 통계 조회", description = "연도별 월별 계약 건수 및 계약 금액 통계")
    @GetMapping("/stats/monthly")
    public ApiResponse<ContractStatsResponse> getMonthlyStats(
            @Parameter(description = "조회 연도 (기본값: 현재 연도)")
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ApiResponse.ok(statisticsService.getMonthlyStats(targetYear));
    }

    @Operation(summary = "분기별 계약 통계 조회", description = "연도별 분기별 계약 건수 및 계약 금액 통계")
    @GetMapping("/stats/quarterly")
    public ApiResponse<ContractStatsResponse> getQuarterlyStats(
            @Parameter(description = "조회 연도 (기본값: 현재 연도)")
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ApiResponse.ok(statisticsService.getQuarterlyStats(targetYear));
    }
}
