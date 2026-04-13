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
import com.nemonemo.domain.inquiry.repository.InquiryRepository;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContractService {

    private final ContractRepository contractRepository;
    private final UnitRepository unitRepository;
    private final InquiryRepository inquiryRepository;

    public List<ContractResponse> getContracts(ContractStatus status, Long unitId) {
        return contractRepository.findAllByFilter(status, unitId)
                .stream().map(ContractResponse::from).toList();
    }

    public ContractResponse getContract(Long id) {
        return contractRepository.findById(id)
                .map(ContractResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));
    }

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
            inquiry = inquiryRepository.findById(request.getInquiryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        }

        Contract contract = Contract.builder()
                .unit(unit)
                .inquiry(inquiry)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .monthlyPrice(request.getMonthlyPrice())
                .build();

        contractRepository.save(contract);
        unit.changeStatus(UnitStatus.OCCUPIED);

        return ContractResponse.from(contract);
    }

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

        contract.update(newUnit, request.getCustomerName(), request.getCustomerPhone(), request.getCustomerEmail(),
                request.getStartDate(), request.getEndDate(), request.getMonthlyPrice());

        return ContractResponse.from(contract);
    }

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

    @Scheduled(cron = "0 0 1 * * *")
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
}
