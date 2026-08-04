package com.dbtraining.reconx.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "original_topic", nullable = false)
    private String originalTopic;

    @Column(name = "trade_ref", nullable = false)
    private String tradeRef;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOriginalTopic() { return originalTopic; }
    public void setOriginalTopic(String originalTopic) { this.originalTopic = originalTopic; }

    public String getTradeRef() { return tradeRef; }
    public void setTradeRef(String tradeRef) { this.tradeRef = tradeRef; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DlqMessage message = new DlqMessage();

        public Builder eventId(String eventId) {
            message.eventId = eventId;
            return this;
        }

        public Builder originalTopic(String originalTopic) {
            message.originalTopic = originalTopic;
            return this;
        }

        public Builder tradeRef(String tradeRef) {
            message.tradeRef = tradeRef;
            return this;
        }

        public Builder payload(String payload) {
            message.payload = payload;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            message.errorMessage = errorMessage;
            return this;
        }

        public DlqMessage build() {
            return message;
        }
    }
}