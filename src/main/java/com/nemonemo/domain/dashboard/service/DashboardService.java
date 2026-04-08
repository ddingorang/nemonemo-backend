// Created: 2026-04-08 22:49:09
package com.nemonemo.domain.dashboard.service;

import com.nemonemo.domain.contract.dto.ContractResponse;
import com.nemonemo.domain.contract.repository.ContractRepository;
import com.nemonemo.domain.dashboard.dto.DashboardResponse;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.inquiry.repository.InquiryRepository;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UnitRepository unitRepository;
    private final ContractRepository contractRepository;
    private final InquiryRepository inquiryRepository;

    public DashboardResponse getDashboard() {
        DashboardResponse.UnitSummary unitSummary = DashboardResponse.UnitSummary.builder()
                .total(unitRepository.countByIsActiveTrue())
                .available(unitRepository.countByIsActiveTrueAndStatus(UnitStatus.AVAILABLE))
                .occupied(unitRepository.countByIsActiveTrueAndStatus(UnitStatus.OCCUPIED))
                .reserved(unitRepository.countByIsActiveTrueAndStatus(UnitStatus.RESERVED))
                .maintenance(unitRepository.countByIsActiveTrueAndStatus(UnitStatus.MAINTENANCE))
                .build();

        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        List<ContractResponse> expiringThisMonth = contractRepository
                .findAllExpiringSoon(today, endOfMonth)
                .stream().map(ContractResponse::from).toList();

        long pendingInquiryCount = inquiryRepository.countByStatus(InquiryStatus.PENDING);

        return DashboardResponse.builder()
                .unitSummary(unitSummary)
                .expiringThisMonth(expiringThisMonth)
                .pendingInquiryCount(pendingInquiryCount)
                .build();
    }
}
