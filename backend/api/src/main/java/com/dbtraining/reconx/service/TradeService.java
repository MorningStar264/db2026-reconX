package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {
        // Check for duplicate tradeRef
        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
            throw new DuplicateTradeRefException("Trade reference already exists: " + req.tradeRef());
        }

        // Look up counterparty and instrument
        Counterparty counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found: " + req.counterpartyId()));
        
        Instrument instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException("Instrument not found: " + req.instrumentId()));

        // Build new Trade
        Trade trade = new Trade();
        trade.setTradeRef(req.tradeRef());
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());
        trade.setStatus(TradeStatus.PENDING);

        Trade saved = tradeRepo.save(trade);
        
        // Metrics
        metrics.incrementTradeCreated();
        metrics.recordTradeValue(saved.getQuantity().multiply(saved.getPrice()).doubleValue());
        
        // Publish event
        events.publish(new TradeEvent(
            UUID.randomUUID(), 
            saved.getTradeRef(),
            TradeEvent.EventType.TRADE_CREATED, 
            Instant.now(), 
            actor, 
            null, 
            null
        ));
        
        return saved;
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found: " + id));

        // Update fields
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());
        
        // Update counterparty if changed
        if (!trade.getCounterparty().getId().equals(req.counterpartyId())) {
            Counterparty counterparty = cpRepo.findById(req.counterpartyId())
                    .orElseThrow(() -> new TradeNotFoundException("Counterparty not found: " + req.counterpartyId()));
            trade.setCounterparty(counterparty);
        }
        
        // Update instrument if changed
        if (!trade.getInstrument().getId().equals(req.instrumentId())) {
            Instrument instrument = instRepo.findById(req.instrumentId())
                    .orElseThrow(() -> new TradeNotFoundException("Instrument not found: " + req.instrumentId()));
            trade.setInstrument(instrument);
        }

        Trade saved = tradeRepo.save(trade);
        
        // Publish event
        events.publish(new TradeEvent(
            UUID.randomUUID(), 
            saved.getTradeRef(),
            TradeEvent.EventType.TRADE_UPDATED, 
            Instant.now(), 
            actor, 
            null, 
            null
        ));
        
        return saved;
    }

    public Trade updateStatus(Long id, String status, String actor) {
        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found: " + id));
        
        TradeStatus newStatus = TradeStatus.valueOf(status.toUpperCase());
        trade.setStatus(newStatus);
        
        Trade saved = tradeRepo.save(trade);
        
        // Publish event with new status
        events.publish(new TradeEvent(
            UUID.randomUUID(), 
            saved.getTradeRef(),
            TradeEvent.EventType.TRADE_UPDATED, 
            Instant.now(), 
            actor, 
            null, 
            status
        ));
        
        return saved;
    }

    public void softDelete(Long id, String actor) {
        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("Trade not found: " + id));
        
        trade.setStatus(TradeStatus.FAILED);
        Trade saved = tradeRepo.save(trade);
        
        events.publish(new TradeEvent(
            UUID.randomUUID(), 
            saved.getTradeRef(),
            TradeEvent.EventType.TRADE_CANCELLED, 
            Instant.now(), 
            actor, 
            null, 
            null
        ));
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        Specification<Trade> spec = Specification.where(null);
        
        if (from != null || to != null) {
            spec = spec.and(tradeDateBetween(from, to));
        }
        
        if (status != null && !status.isEmpty()) {
            try {
                TradeStatus tradeStatus = TradeStatus.valueOf(status.toUpperCase());
                spec = spec.and(hasStatus(tradeStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        if (counterpartyId != null) {
            spec = spec.and(forCounterparty(counterpartyId));
        }
        
        return tradeRepo.findAll(spec, pageable);
    }
}