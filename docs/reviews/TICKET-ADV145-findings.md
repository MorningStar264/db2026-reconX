# TICKET-ADV145 — Kafka Consumer Config Review: Findings

Reviewed against: `application.yml` (`spring.kafka`), `KafkaErrorHandlerConfig.java`.
No whole-file rewrites — findings are scoped to individual keys/lines.

## 1. Backpressure & poll tuning

| # | Config key | Current | Recommended | Justification |
|---|-----------|---------|--------------|----------------|
| 1.1 | `spring.kafka.consumer.properties.max.poll.records` | not set (default 500) | `250` (tune to actual per-record processing cost) | Default of 500 can push `max.poll.interval.ms` past its timeout under slow processing, triggering unwanted rebalances |
| 1.2 | `spring.kafka.consumer.properties.max.poll.interval.ms` | not set (default 300000) | explicit value matched to worst-case batch processing time | Undocumented reliance on the default hides the actual SLA the consumer needs to meet before a rebalance is forced |
| 1.3 | `spring.kafka.listener.concurrency` | not set (no listener container factory shown) | set per-topic based on partition count | No visible concurrency config means all partitions for a topic may be consumed by a single thread — a throughput ceiling with no explicit intent behind it |
| 1.4 | `spring.kafka.consumer.properties.fetch.min.bytes` / `fetch.max.wait.ms` | not set | leave defaults unless a specific latency/throughput tradeoff is known | Flagging as absent, not necessarily wrong — call out explicitly so it's a deliberate choice, not an oversight |

## 2. Error handling, retry & DLQ

| # | Config key | Current | Recommended | Justification |
|---|-----------|---------|--------------|----------------|
| 2.1 | `KafkaErrorHandlerConfig.errorHandler` — exception classification | `DefaultErrorHandler` treats all exceptions as retryable | `errorHandler.addNotRetryableExceptions(DeserializationException.class, ...)` | Retrying a poison-pill deserialization failure 3 times before DLQ wastes the backoff window; non-retryable exceptions should go straight to DLQ |
| 2.2 | `spring.kafka.consumer.value-deserializer` | raw `JsonDeserializer` | wrap with `ErrorHandlingDeserializer`, delegate to `JsonDeserializer` | Without it, a deserialization failure throws inside the poll loop before `DefaultErrorHandler` ever sees the record, bypassing the DLQ path entirely |
| 2.3 | `ExponentialBackOff` in `KafkaErrorHandlerConfig` | `setMaxAttempts(3)`, no max interval | `backoff.setMaxInterval(10_000L)` | Caps worst-case retry delay explicitly; cheap insurance if attempts are ever increased later |
| 2.4 | DLQ topic creation | DLQ topic name is derived (`{topic}-dlq`) at runtime by the recoverer | confirm `KafkaTopicsConfig` also provisions `-dlq` topics at startup | If DLQ topics aren't pre-created and broker `auto.create.topics.enable=false` in prod, the recoverer publish will fail |

## 3. Idempotence & exactly-once semantics

| # | Config key | Current | Recommended | Justification |
|---|-----------|---------|--------------|----------------|
| 3.1 | `spring.kafka.producer.properties.enable.idempotence` | not set (default `false` pre-3.0 client behavior depends on version) | `true` | Prevents duplicate DLQ/recovery publishes on producer retry after a transient broker error |
| 3.2 | `spring.kafka.producer.properties.acks` | not set | `all` | Required alongside idempotence for durability guarantees; without it a leader failure can silently drop an acknowledged DLQ write |
| 3.3 | `spring.kafka.consumer.properties.isolation.level` | not set | `read_committed` | Only matters once/if producers elsewhere in the pipeline use transactions — otherwise this is a no-op; flagged for completeness given `TradeEvent` is financial data |
| 3.4 | Application-level idempotency | no dedupe logic visible in reviewed files | confirm a dedupe check exists on trade event id in the consumer/service layer | `auto-offset-reset: earliest` means a full topic replay on offset loss will reprocess every trade event; only safe if downstream processing is idempotent |

## 4. Observability — metrics, logging, traces

| # | Config key | Current | Recommended | Justification |
|---|-----------|---------|--------------|----------------|
| 4.1 | `management.metrics.distribution.percentiles-histogram.reconciliation.duration` | `true` (good — already covers reconciliation) | add equivalent histogram for `kafka.consumer` / `spring.kafka.listener` timer if not auto-registered | Confirms consumer-side latency (not just reconciliation-side) is visible in Prometheus for SLO tracking |
| 4.2 | DLQ publish visibility | `DeadLetterPublishingRecoverer` has no explicit logging/metric hook | add a listener or counter metric incremented on each DLQ publish | Silent DLQ routing means a growing DLQ can go unnoticed without a dashboard/alert tied to it |
| 4.3 | `management.endpoint.health.show-details` | `when-authorized` (good) | no change — confirmed correct | Avoids leaking internal health detail to unauthenticated callers; called out as a pass, not a gap |
| 4.4 | Trace propagation | no tracing config (`management.tracing.*`) visible in reviewed section | confirm trace/span propagation across Kafka headers if distributed tracing is used elsewhere in the platform | Without it, a trade event's path from producer → consumer → reconciliation can't be correlated in traces |

## 5. Security — TLS, SASL, ACLs

| # | Config key | Current | Recommended | Justification |
|---|-----------|---------|--------------|----------------|
| 5.1 | `spring.kafka.bootstrap-servers` | plaintext (`localhost:9092`, env-overridable, no protocol config) | add `spring.kafka.properties.security.protocol: SASL_SSL` (or `SSL`) for non-dev profiles | No visible TLS/SASL config means broker traffic — including trade event payloads — is unencrypted and unauthenticated by default |
| 5.2 | `spring.kafka.properties.sasl.mechanism` / `sasl.jaas.config` | not set | set per environment, sourced from secret store, not inline in YAML | Required once `SASL_SSL` is enabled; must not be hardcoded in `application.yml` |
| 5.3 | Kafka ACLs | out of scope for `application.yml` — flagged for infra/ops | confirm topic-level ACLs restrict `reconx-service` group to only the topics it needs (consume) and DLQ topics (produce) | Principle of least privilege — a compromised consumer shouldn't be able to produce to arbitrary topics |
| 5.4 | `reconx.security.jwt.secret` | plaintext static secret committed in YAML | `${JWT_SECRET}` from env/vault | Separate from Kafka but in the same file under review — static secret in VCS allows token forgery by anyone with repo read access |

## Summary

- **Good/no-action items** confirmed during review: DLQ recoverer pattern (2.x), `spring.json.trusted.packages` scoping, `health.show-details: when-authorized`, `reconciliation.duration` histogram.
- **Highest-risk gaps**: no TLS/SASL (5.1–5.2), no `ErrorHandlingDeserializer` (2.2 — DLQ can be silently bypassed), plaintext JWT secret (5.4).