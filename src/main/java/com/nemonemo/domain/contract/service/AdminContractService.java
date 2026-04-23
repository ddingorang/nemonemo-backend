// Created: 2026-04-08 22:46:22
package com.nemonemo.domain.contract.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.contract.dto.ContractCreateRequest;
import com.nemonemo.domain.contract.dto.ContractResponse;
import com.nemonemo.domain.contract.dto.ContractUpdateRequest;
import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.contract.entity.ContractStatus;
import com.nemonemo.domain.contract.repository.ContractRepository;
import com.nemonemo.domain.inquiry.entity.Inquiry;
import com.nemonemo.domain.inquiry.service.AdminInquiryService;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContractService {

    private final ContractRepository contractRepository;
    private final UnitRepository unitRepository;
    private final AdminInquiryService adminInquiryService;

    // 상태/유닛 ID/월 필터로 계약 목록 페이지네이션 조회
    public Page<ContractResponse> getContracts(ContractStatus status, Long unitId, String yearMonth, Pageable pageable) {
        if (yearMonth != null) {
            YearMonth ym = YearMonth.parse(yearMonth);
            return contractRepository.findAllByMonth(status,
                    ym.atDay(1).atStartOfDay(),
                    ym.plusMonths(1).atDay(1).atStartOfDay(),
                    pageable).map(ContractResponse::from);
        }
        return contractRepository.findAllByFilter(status, unitId, pageable)
                .map(ContractResponse::from);
    }

    // 특정 계약 상세 조회
    public ContractResponse getContract(Long id) {
        return contractRepository.findById(id)
                .map(ContractResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    // 유닛 가용 여부 확인 후 계약 생성 및 유닛 점유 처리
    @Transactional
    public ContractResponse createContract(ContractCreateRequest request) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(request.getUnitId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        if (unit.getStatus() != UnitStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.UNIT_NOT_AVAILABLE);
        }

        if (contractRepository.existsByUnitIdAndStatus(unit.getId(), ContractStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.CONTRACT_ALREADY_ACTIVE);
        }

        Inquiry inquiry = null;
        if (request.getInquiryId() != null) {
            inquiry = adminInquiryService.getInquiryEntity(request.getInquiryId());
        }

        Contract contract = Contract.builder()
                .unit(unit)
                .inquiry(inquiry)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerAddress(request.getCustomerAddress())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice(request.getTotalPrice())
                .memo(request.getMemo())
                .build();

        contractRepository.save(contract);
        unit.changeStatus(UnitStatus.OCCUPIED);

        return ContractResponse.from(contract);
    }

    // 계약 내용 및 유닛 변경 처리
    @Transactional
    public ContractResponse updateContract(Long id, ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }

        Unit newUnit = unitRepository.findByIdAndIsActiveTrue(request.getUnitId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));

        Unit oldUnit = contract.getUnit();
        if (!oldUnit.getId().equals(newUnit.getId())) {
            if (newUnit.getStatus() != UnitStatus.AVAILABLE) {
                throw new BusinessException(ErrorCode.UNIT_NOT_AVAILABLE);
            }
            oldUnit.changeStatus(UnitStatus.AVAILABLE);
            newUnit.changeStatus(UnitStatus.OCCUPIED);
        }

        contract.update(newUnit, request.getCustomerName(), request.getCustomerPhone(), request.getCustomerAddress(),
                request.getStartDate(), request.getEndDate(), request.getTotalPrice(), request.getMemo());

        return ContractResponse.from(contract);
    }

    // 계약 해지 및 유닛 상태 복원
    @Transactional
    public ContractResponse terminateContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONTRACT_NOT_ACTIVE);
        }

        contract.terminate();
        contract.getUnit().changeStatus(UnitStatus.AVAILABLE);

        return ContractResponse.from(contract);
    }

    // 만료된 계약 일괄 처리 — ContractScheduler에서 @Scheduled로 호출
    @Transactional
    public void processExpiredContracts() {
        List<Contract> expired = contractRepository.findAllExpired(LocalDate.now());
        for (Contract contract : expired) {
            contract.expire();
            contract.getUnit().changeStatus(UnitStatus.AVAILABLE);
        }
        if (!expired.isEmpty()) {
            log.info("만료 계약 처리 완료: {}건", expired.size());
        }
    }

    // 유닛에 연관된 계약 전체 삭제 후 유닛 상태 초기화 (AdminUnitController에서 호출)
    @Transactional
    public void deleteContractsByUnit(Long unitId) {
        Unit unit = unitRepository.findByIdAndIsActiveTrue(unitId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));
        contractRepository.deleteAllByUnitId(unit.getId());
        unit.changeStatus(UnitStatus.AVAILABLE);
    }

    // 대시보드용: 만료 임박 계약 목록 조회
    public List<ContractResponse> getExpiringSoon(LocalDate from, LocalDate to) {
        return contractRepository.findAllExpiringSoon(from, to).stream()
                .map(ContractResponse::from)
                .toList();
    }
}
