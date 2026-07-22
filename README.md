# FinSight-AI

AI-powered loan application processing platform built on Spring Boot microservices.

## Architecture

```
Browser → nginx (frontend) → API Gateway (:8080)
                                    │
              ┌─────────────────────┼──────────────────────┐
              ▼                     ▼                      ▼
        auth-service          document-service         ai-service
           (:8081)               (:8082)                (:8083)
                                                           │
                                                    workflow-service
                                                        (:8084)
Infrastructure:
  PostgreSQL + pgvector │ Redis │ Kafka │ Eureka │ Config Server │ Zipkin
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| `config-server` | 8888 | Centralised config (Eureka-registered) |
| `discovery-server` | 8761 | Eureka service registry |
| `api-gateway` | 8080 | Spring Cloud Gateway — routing, CORS, rate-limit, error handling |
| `auth-service` | 8081 | JWT auth, user registration, Redis-backed rate limiting |
| `document-service` | 8082 | PDF upload, text extraction, file validation, ClamAV interface |
| `ai-service` | 8083 | Spring AI + pgvector RAG pipeline, structured data extraction |
| `workflow-service` | 8084 | Kafka-driven orchestration, Resilience4j circuit breakers, DLQ |
| `frontend` | 3000 | React / Vite SPA (proxied via nginx in Docker) |
| `kafka-ui` | 8090 | Kafka topic browser & message viewer |
| `prometheus` | 9090 | Metrics collection (scrapes all /actuator/prometheus) |
| `grafana` | 3001 | Observability dashboards (auto-provisioned) |
| `zipkin` | 9411 | Distributed tracing UI |

## Quick Start

### Prerequisites

- Docker ≥ 24 and Docker Compose v2
- A Groq or OpenAI API key

### 1. Clone & configure secrets

```bash
git clone https://github.com/your-org/FinSight-AI.git
cd FinSight-AI
cp .env.example .env          # fill in your secrets
```

`.env` must contain **at minimum**:

```env
JWT_SECRET=<random-256-bit-hex>
GROQ_API_KEY=<your-key>        # or OPENAI_API_KEY
DB_PASSWORD=finsight_password
```

### 2. Start everything

```bash
docker compose up --build -d
```

Services start in dependency order. Config-server and Eureka come up first; microservices wait for them.

### 3. Access

| URL | What |
|---|---|
| `http://localhost:3000` | Frontend SPA |
| `http://localhost:8080` | API Gateway |
| `http://localhost:8761` | Eureka Dashboard |
| `http://localhost:8090` | Kafka UI (topic browser) |
| `http://localhost:9090` | Prometheus |
| `http://localhost:3001` | Grafana (admin / admin) |
| `http://localhost:9411` | Zipkin Distributed Tracing |
| `http://localhost:8081/swagger-ui.html` | Auth API docs |
| `http://localhost:8083/swagger-ui.html` | AI extraction API docs |

### 4. Verify health

```bash
# All services up
curl http://localhost:8080/actuator/health | jq '.status'

# Circuit breaker state
curl http://localhost:8084/actuator/health | jq '.components.circuitBreakers'

# Eureka registry
curl http://localhost:8761/eureka/apps | grep '<app>'
```

## API Reference

### Auth

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"secret123"}'

# Login → returns { "token": "...", "refreshToken": "..." }
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"secret123"}'

# Refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

### Upload & Process a Document

```bash
TOKEN="Bearer <your-jwt>"

# Upload PDF — triggers full workflow
curl -X POST http://localhost:8080/api/v1/workflows \
  -H "Authorization: $TOKEN" \
  -F 'metadata={"title":"Q3 Application","description":""};type=application/json' \
  -F 'file=@/path/to/loan.pdf;type=application/pdf'

# Poll workflow status
curl http://localhost:8080/api/v1/workflows/<id> -H "Authorization: $TOKEN"

# Get extracted structured data
curl http://localhost:8080/api/v1/workflows/<id>/extraction -H "Authorization: $TOKEN"

# Get document content (extracted text)
curl http://localhost:8080/api/v1/documents/<documentId>/content -H "Authorization: $TOKEN"

# Workflow audit trail
curl http://localhost:8080/api/v1/workflows/<id>/audit-log -H "Authorization: $TOKEN"
```

### Analytics

```bash
# Summary stats (total, completed, failed, avg duration)
curl http://localhost:8080/api/v1/analytics/summary -H "Authorization: $TOKEN"

# Per-workflow time-series data
curl http://localhost:8080/api/v1/analytics/timeseries -H "Authorization: $TOKEN"
```

## Security Notes

- **`JWT_SECRET`** is required at startup — the application will refuse to start if it is missing.
- **`GROQ_API_KEY`** (or **`OPENAI_API_KEY`**) is required for AI extraction; defaults to Groq's `llama3-8b-8192`.
- Login/register endpoints are rate-limited to **5 requests/min per IP** via Redis.
- File uploads are validated for type (PDF only) and size (≤ 10 MB) before storage.
- All inter-service calls are guarded by Resilience4j circuit breakers (50% failure threshold, 30 s open state).
- Expired/invalid JWT tokens auto-redirect the frontend to the login page (401 interceptor).
- Refresh tokens are supported — the `/api/v1/auth/refresh` endpoint issues new JWT pairs.

## Development

### Build & test a single service

```bash
cd workflow-service
mvn clean test
```

### Run locally (outside Docker)

Requires Postgres, Redis, Kafka, and Eureka running (see `docker-compose.yml` for ports).

```bash
export JWT_SECRET=$(openssl rand -hex 32)
export GROQ_API_KEY=<key>
cd auth-service && mvn spring-boot:run
```

### Frontend dev server

```bash
cd frontend
cp .env.example .env      # VITE_API_BASE_URL defaults to empty (uses Vite proxy → localhost:8080)
npm install
npm run dev               # http://localhost:3000
```

> **Note:** The frontend `.env.example` does not exist in the repo yet. Create it manually:
> ```env
> VITE_API_BASE_URL=http://localhost:8080
> ```

### Frontend pages

| Route | Page | Description |
|---|---|---|
| `/upload` | Upload | Upload a PDF document and kick off the workflow pipeline |
| `/documents` | Workflows | List all workflows with status, step, and creation time |
| `/documents/:id` | Workflow Detail | View workflow summary, extracted data, and full audit log |
| `/analytics` | Analytics | Charts for processing time, accuracy, and cost per workflow |
| `/login` | Login | JWT-based authentication (pre-filled dev credentials) |

### Generate a sample PDF for testing

```bash
# Compile and run the sample PDF generator
javac infra/scripts/GenSamplePdf.java -d /tmp/classes
java -cp /tmp/classes GenSamplePdf
# Creates loan-application-sample.pdf in the current directory
```

The generated PDF contains realistic loan application data suitable for end-to-end workflow testing.

## Production Hardening Applied

| Area | What |
|---|---|
| Secrets | `JWT_SECRET` enforced via env — no hardcoded fallbacks |
| Auth | BCrypt cost 12, Redis rate limiter (5 req/min/IP) |
| API Gateway | Centralized JSON error handler, CORS, DedupeResponseHeader |
| File Upload | Type (PDF), size (10 MB), malware scan gate |
| RAG Pipeline | Real pgvector store — chunking → embed → retrieve → generate |
| Kafka | Retryable topics (4 attempts, exponential back-off), DLQ marks workflow FAILED |
| Circuit Breakers | Resilience4j on all inter-service calls, health indicators exposed |
| Observability | Micrometer + Prometheus metrics, distributed tracing via Zipkin |
