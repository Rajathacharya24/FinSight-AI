<p align="center">
  <strong>⚡ FinSight AI</strong><br/>
  <em>AI-powered loan application processing platform built on Spring Boot microservices application </em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=white" alt="React" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/Kafka-Streaming-231F20?style=flat&logo=apachekafka&logoColor=white" alt="Kafka" />
  <img src="https://img.shields.io/badge/PostgreSQL+pgvector-RAG-4169E1?style=flat&logo=postgresql&logoColor=white" alt="PostgreSQL" />
</p>

---

## What is FinSight AI?

FinSight AI automates the end-to-end processing of loan application PDFs using an **AI-powered microservices pipeline**:

1. **Upload** a PDF loan application
2. **Extract text** from the document (Apache PDFBox)
3. **AI extraction** of structured data (applicant name, income, loan amount, etc.) using Spring AI + Groq/OpenAI
4. **Compliance validation** — automated rule checks
5. **Decision recommendation** — approve/reject/manual-review with confidence scores
6. **Notifications** — email (via Mailpit/SMTP) and in-app alerts

All orchestrated through **Kafka event-driven workflows** with full **audit trails**, **circuit breakers**, and **distributed tracing**.

---

## Architecture

```
Browser → nginx (frontend:3000) → API Gateway (:8080)
                                       │
               ┌───────────────────────┼────────────────────────┐
               ▼                       ▼                        ▼
         auth-service           document-service           ai-service
            (:8081)                 (:8082)                  (:8083)
                                                               │
                                                        workflow-service
                                                            (:8084)
                                                               │
                                                     notification-service
                                                            (:8085)

Infrastructure:
  PostgreSQL + pgvector │ Redis │ Kafka │ MinIO │ Eureka │ Config Server │ Zipkin
```

---

## 🚀 How to Start (Step by Step)

### Prerequisites

| Tool | Version | Why |
|------|---------|-----|
| **Docker** | ≥ 24 | Runs all services as containers |
| **Docker Compose** | v2 (built into Docker Desktop) | Orchestrates 15+ containers |
| **Groq API Key** | Free at [console.groq.com](https://console.groq.com) | Powers AI data extraction |

> **That's it!** No Java, Maven, or Node.js installation needed — everything builds inside Docker.

### Step 1 — Clone the repository

```bash
git clone https://github.com/Rajathacharya24/FinSight-AI.git
cd FinSight-AI
```

### Step 2 — Create your `.env` file

```bash
cp .env.example .env
```

Now open `.env` and set **two required values**:

```env
# Generate a random JWT secret (copy-paste this command)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')

# Paste your Groq API key (get one free at https://console.groq.com)
GROQ_API_KEY=gsk_your_actual_key_here
```

> **Tip:** Everything else in `.env` has safe defaults for local development. You only need `JWT_SECRET` and `GROQ_API_KEY`.

### Step 3 — Start all services

```bash
docker compose up --build -d
```

This will:
- Build all 8 Java microservices + the React frontend
- Start PostgreSQL, Redis, Kafka, MinIO, Zipkin, Prometheus, Grafana, Mailpit
- Wire everything together with health checks and dependency ordering

> ⏱ **First build takes 5-10 minutes** (Maven downloads dependencies). Subsequent starts are much faster.

### Step 4 — Wait for services to be ready

```bash
# Watch all containers until healthy
docker compose ps

# Or check the API Gateway health endpoint
curl http://localhost:8080/actuator/health
```

All services should show `healthy` status. The config-server and discovery-server start first; others wait for them.

### Step 5 — Open the app

| URL | What |
|-----|------|
| **http://localhost:3000** | 🖥 **Frontend** — main application |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka Service Registry |
| http://localhost:8090 | Kafka UI (topic browser) |
| http://localhost:9090 | Prometheus Metrics |
| http://localhost:3001 | Grafana Dashboards (admin / admin) |
| http://localhost:9411 | Zipkin Distributed Tracing |
| http://localhost:8025 | Mailpit (email viewer) |
| http://localhost:9001 | MinIO Console (minioadmin / minioadmin) |

### Step 6 — Login

Use the pre-seeded demo credentials:

| Field | Value |
|-------|-------|
| Username | `demo` |
| Password | `demo123` |

### Step 7 — Try it end-to-end

1. Go to **Upload** → upload any PDF (a loan application PDF works best)
2. The system will process it through the full pipeline: upload → extract → validate → recommend
3. Go to **Workflows** → see your workflow and click it for detailed results
4. Check **Analytics** → view processing time, accuracy, and cost charts
5. Check **Notifications** → see in-app alerts for completed workflows
6. Check **Mailpit** at http://localhost:8025 → see email notifications

---

## Services Overview

| Service | Port | Technology | Responsibility |
|---------|------|------------|----------------|
| `config-server` | 8888 | Spring Cloud Config | Centralized configuration |
| `discovery-server` | 8761 | Eureka | Service registry |
| `api-gateway` | 8080 | Spring Cloud Gateway | Routing, CORS, JWT validation, rate limiting |
| `auth-service` | 8081 | Spring Security + JWT | User registration, login, refresh tokens, Redis rate limiting |
| `document-service` | 8082 | Spring Boot + PDFBox | PDF upload, text extraction, MinIO storage, virus scan gate |
| `ai-service` | 8083 | Spring AI + pgvector | RAG pipeline: chunk → embed → retrieve → structured extraction |
| `workflow-service` | 8084 | Spring Boot + Kafka | Orchestrates the full pipeline, Resilience4j circuit breakers, DLQ |
| `notification-service` | 8085 | Spring Boot + Kafka | Email (Mailpit/SMTP), webhooks, in-app notification history |
| `frontend` | 3000 | React + Vite + Tailwind | SPA with dark glassmorphism UI |

---

## API Quick Reference

### Authentication

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"newuser","email":"user@example.com","password":"secret123"}'

