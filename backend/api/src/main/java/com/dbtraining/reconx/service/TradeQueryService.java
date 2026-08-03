package com.dbtraining.reconx.service;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import com.dbtraining.reconx.repository.TradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TradeQueryService {

    private final TradeRepository tradeRepository;

    public TradeQueryService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Page<Trade> search(LocalDate from, LocalDate to, TradeStatus status, Long counterpartyId, Pageable pageable) {
        return tradeRepository.findByFilters(from, to, status, counterpartyId, pageable);
    }

    public void softDelete(Long id, String principal) {
        tradeRepository.findById(id).ifPresent(trade -> {
            trade.setStatus(TradeStatus.FAILED);
            tradeRepository.save(trade);
        });
    }
}