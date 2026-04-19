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

    // 상태/사이즈 필터로 문의 목록 조회
    public List<InquiryResponse> getInquiries(InquiryStatus status, UnitSize size) {
        return inquiryRepository.findAllByFilter(status, size)
                .stream().map(InquiryResponse::from).toList();
    }

    // 특정 문의 상세 조회
    public InquiryResponse getInquiry(Long id) {
        return inquiryRepository.findById(id)
                .map(InquiryResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }

    // 문의 처리 상태 변경
    @Transactional
    public InquiryResponse updateStatus(Long id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.changeStatus(request.getStatus());
        return InquiryResponse.from(inquiry);
    }

    // 관리자 메모 수정
    @Transactional
    public InquiryResponse updateMemo(Long id, InquiryMemoUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.updateAdminMemo(request.getAdminMemo());
        return InquiryResponse.from(inquiry);
    }
}
