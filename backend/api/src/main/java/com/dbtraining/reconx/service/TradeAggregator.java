package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.respository.entity.AuditLogEntry;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TradeAggregator {

    private static final Logger log = LoggerFactory.getLogger(TradeAggregator.class);
    
    private final AuditLogRepository auditRepo;
    private final ObjectMapper objectMapper;

    public TradeAggregator(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<JsonNode> rebuild(String tradeRef) {
        List<AuditLogEntry> events = auditRepo.findByTradeRefOrderByOccurredAtAsc(tradeRef);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        JsonNode state = null;
        for (AuditLogEntry e : events) {
            try {
                // Get operation from eventType field
                String operation = e.getEventType();
                if (operation == null) {
                    operation = e.getOperation(); // fallback
                }
                
                if (operation == null) {
                    log.warn("No operation found for audit entry: {}", e.getId());
                    continue;
                }

                // Parse event type
                TradeEvent.EventType eventType;
                try {
                    eventType = TradeEvent.EventType.valueOf(operation.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown event type: {}", operation);
                    continue;
                }

                switch (eventType) {
                    case TRADE_CREATED, TRADE_UPDATED -> {
                        String afterData = e.getAfterData();
                        if (afterData != null && !afterData.isEmpty()) {
                            state = objectMapper.readTree(afterData);
                        }
                    }
                    case TRADE_CANCELLED -> {
                        state = null;
                    }
                    default -> {
                        // Do nothing for other event types
                    }
                }
            } catch (JsonProcessingException ex) {
                log.error("Failed to parse JSON for audit entry {}: {}", e.getId(), ex.getMessage());
            } catch (Exception ex) {
                log.error("Error processing audit entry {}: {}", e.getId(), ex.getMessage());
            }
        }
        
        return Optional.ofNullable(state);
    }
}