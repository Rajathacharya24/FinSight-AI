# FinSight AI — Architecture

## High-level diagram

```
                        ┌──────────────┐
                        │   Frontend   │  React + Tailwind (port 3000)
                        └──────┬───────┘
                               │ HTTPS
                               ▼
                        ┌──────────────┐
                        │ API Gateway  │  Spring Cloud Gateway (8080)
                        └──────┬───────┘
                               │ JWT
       ┌───────────────────────┼────────────────────────────────┐
       │                       │                                │
       ▼                       ▼                                ▼
┌────────────┐         ┌──────────────┐                ┌────────────┐
│ Auth (8081)│         │ Workflow     │  Orchestrates  │ Document   │
│            │         │ Service 8084 │ ─────────────► │ (8082)     │
└─────┬──────┘         │              │                └─────┬──────┘
      │                │ ┌──────────┐ │                      │
      ▼                │ │Doc Analy.│ │                      ▼
 ┌──────────┐          │ │ Agent    │ │                ┌──────────┐
 │ Postgres │ ◄────────┤ │Complianc.│ │                │   AI     │
 │ pgvector │          │ │ Agent    │ │ ─────────────► │ (8083)   │
 └──────────┘          │ │Decision  │ │                └──────────┘
                       │ │ Agent    │ │
                       │ └──────────┘ │
                       └──────┬───────┘
                              │
                              ▼
                       ┌──────────────┐         ┌──────────┐
                       │    Kafka     │ ──────► │  DLT     │
                       └──────┬───────┘         └──────────┘
                              │
                ┌─────────────┴───────────────────┐
                ▼                                 ▼
        DocumentUploaded                 AnalysisCompleted
        ExtractionCompleted

                       ┌──────────────────────────┐
                       │   Observability stack    │
                       │ Prometheus │ Grafana     │
                       │ Zipkin    │ Actuator     │
                       └──────────────────────────┘
```

## Workflow Service — internals

```
                ┌─────────────────────────────────────────┐
                │      WorkflowController (REST)          │
                └─────────────────┬───────────────────────┘
                                  │
                ┌─────────────────▼──────────────────────┐
                │      WorkflowOrchestrator              │
                │  ┌──────────────────────────────────┐  │
                │  │  Upload → Extract → Validate →   │  │
                │  │  Recommend (transactional state) │  │
                │  └──────────────────────────────────┘  │
                └──┬─────────────┬──────────────┬────────┘
                   │             │              │
                   ▼             ▼              ▼
         DocumentAnalyzer  ComplianceAgent  DecisionAgent
                   │             │              │
                   └─────────────┬┴──────────────┘
                                 │
                                 ▼
              ┌──────────────────────────────────────┐
              │  AuditLogService → workflow_audit    │
              │  WorkflowInstanceRepository (JPA)    │
              │  WorkflowEventProducer (Kafka)       │
              └──────────────────────────────────────┘
```

## Event flow

1. **Client** uploads PDF via `POST /api/v1/workflows`.
2. **DocumentAnalyzerAgent**
   - Calls `document-service` to persist the file.
   - Publishes `DocumentUploaded` to Kafka.
   - Calls `ai-service` for structured extraction.
   - Publishes `ExtractionCompleted`.
3. **ComplianceAgent** runs rule-based validation.
4. **DecisionAgent** produces APPROVE / REVIEW / REJECT.
5. Orchestrator publishes `AnalysisCompleted` with compliance + decision payloads.
6. Each step writes to `workflow_audit_logs` for the audit trail returned by `GET /workflows/{id}/audit-log`.

## Retry / DLQ

Kafka listeners use `@RetryableTopic` with:

- 4 attempts
- Exponential backoff: 1s → 2s → 4s → 8s (capped at 10s)
- Failures route to `<topic>.DLT`
- A dedicated DLT listener logs / could re-queue events

## State storage

```
workflow_instances           workflow_audit_logs
─────────────────────        ──────────────────────
id PK                        id PK
document_id                  workflow_id  FK → workflow_instances
user_id                      step
status                       agent
current_step                 action
extraction_data  TEXT(JSON)  status
compliance_data  TEXT(JSON)  input_summary
decision_data    TEXT(JSON)  output_summary
error_message                created_at
created_at
updated_at
```

## Observability

- **Metrics:** Each Spring Boot service exposes `/actuator/prometheus`; Prometheus scrapes every 15s. The orchestrator method `WorkflowOrchestrator.execute` is wrapped with `@Timed("workflow.execute")` so latency histograms appear in Grafana.
- **Tracing:** `micrometer-tracing-bridge-otel` → Zipkin exporter. Every HTTP / Kafka call propagates `traceId` + `spanId`, also injected into log lines via the logging pattern.
- **Logs:** Pattern: `LEVEL [service,traceId,spanId]`.
- **Dashboards:** Provisioned automatically into Grafana from `infra/grafana/dashboards/`.
