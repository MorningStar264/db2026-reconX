package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.model.DlqMessage;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dlq")
public class DlqAdminController {

    private final DlqMessageRepository repo;
    private final ObjectMapper objectMapper;

    public DlqAdminController(DlqMessageRepository repo) {
        this.repo = repo;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public List<DlqMessage> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<DlqMessage> getByEventId(@PathVariable String eventId) {
        return repo.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{eventId}/retry")
    public ResponseEntity<Void> retry(@PathVariable String eventId) {
        return repo.findByEventId(eventId)
                .map(msg -> {
                    // Convert payload String to TradeEvent if needed
                    try {
                        TradeEvent event = objectMapper.readValue(msg.getPayload(), TradeEvent.class);
                        // Logic to retry the event
                        // kafkaTemplate.send(msg.getOriginalTopic(), event);
                        msg.setProcessed(true);
                        repo.save(msg);
                    } catch (Exception e) {
                        // Handle error
                    }
                    return ResponseEntity.accepted().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> delete(@PathVariable String eventId) {
        repo.findByEventId(eventId).ifPresent(repo::delete);
        return ResponseEntity.noContent().build();
    }
}