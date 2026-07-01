# FinSight AI — Deployment Guide

## Prerequisites

- Docker 24+ and Docker Compose v2
- (Optional dev) Java 21, Maven 3.9, Node 20

## 1. Configure environment

```bash
cp .env.example .env
# edit secrets: GROQ_API_KEY, JWT_SECRET, POSTGRES_PASSWORD
```

## 2. Build & run (Compose)

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f workflow-service
```

The compose file boots services in dependency order: postgres → kafka → config-server → discovery-server → core services → frontend.

## 3. Verify

```bash
curl http://localhost:8084/actuator/health
curl http://localhost:8080/api/v1/workflows         # list (empty array first time)
open http://localhost:3000                          # frontend
open http://localhost:3001                          # Grafana
```

## 4. Run a workflow end-to-end

```bash
curl -X POST http://localhost:8080/api/v1/workflows \
  -F 'metadata={"title":"Demo","description":"Smoke test"};type=application/json' \
  -F 'file=@./samples/loan-app.pdf'
```

You should see:

- A `WorkflowResponse` JSON body with `status: COMPLETED`.
- An event in Kafka UI under `finsight.analysis.completed`.
- A new entry in Grafana → "FinSight Workflow Service".
- Traces in Zipkin spanning gateway → workflow → document → ai service.

## 5. Local development (without Docker)

```bash
# infra only
docker compose up -d postgres redis kafka zookeeper zipkin prometheus grafana

# backend
mvn -B clean install -DskipTests
(cd workflow-service && mvn spring-boot:run)

# frontend
cd frontend && npm install && npm run dev
```

The frontend dev server proxies `/api/*` to `VITE_API_BASE_URL` (default `http://localhost:8080`).

## 6. Tests

```bash
mvn -B verify                     # all backend
(cd frontend && npm test)         # frontend
```

The integration test in `workflow-service` uses H2 + mocked downstream clients — no Kafka or Postgres needed locally.

## 7. CI/CD (GitHub Actions)

- `.github/workflows/backend.yml` — runs on every push, builds + tests, then docker-builds each backend module on `main`.
- `.github/workflows/frontend.yml` — builds and tests the React app.

To push images, add a deploy job that authenticates to your registry (`docker/login-action@v3`) and runs `docker push`.

## 8. Production checklist

- [ ] Externalize secrets (Vault, AWS Secrets Manager, K8s Secrets).
- [ ] Replace `H2` in test profile with a managed Postgres.
- [ ] Set `management.tracing.sampling.probability` to e.g. `0.1`.
- [ ] Replace single-broker Kafka with a 3-broker cluster.
- [ ] Add ingress + TLS in front of the API gateway and frontend.
- [ ] Lock down `actuator` endpoints (`include: health,info,prometheus`).
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (already done).
- [ ] Configure log aggregation (Loki / ELK).

## 9. Troubleshooting

| Symptom                                                   | Likely cause                                          |
|-----------------------------------------------------------|-------------------------------------------------------|
| `workflow-service` exits with Flyway error                | Migration mismatch — check `flyway_schema_history`.   |
| `503` from gateway calling `workflow-service`             | Eureka registration delay — wait ~30s after start.    |
| Kafka listener prints `OFFSET_OUT_OF_RANGE`               | Reset consumer group: `kafka-consumer-groups --reset` |
| Grafana panels show "No data"                             | Confirm Prometheus targets `up == 1` on `:9090`.      |
| Zipkin empty                                              | `ZIPKIN_URL` not set or `management.tracing.enabled=false`. |
