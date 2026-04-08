// Created: 2026-04-07 22:42:42
package com.nemonemo.domain.inquiry.dto;

import com.nemonemo.domain.unit.entity.UnitSize;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InquiryRequest {

    private Long unitId;

    private UnitSize desiredSize;

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
    private String customerName;

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^\\d{10,11}$", message = "연락처는 숫자 10~11자리로 입력해 주세요.")
    private String customerPhone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String customerEmail;

    @NotNull(message = "희망 시작일을 입력해 주세요.")
    @Future(message = "희망 시작일은 오늘 이후 날짜여야 합니다.")
    private LocalDate desiredStartDate;

    @NotNull(message = "희망 사용 기간을 입력해 주세요.")
    @Min(value = 1, message = "희망 사용 기간은 최소 1개월 이상이어야 합니다.")
    @Max(value = 60, message = "희망 사용 기간은 최대 60개월까지 입력 가능합니다.")
    private Integer desiredDurationMonths;

    @Size(max = 1000, message = "문의 내용은 1000자 이내로 입력해 주세요.")
    private String message;
}
