# FinSight AI — Run Instructions

## TL;DR

```bash
cp .env.example .env
docker compose up -d --build
open http://localhost:3000
```

## Step-by-step

### 1. Bring up infrastructure first

```bash
docker compose up -d postgres redis kafka zookeeper zipkin prometheus grafana kafka-ui
```

Wait until `docker compose ps` shows them healthy.

| Service | Port | Purpose |
|---|---|---|
| PostgreSQL (pgvector) | 5432 | Relational + vector database |
| Redis | 6379 | Caching, rate limiting |
| Kafka | 9092 | Async event bus |
| ZooKeeper | 2181 | Kafka coordination |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3001 | Observability dashboards |
| Kafka UI | 8090 | Topic browser & message viewer |
| Zipkin | 9411 | Distributed tracing |

### 2. Bring up Spring Cloud infra

```bash
docker compose up -d config-server discovery-server api-gateway
```

Check Eureka at <http://localhost:8761> — wait for the dashboard to load.

### 3. Bring up the core services

```bash
docker compose up -d auth-service document-service ai-service workflow-service
```

Each service should appear in Eureka within ~30 seconds.

### 4. Bring up the frontend

```bash
docker compose up -d frontend
```

### 5. Smoke test

First, generate a sample PDF (or use your own loan application):

```bash
javac infra/scripts/GenSamplePdf.java -d /tmp/classes
java -cp /tmp/classes GenSamplePdf
# Creates loan-application-sample.pdf in current directory
```

Then run the end-to-end workflow:

```bash
# Login (dev credentials from DemoUserSeeder)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@finsight.ai","password":"demo"}' | jq -r '.token')
echo "Token: $TOKEN"

# Start a workflow
curl -X POST http://localhost:8080/api/v1/workflows \
  -H "Authorization: Bearer $TOKEN" \
  -F 'metadata={"title":"Demo Loan"};type=application/json' \
  -F 'file=@loan-application-sample.pdf'

# Inspect
curl http://localhost:8080/api/v1/workflows -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/v1/workflows/1/audit-log -H "Authorization: Bearer $TOKEN"

# Analytics
curl http://localhost:8080/api/v1/analytics/summary -H "Authorization: Bearer $TOKEN"
```

### 6. Local (no Docker) for the workflow-service alone

```bash
docker compose up -d postgres kafka zookeeper zipkin

export DB_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export ZIPKIN_URL=http://localhost:9411/api/v2/spans

cd workflow-service
mvn spring-boot:run
```

Then hit <http://localhost:8084/swagger-ui.html>.

## Tearing down

```bash
docker compose down            # keep volumes
docker compose down -v         # wipe everything
```
