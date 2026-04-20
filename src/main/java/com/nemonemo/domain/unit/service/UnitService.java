// Created: 2026-04-07 22:42:55
package com.nemonemo.domain.unit.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.contract.repository.ContractRepository;
import com.nemonemo.domain.unit.dto.UnitResponse;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitService {

    private final UnitRepository unitRepository;
    private final ContractRepository contractRepository;

    // 만료 임박 표시 포함 사이즈/상태 필터로 유닛 목록 조회
    public List<UnitResponse> getUnits(UnitSize size, UnitStatus status) {
        Set<Long> expiringSoonUnitIds = getExpiringSoonUnitIds();

        List<Unit> units;
        if (size != null && status != null) {
            units = unitRepository.findAllByIsActiveTrueAndSizeAndStatus(size, status);
        } else if (size != null) {
            units = unitRepository.findAllByIsActiveTrueAndSize(size);
        } else if (status != null) {
            units = unitRepository.findAllByIsActiveTrueAndStatus(status);
        } else {
            units = unitRepository.findAllByIsActiveTrue();
        }

        return units.stream()
                .map(u -> UnitResponse.from(u, expiringSoonUnitIds.contains(u.getId())))
                .toList();
    }

    // 특정 유닛 상세 및 만료 임박 여부 조회
    public UnitResponse getUnit(Long id) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));
        Set<Long> expiringSoonUnitIds = getExpiringSoonUnitIds();
        return UnitResponse.from(unit, expiringSoonUnitIds.contains(unit.getId()));
    }

    // 14일 내 계약 만료 유닛 ID 목록 조회
    private Set<Long> getExpiringSoonUnitIds() {
        LocalDate today = LocalDate.now();
        return contractRepository.findUnitIdsExpiringSoon(today, today.plusDays(14))
                .stream().collect(Collectors.toSet());
    }
}
