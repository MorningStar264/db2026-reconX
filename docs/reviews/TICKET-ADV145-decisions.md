# TICKET-ADV145 — Kafka Consumer Config Review: Team Decisions

Status per finding: **Accept** (will apply), **Reject** (won't apply, with reason),
**Defer** (needs follow-up before a call can be made).

> Pre-filled with placeholder decisions based on risk level in the findings doc.
> Replace `Accept`/`Reject`/`Defer` and rationale with your team's actual call
> before merging — these are drafts, not final.

## 1. Backpressure & poll tuning

| # | Finding | Decision | Rationale |
|---|---------|----------|-----------|
| 1.1 | `max.poll.records` unset | Defer | Need a throughput baseline from staging before picking a number |
| 1.2 | `max.poll.interval.ms` unset | Defer | Depends on 1.1 — same tuning exercise |
| 1.3 | `listener.concurrency` unset | Accept | Partition count is known; setting explicit concurrency is low-risk and closes the gap now |
| 1.4 | `fetch.min.bytes` / `fetch.max.wait.ms` unset | Reject | No known latency/throughput problem to solve; leave on defaults until one appears |

## 2. Error handling, retry & DLQ

| # | Finding | Decision | Rationale |
|---|---------|----------|-----------|
| 2.1 | No non-retryable exception classification | Accept | Straightforward fix, prevents wasted retries on poison-pill messages |
| 2.2 | Missing `ErrorHandlingDeserializer` wrapper | Accept | Closes a real gap — deserialization failures currently bypass the DLQ entirely |
| 2.3 | No `maxInterval` on backoff | Accept | One-line change, cheap insurance |
| 2.4 | DLQ topic pre-provisioning unconfirmed | Defer | Needs a check against `KafkaTopicsConfig` and broker `auto.create.topics.enable` setting before deciding |

## 3. Idempotence & exactly-once semantics

| # | Finding | Decision | Rationale |
|---|---------|----------|-----------|
| 3.1 | Producer `enable.idempotence` unset | Accept | Low-risk, standard hardening for a producer used in DLQ/recovery paths |
| 3.2 | Producer `acks` unset | Accept | Required alongside 3.1 for the durability guarantee to actually hold |
| 3.3 | Consumer `isolation.level` unset | Reject | No transactional producers exist elsewhere in the pipeline today; revisit if that changes |
| 3.4 | Application-level idempotency unconfirmed | Defer | Needs code-level check in the reconciliation service before it can be marked resolved |

## 4. Observability

| # | Finding | Decision | Rationale |
|---|---------|----------|-----------|
| 4.1 | No consumer-side latency histogram confirmed | Defer | Need to check what Micrometer auto-registers for `spring.kafka.listener` before adding config |
| 4.2 | No DLQ publish metric/alert | Accept | Silent DLQ growth is an operational blind spot worth closing |
| 4.3 | `health.show-details: when-authorized` | Accept | Already correct — no change, confirming as-is |
| 4.4 | No trace propagation confirmed | Defer | Depends on whether distributed tracing is adopted platform-wide; out of scope for this ticket alone |

## 5. Security

| # | Finding | Decision | Rationale |
|---|---------|----------|-----------|
| 5.1 | No TLS/SASL on Kafka | Accept | Blocking for any non-dev environment; must be fixed before prod rollout |
| 5.2 | SASL credentials sourcing | Accept | Follows directly from 5.1; must be env/vault-sourced, not inline |
| 5.3 | Kafka ACLs unconfirmed | Defer | Infra/ops-owned, not something this PR's YAML change can resolve on its own |
| 5.4 | JWT secret hardcoded in YAML | Accept | Security-blocking, non-negotiable |

## Summary

- **Accept**: 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 4.2, 4.3, 5.1, 5.2, 5.4
- **Reject**: 1.4, 3.3
- **Defer**: 1.1, 1.2, 2.4, 3.4, 4.1, 4.4, 5.3