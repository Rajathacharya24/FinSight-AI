# FinSight AI

AI-powered financial document intelligence platform: upload a document, agents extract structured data, validate compliance, and produce a recommendation.

```
Upload  →  Extract  →  Validate  →  Recommend
 (Document        (Document          (Compliance     (Decision
  Analyzer)        Analyzer)          Agent)          Agent)
```

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Cloud (Config, Eureka, Gateway)
- **Messaging:** Apache Kafka (producer / consumer / retry / DLQ)
- **AI:** Spring AI (RAG via pgvector)
- **Persistence:** PostgreSQL + Flyway
- **Frontend:** React + Vite + Tailwind + Recharts
- **Observability:** Prometheus, Grafana, Zipkin, Spring Actuator, Micrometer Tracing
- **Packaging:** Docker Compose, GitHub Actions

## Services

| Service             | Port  | Purpose                                          |
|---------------------|-------|--------------------------------------------------|
| config-server       | 8888  | Centralised configuration                        |
| discovery-server    | 8761  | Eureka service registry                          |
| api-gateway         | 8080  | Public entry point                               |
| auth-service        | 8081  | JWT auth, users                                  |
| document-service    | 8082  | PDF upload + text extraction                     |
| ai-service          | 8083  | LLM extraction, RAG                              |
| workflow-service    | 8084  | Orchestrates the 3 agents, publishes Kafka events|
| frontend            | 3000  | React console                                    |
| postgres            | 5432  | Relational + pgvector                            |
| redis               | 6379  | Cache, rate limiting                             |
| kafka               | 9092  | Event bus                                        |
| kafka-ui            | 8090  | Browser UI for topics                            |
| prometheus          | 9090  | Metrics                                          |
| grafana             | 3001  | Dashboards                                       |
| zipkin              | 9411  | Distributed tracing                              |

## Quick start

```bash
cp .env.example .env
docker compose up -d --build
```

Then open:

- Frontend: <http://localhost:3000>
- Swagger (workflow): <http://localhost:8084/swagger-ui.html>
- Grafana: <http://localhost:3001> (admin / admin)
- Prometheus: <http://localhost:9090>
- Zipkin: <http://localhost:9411>
- Kafka UI: <http://localhost:8090>
- Eureka: <http://localhost:8761>

## Workflow Service API

### Start a workflow

```bash
curl -X POST http://localhost:8080/api/v1/workflows \
  -F 'metadata={"title":"Loan App","description":"Q3"};type=application/json' \
  -F 'file=@sample.pdf'
```

### Get a workflow + audit log

```bash
curl http://localhost:8080/api/v1/workflows/1
curl http://localhost:8080/api/v1/workflows/1/audit-log
```

### Analytics

```bash
curl http://localhost:8080/api/v1/analytics/summary
curl http://localhost:8080/api/v1/analytics/timeseries
```

## Kafka events

| Topic                              | Producer        | When                          |
|------------------------------------|-----------------|-------------------------------|
| `finsight.document.uploaded`       | workflow-service| After upload step             |
| `finsight.extraction.completed`    | workflow-service| After extraction step         |
| `finsight.analysis.completed`      | workflow-service| After full workflow completes |

Each topic auto-creates a `*.DLT` dead-letter topic. Listeners retry 4× with exponential backoff (1s → 10s) before routing to DLT.

## Tests

```bash
mvn -B verify                       # all backend modules
cd frontend && npm install && npm test
```

## Phases

| Phase | Scope                                                        |
|-------|--------------------------------------------------------------|
| 1     | Infrastructure (Postgres, Redis, Eureka, Config, Gateway)    |
| 2     | Auth service (JWT)                                           |
| 3     | Document upload + extraction                                 |
| 4     | AI extraction agents                                         |
| 5     | RAG (pgvector + embeddings)                                  |
| 6     | Kafka events + workflow orchestration                        |
| 7     | React dashboard                                              |
| 8     | Production: Docker Compose, CI, monitoring, deployment guide |

## More docs

- [ARCHITECTURE.md](./ARCHITECTURE.md) — diagrams + flow
- [DEPLOYMENT.md](./DEPLOYMENT.md) — running locally & in prod
- [.env.example](./.env.example) — env var reference

## License

MIT
