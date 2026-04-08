// Created: 2026-04-07 22:42:45
package com.nemonemo.domain.inquiry.dto;

import com.nemonemo.domain.inquiry.entity.Inquiry;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.unit.entity.UnitSize;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryResponse {

    private Long id;
    private Long unitId;
    private String unitNumber;
    private UnitSize desiredSize;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate desiredStartDate;
    private Integer desiredDurationMonths;
    private String message;
    private InquiryStatus status;
    private LocalDateTime createdAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .unitId(inquiry.getUnit() != null ? inquiry.getUnit().getId() : null)
                .unitNumber(inquiry.getUnit() != null ? inquiry.getUnit().getUnitNumber() : null)
                .desiredSize(inquiry.getDesiredSize())
                .customerName(inquiry.getCustomerName())
                .customerPhone(inquiry.getCustomerPhone())
                .customerEmail(inquiry.getCustomerEmail())
                .desiredStartDate(inquiry.getDesiredStartDate())
                .desiredDurationMonths(inquiry.getDesiredDurationMonths())
                .message(inquiry.getMessage())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
