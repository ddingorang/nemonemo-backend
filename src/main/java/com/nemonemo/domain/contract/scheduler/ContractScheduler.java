package com.nemonemo.domain.contract.scheduler;

import com.nemonemo.domain.contract.service.AdminContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final AdminContractService adminContractService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processExpiredContracts() {
        adminContractService.processExpiredContracts();
    }
}
