// Created: 2026-04-07 23:04:00
package com.nemonemo.domain.inquiry.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.inquiry.dto.InquiryMemoUpdateRequest;
import com.nemonemo.domain.inquiry.dto.InquiryResponse;
import com.nemonemo.domain.inquiry.dto.InquiryStatusUpdateRequest;
import com.nemonemo.domain.inquiry.entity.Inquiry;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.inquiry.repository.InquiryRepository;
import com.nemonemo.domain.unit.entity.UnitSize;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;

    public List<InquiryResponse> getInquiries(InquiryStatus status, UnitSize size) {
        return inquiryRepository.findAllByFilter(status, size)
                .stream().map(InquiryResponse::from).toList();
    }

    public InquiryResponse getInquiry(Long id) {
        return inquiryRepository.findById(id)
                .map(InquiryResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }

    @Transactional
    public InquiryResponse updateStatus(Long id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.changeStatus(request.getStatus());
        return InquiryResponse.from(inquiry);
    }

    @Transactional
    public InquiryResponse updateMemo(Long id, InquiryMemoUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.updateAdminMemo(request.getAdminMemo());
        return InquiryResponse.from(inquiry);
    }
}
