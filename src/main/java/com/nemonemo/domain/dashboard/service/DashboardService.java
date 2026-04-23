// Created: 2026-04-08 22:49:09
package com.nemonemo.domain.dashboard.service;

import com.nemonemo.domain.contract.dto.ContractResponse;
import com.nemonemo.domain.contract.service.AdminContractService;
import com.nemonemo.domain.dashboard.dto.DashboardResponse;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.inquiry.service.AdminInquiryService;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.service.AdminUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AdminUnitService adminUnitService;
    private final AdminContractService adminContractService;
    private final AdminInquiryService adminInquiryService;

    // 유닛 현황, 이달 만료 계약, 미처리 문의 수 집계
    public DashboardResponse getDashboard() {
        DashboardResponse.UnitSummary unitSummary = DashboardResponse.UnitSummary.builder()
                .total(adminUnitService.countActive())
                .available(adminUnitService.countActiveByStatus(UnitStatus.AVAILABLE))
                .occupied(adminUnitService.countActiveByStatus(UnitStatus.OCCUPIED))
                .reserved(adminUnitService.countActiveByStatus(UnitStatus.RESERVED))
                .disabled(adminUnitService.countActiveByStatus(UnitStatus.DISABLED))
                .build();

        LocalDate today = LocalDate.now();
        List<ContractResponse> expiringThisMonth = adminContractService.getExpiringSoon(today, today.plusDays(7));

        long pendingInquiryCount = adminInquiryService.countByStatus(InquiryStatus.PENDING);

        return DashboardResponse.builder()
                .unitSummary(unitSummary)
                .expiringThisMonth(expiringThisMonth)
                .pendingInquiryCount(pendingInquiryCount)
                .build();
    }
}
