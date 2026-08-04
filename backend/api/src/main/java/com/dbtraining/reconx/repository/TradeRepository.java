package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TradeRepository
        extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    Optional<Trade> findByTradeRef(String tradeRef);

    @Query("""
        SELECT t FROM Trade t
        WHERE (t.tradeDate BETWEEN :from AND :to OR (:from IS NULL AND :to IS NULL))
          AND (:status IS NULL OR t.status = :status)
          AND (:counterpartyId IS NULL OR t.counterparty.id = :counterpartyId)
        """)
    Page<Trade> findByFilters(
        @Param("from")           LocalDate from,
        @Param("to")             LocalDate to,
        @Param("status")         TradeStatus status,
        @Param("counterpartyId") Long counterpartyId,
        Pageable pageable
    );

    // Add this method for the gauge
    long countByStatus(TradeStatus status);
}