// Created: 2026-04-07 22:42:06
package com.nemonemo.domain.inquiry.entity;

public enum InquiryStatus {
    PENDING,      // 접수 대기
    IN_PROGRESS,  // 상담 진행 중
    COMPLETED,    // 완료 (계약 체결 또는 종결)
    CANCELLED     // 취소
}
