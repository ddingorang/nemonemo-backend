// Created: 2026-04-19 20:59:59
package com.nemonemo.domain.contract.repository;

import com.nemonemo.domain.contract.dto.ContractStatRaw;
import com.nemonemo.domain.contract.entity.QContract;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContractQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QContract contract = QContract.contract;

    public List<ContractStatRaw> findMonthlyStats(LocalDate from, LocalDate to) {
        NumberTemplate<Integer> month = Expressions.numberTemplate(Integer.class, "MONTH({0})", contract.startDate);

        return queryFactory
                .select(Projections.constructor(ContractStatRaw.class,
                        month,
                        contract.unit.size,
                        contract.count(),
                        contract.totalPrice.sum()))
                .from(contract)
                .where(contract.startDate.between(from, to))
                .groupBy(month, contract.unit.size)
                .orderBy(month.asc())
                .fetch();
    }

    public List<ContractStatRaw> findQuarterlyStats(LocalDate from, LocalDate to) {
        NumberTemplate<Integer> quarter = Expressions.numberTemplate(Integer.class, "QUARTER({0})", contract.startDate);

        return queryFactory
                .select(Projections.constructor(ContractStatRaw.class,
                        quarter,
                        contract.unit.size,
                        contract.count(),
                        contract.totalPrice.sum()))
                .from(contract)
                .where(contract.startDate.between(from, to))
                .groupBy(quarter, contract.unit.size)
                .orderBy(quarter.asc())
                .fetch();
    }
}
