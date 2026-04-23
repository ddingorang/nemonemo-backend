// Created: 2026-04-07 23:03:55
package com.nemonemo.domain.unit.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.contract.repository.ContractRepository;
import com.nemonemo.domain.unit.dto.UnitCreateRequest;
import com.nemonemo.domain.unit.dto.UnitResponse;
import com.nemonemo.domain.unit.dto.UnitStatusUpdateRequest;
import com.nemonemo.domain.unit.dto.UnitUpdateRequest;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import com.nemonemo.domain.warehouse.entity.Warehouse;
import com.nemonemo.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUnitService {

    private final UnitRepository unitRepository;
    private final WarehouseRepository warehouseRepository;
    private final ContractRepository contractRepository;

    // 활성 계약 정보 포함하여 사이즈/상태 필터로 유닛 목록 조회
    public List<UnitResponse> getUnits(UnitSize size, UnitStatus status) {
        Map<Long, Contract> activeContracts = contractRepository.findAllActive()
                .stream().collect(Collectors.toMap(c -> c.getUnit().getId(), c -> c));

        return unitRepository.findAllByFilter(size, status).stream()
                .map(u -> UnitResponse.from(u, false, activeContracts.get(u.getId())))
                .toList();
    }

    // 창고에 새 유닛 등록
    @Transactional
    public UnitResponse createUnit(UnitCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Unit unit = Unit.builder()
                .warehouse(warehouse)
                .unitNumber(request.getUnitNumber())
                .size(request.getSize())
                .zone(request.getZone())
                .monthlyPrice(request.getMonthlyPrice())
                .build();

        return UnitResponse.from(unitRepository.save(unit));
    }

    // 유닛 번호, 구역, 월 가격 수정
    @Transactional
    public UnitResponse updateUnit(Long id, UnitUpdateRequest request) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        unit.update(request.getUnitNumber(), request.getZone(), request.getMonthlyPrice());

        return UnitResponse.from(unit);
    }

    // 유닛 상태 변경
    @Transactional
    public UnitResponse updateUnitStatus(Long id, UnitStatusUpdateRequest request) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        unit.changeStatus(request.getStatus());
        return UnitResponse.from(unit);
    }

    // 대시보드용: 활성 유닛 총 수
    public long countActive() {
        return unitRepository.countByIsActiveTrue();
    }

    // 대시보드용: 상태별 활성 유닛 수
    public long countActiveByStatus(UnitStatus status) {
        return unitRepository.countByIsActiveTrueAndStatus(status);
    }
}
