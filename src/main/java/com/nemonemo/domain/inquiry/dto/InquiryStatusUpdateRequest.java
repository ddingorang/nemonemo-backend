// Created: 2026-04-07 23:03:08
package com.nemonemo.domain.inquiry.dto;

import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InquiryStatusUpdateRequest {

    @NotNull(message = "변경할 상태를 선택해 주세요.")
    private InquiryStatus status;
}
