// Created: 2026-04-08 22:45:54
package com.nemonemo.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ContractCreateRequest {

    @NotNull
    private Long unitId;

    private Long inquiryId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerPhone;

    private String customerAddress;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private BigDecimal totalPrice;

    private String memo;
}
