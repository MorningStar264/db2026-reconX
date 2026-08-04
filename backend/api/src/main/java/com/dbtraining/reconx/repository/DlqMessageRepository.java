package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.model.DlqMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DlqMessageRepository extends JpaRepository<DlqMessage, Long> {
    Optional<DlqMessage> findByEventId(String eventId);
    List<DlqMessage> findByProcessedFalse();
    List<DlqMessage> findByOriginalTopic(String originalTopic);
    List<DlqMessage> findByTradeRef(String tradeRef);
}