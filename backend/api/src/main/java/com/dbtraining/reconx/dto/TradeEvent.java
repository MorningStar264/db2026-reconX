package com.dbtraining.reconx.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * ============================================================================
 * TradeEvent payload (Kafka envelope)
 *
 * WHAT:    Wire format for trade-events Kafka topic. eventId is the
 *          idempotency key; consumers deduplicate by it.
 * HOW:     Record — Jackson serialises automatically (component model
 *          = default). before/after are JSON strings (not objects) to keep
 *          the contract resilient to entity refactors.
 * WHY:     Including before+after on every event makes downstream consumers
 *          (audit, recon) self-contained — they don't have to fetch the
 *          current state from the DB.
 * ============================================================================
 */
public record TradeEvent(
        UUID eventId,
        String tradeRef,
        EventType eventType,
        Instant timestamp,
        String actor,
        String before,
        String after
) {
    public enum EventType {
        TRADE_CREATED, TRADE_UPDATED, TRADE_CANCELLED
    }

    // Constructor with default values
    public TradeEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // Convenience constructor for creation events
    public TradeEvent(String tradeRef, EventType eventType, String actor) {
        this(UUID.randomUUID(), tradeRef, eventType, Instant.now(), actor, null, null);
    }

    // Convenience constructor with before/after state
    public TradeEvent(String tradeRef, EventType eventType, String actor, String before, String after) {
        this(UUID.randomUUID(), tradeRef, eventType, Instant.now(), actor, before, after);
    }

    // Helper method to get eventId as String (for consumers)
    public String getEventIdAsString() {
        return eventId != null ? eventId.toString() : null;
    }

    // Helper method to get before state safely
    public String getBeforeState() {
        return before;
    }

    // Helper method to get after state safely
    public String getAfterState() {
        return after;
    }
}