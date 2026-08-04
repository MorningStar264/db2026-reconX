# TICKET-ADV145 — Kafka Consumer Config Review

## Prompt sent to Claude

```
Review the following Spring Kafka consumer configuration for production
readiness. Flag any missing or risky settings in these areas:
  (1) backpressure & poll tuning,
  (2) error handling, retry & DLQ,
  (3) idempotence and exactly-once semantics,
  (4) observability — metrics, logging, traces,
  (5) security — TLS, SASL, ACLs.
```

## Constraints given

- No whole-file rewrites — flag specific keys only.
- Each finding: concrete config key + recommended value + one-line justification.

## Files reviewed

- `application.yml` (`spring.kafka` section, `management.*`, `reconx.security.jwt.*`)
- `src/main/java/com/dbtraining/reconx/kafka/KafkaErrorHandlerConfig.java`

## How to reproduce this review

1. Open `application.yml` (Kafka section) and `KafkaErrorHandlerConfig.java`.
2. Paste both into a new Claude conversation.
3. Send the prompt above verbatim.
4. Collect findings into `TICKET-ADV145-findings.md`.
5. Record team calls in `TICKET-ADV145-decisions.md`.
6. Apply accepted findings, commit, open PR linking all three docs.