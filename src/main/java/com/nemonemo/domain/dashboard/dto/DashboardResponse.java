// Created: 2026-04-08 22:48:46
package com.nemonemo.domain.dashboard.dto;

import com.nemonemo.domain.contract.dto.ContractResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private UnitSummary unitSummary;
    private List<ContractResponse> expiringThisMonth;
    private long pendingInquiryCount;

    @Getter
    @Builder
    public static class UnitSummary {
        private long total;
        private long available;
        private long occupied;
        private long reserved;
        private long disabled;
    }
}