# Login (returns JWT + refresh token)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo123"}'

# Save the token for subsequent requests
TOKEN="Bearer <paste-token-from-login-response>"

# Refresh an expired token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

### Upload & Process a Document

```bash
# Upload a PDF and start the workflow pipeline
curl -X POST http://localhost:8080/api/v1/workflows \
  -H "Authorization: $TOKEN" \
  -F 'metadata={"title":"Q3 Application","description":""};type=application/json' \
  -F 'file=@/path/to/loan.pdf;type=application/pdf'

# Check workflow status
curl http://localhost:8080/api/v1/workflows/<id> -H "Authorization: $TOKEN"

# View extracted structured data
curl http://localhost:8080/api/v1/workflows/<id>/extraction -H "Authorization: $TOKEN"

# View audit trail
curl http://localhost:8080/api/v1/workflows/<id>/audit-log -H "Authorization: $TOKEN"
```

### Analytics

```bash
# Summary stats (total, completed, failed, avg duration)
curl http://localhost:8080/api/v1/analytics/summary -H "Authorization: $TOKEN"

# Per-workflow time-series data
curl http://localhost:8080/api/v1/analytics/timeseries -H "Authorization: $TOKEN"
```

---

## Stopping & Cleaning Up

```bash
# Stop all services (preserves data)
docker compose down

# Stop and delete all data volumes (fresh start)
docker compose down -v --remove-orphans
```

---

## Development Guide

### Build & test a single service (requires Java 21 + Maven locally)

```bash
cd auth-service
mvn clean test
```

### Frontend dev server (requires Node.js 20+)

```bash
cd frontend
npm install
npm run dev       # → http://localhost:3000 (proxies API to :8080)
```

### Rebuild a single service in Docker

```bash
docker compose up --build -d auth-service
```

