// Created: 2026-04-07 23:03:09
package com.nemonemo.domain.inquiry.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class InquiryMemoUpdateRequest {

    @Size(max = 2000, message = "메모는 2000자 이내로 입력해 주세요.")
    private String adminMemo;
}
