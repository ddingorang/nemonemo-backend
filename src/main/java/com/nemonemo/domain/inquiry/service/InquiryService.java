// Created: 2026-04-07 22:43:02
package com.nemonemo.domain.inquiry.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.inquiry.dto.InquiryRequest;
import com.nemonemo.domain.inquiry.dto.InquiryResponse;
import com.nemonemo.domain.inquiry.entity.Inquiry;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.inquiry.repository.InquiryRepository;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UnitService unitService;

    // 중복 문의 방지 후 예약 문의 저장
    @Transactional
    public InquiryResponse submitInquiry(InquiryRequest request) {
        boolean hasPending = inquiryRepository.existsByCustomerPhoneAndStatusIn(
                request.getCustomerPhone(),
                List.of(InquiryStatus.PENDING, InquiryStatus.IN_PROGRESS)
        );
        if (hasPending) {
            throw new BusinessException(ErrorCode.INQUIRY_DUPLICATE);
        }

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitService.getUnitEntity(request.getUnitId());
        }

        if (unit == null && request.getDesiredSize() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Inquiry inquiry = Inquiry.builder()
                .unit(unit)
                .desiredSize(unit != null ? unit.getSize() : request.getDesiredSize())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .desiredStartDate(request.getDesiredStartDate())
                .desiredDurationMonths(request.getDesiredDurationMonths())
                .message(request.getMessage())
                .build();

        return InquiryResponse.from(inquiryRepository.save(inquiry));
    }

    // 문의 ID와 연락처 일치 여부 확인 후 문의 상태 조회
    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(Long id, String customerPhone) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.getCustomerPhone().equals(customerPhone)) {
            throw new BusinessException(ErrorCode.INQUIRY_NOT_FOUND);
        }

        return InquiryResponse.from(inquiry);
    }
}