### View logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f workflow-service
```

### Generate a sample PDF for testing

```bash
javac infra/scripts/GenSamplePdf.java -d /tmp/classes
java -cp /tmp/classes GenSamplePdf
# Creates loan-application-sample.pdf
```

---

## Tech Stack & What to Learn

This project covers a **wide range of technologies**. Here's a learning guide:

### Backend (Java / Spring)

| Technology | What It Does | Learn More |
|------------|-------------|------------|
| **Spring Boot 3.5** | Framework for each microservice | [spring.io/guides](https://spring.io/guides) |
| **Spring Cloud Gateway** | API routing, CORS, rate limiting | [docs](https://docs.spring.io/spring-cloud-gateway/reference/) |
| **Spring Cloud Config** | Centralized config management | [docs](https://docs.spring.io/spring-cloud-config/reference/) |
| **Spring Cloud Eureka** | Service discovery and registration | [docs](https://docs.spring.io/spring-cloud-netflix/reference/) |
| **Spring Security + JWT** | Authentication, authorization, role-based access | [docs](https://docs.spring.io/spring-security/reference/) |
| **Spring AI** | LLM integration (Groq/OpenAI) for data extraction | [docs](https://docs.spring.io/spring-ai/reference/) |
| **Spring Kafka** | Event-driven messaging between services | [docs](https://docs.spring.io/spring-kafka/reference/) |
| **Resilience4j** | Circuit breakers, retry, time limiter | [resilience4j.readme.io](https://resilience4j.readme.io/) |
| **Flyway** | Database schema migrations | [flywaydb.org](https://flywaydb.org/) |
| **Micrometer + Prometheus** | Metrics collection and monitoring | [micrometer.io](https://micrometer.io/) |
| **Zipkin** | Distributed tracing across services | [zipkin.io](https://zipkin.io/) |
| **Lombok** | Reduces Java boilerplate | [projectlombok.org](https://projectlombok.org/) |

### Frontend

| Technology | What It Does | Learn More |
|------------|-------------|------------|
| **React 18** | UI component framework | [react.dev](https://react.dev/) |
| **Vite** | Fast build tool and dev server | [vitejs.dev](https://vitejs.dev/) |
| **React Router 6** | Client-side routing | [reactrouter.com](https://reactrouter.com/) |
| **Tailwind CSS** | Utility-first CSS framework | [tailwindcss.com](https://tailwindcss.com/) |
| **Recharts** | React charting library | [recharts.org](https://recharts.org/) |
| **Axios** | HTTP client with interceptors | [axios-http.com](https://axios-http.com/) |

### Infrastructure

| Technology | What It Does | Learn More |
|------------|-------------|------------|
| **Docker Compose** | Multi-container orchestration | [docs.docker.com/compose](https://docs.docker.com/compose/) |
| **PostgreSQL + pgvector** | Relational DB with vector embeddings for RAG | [pgvector](https://github.com/pgvector/pgvector) |
| **Apache Kafka** | Event streaming platform | [kafka.apache.org](https://kafka.apache.org/) |
| **Redis** | In-memory cache (rate limiting) | [redis.io](https://redis.io/) |
| **MinIO** | S3-compatible object storage | [min.io](https://min.io/) |
| **Grafana** | Monitoring dashboards | [grafana.com](https://grafana.com/) |
| **Nginx** | Reverse proxy for frontend | [nginx.org](https://nginx.org/) |

---

## Project Structure

```
FinSight-AI/
├── api-gateway/           # Spring Cloud Gateway (routing, JWT validation)
├── auth-service/          # JWT auth, user management, Redis rate limiting
├── document-service/      # PDF upload, text extraction, MinIO storage
├── ai-service/            # Spring AI + pgvector RAG pipeline
├── workflow-service/      # Kafka-driven orchestration, circuit breakers
├── notification-service/  # Email + webhook + in-app notifications
├── config-server/         # Spring Cloud Config (centralized config)
├── discovery-server/      # Eureka service registry
├── frontend/              # React + Vite + Tailwind SPA
├── infra/                 # Prometheus, Grafana, Postgres init scripts
├── docker-compose.yml     # Full stack orchestration
├── .env.example           # Environment template
├── Makefile               # Developer shortcuts (make up, make down, etc.)
└── pom.xml                # Maven parent POM
```

---

## Security Notes

- **`JWT_SECRET`** is **required** at startup — services will fail without it
- **`GROQ_API_KEY`** is required for AI extraction (free at [console.groq.com](https://console.groq.com))
- Login/register endpoints are **rate-limited** (5 req/min/IP via Redis)
- File uploads are validated: **PDF only, ≤ 10 MB**
- All inter-service calls are guarded by **Resilience4j circuit breakers**
- JWT tokens auto-refresh; expired tokens redirect to login

---

## License

MIT
