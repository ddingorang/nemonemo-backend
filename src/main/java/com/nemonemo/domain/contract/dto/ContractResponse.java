// Created: 2026-04-08 22:46:07
package com.nemonemo.domain.contract.dto;

import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.contract.entity.ContractStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ContractResponse {

    private Long id;
    private Long unitId;
    private String unitNumber;
    private Long inquiryId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private ContractStatus status;
    private boolean expiringSoon;
    private LocalDateTime createdAt;

    public static ContractResponse from(Contract contract) {
        boolean expiringSoon = contract.getStatus() == ContractStatus.ACTIVE
                && contract.getEndDate().minusDays(30).isBefore(LocalDate.now().plusDays(1));

        return ContractResponse.builder()
                .id(contract.getId())
                .unitId(contract.getUnit().getId())
                .unitNumber(contract.getUnit().getUnitNumber())
                .inquiryId(contract.getInquiry() != null ? contract.getInquiry().getId() : null)
                .customerName(contract.getCustomerName())
                .customerPhone(contract.getCustomerPhone())
                .customerAddress(contract.getCustomerAddress())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .totalPrice(contract.getTotalPrice())
                .status(contract.getStatus())
                .expiringSoon(expiringSoon)
                .createdAt(contract.getCreatedAt())
                .build();
    }
}
