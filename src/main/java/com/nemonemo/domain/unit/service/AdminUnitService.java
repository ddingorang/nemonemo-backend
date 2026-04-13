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

    public List<UnitResponse> getUnits(UnitSize size, UnitStatus status) {
        Map<Long, Contract> activeContracts = contractRepository.findAllActive()
                .stream().collect(Collectors.toMap(c -> c.getUnit().getId(), c -> c));

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
                .map(u -> UnitResponse.from(u, false, activeContracts.get(u.getId())))
                .toList();
    }

    @Transactional
    public UnitResponse createUnit(UnitCreateRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Unit unit = Unit.builder()
                .warehouse(warehouse)
                .unitNumber(request.getUnitNumber())
                .size(request.getSize())
                .areaSqm(request.getAreaSqm())
                .floor(request.getFloor())
                .zone(request.getZone())
                .monthlyPrice(request.getMonthlyPrice())
                .build();

        return UnitResponse.from(unitRepository.save(unit));
    }

    @Transactional
    public UnitResponse updateUnit(Long id, UnitUpdateRequest request) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        unit.update(request.getUnitNumber(), request.getAreaSqm(),
                request.getFloor(), request.getZone(), request.getMonthlyPrice());

        return UnitResponse.from(unit);
    }

    @Transactional
    public UnitResponse updateUnitStatus(Long id, UnitStatusUpdateRequest request) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        unit.changeStatus(request.getStatus());
        return UnitResponse.from(unit);
    }

    @Transactional
    public void deleteUnitContracts(Long id) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));
        contractRepository.deleteAllByUnitId(unit.getId());
        unit.changeStatus(UnitStatus.AVAILABLE);
    }
}
