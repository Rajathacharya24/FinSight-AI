# FinSight-AI — Runbook

## Quick Reference

| Problem | First check | Command |
|---------|-------------|---------|
| Service not starting | Container logs | `make logs SERVICE=<name>` |
| Workflow stuck | DB status + DLQ | See [Stuck Workflow](#stuck-workflow) |
| Auth failing | JWT secret mismatch | See [JWT Secret Rotation](#jwt-secret-rotation) |
| Email not sending | Mailpit UI | http://localhost:8025 |
| Kafka lag | Kafka UI | http://localhost:8090 |
| Tracing | Zipkin | http://localhost:9411 |
| Metrics | Grafana | http://localhost:3001 |

---

## Diagnosing a Stuck Workflow

### Symptoms
- Workflow in `SCANNING`, `EXTRACTING_TEXT`, or `AI_EXTRACTION` for > 5 minutes
- No new audit log entries

### Steps

1. **Check workflow status directly:**
   ```bash
   curl http://localhost:8080/api/v1/workflows/<id>
   ```

2. **Check Kafka consumer lag** in Kafka UI (`:8090`):
   - `workflow-service` consumer group should have lag 0
   - If lag is growing, `workflow-service` may be crashed

3. **Check workflow-service logs:**
   ```bash
   make logs SERVICE=workflow-service
   ```

4. **Check for dead-letter events:**
   - In Kafka UI, inspect `finsight.document.uploaded-dlt`, `finsight.extraction.completed-dlt`, `finsight.analysis.completed-dlt` topics
   - Events in DLQ mean the consumer failed 4 times — the workflow is now in `FAILED` state

5. **Manually fail and re-trigger** (if needed):
   ```sql
   -- Connect to postgres
   docker exec -it finsight-postgres psql -U finsight_user -d finsight_db
   
   -- Check workflow status
   SELECT id, status, current_step, error_message, updated_at
   FROM workflow_instances WHERE id = <id>;
   
   -- Manually reset to UPLOADED for re-processing (use with caution!)
   UPDATE workflow_instances SET status = 'UPLOADED', error_message = NULL
   WHERE id = <id>;
   ```

---

## DLQ Replay

Dead-letter queue events are written to `*.dlt` topics. To replay:

1. Open Kafka UI (`:8090`) → Topics → `finsight.document.uploaded-dlt`
2. Identify the failed message(s)
3. Use `kafka-console-producer` to replay into the original topic:
   ```bash
   docker exec finsight-kafka kafka-console-consumer \
     --bootstrap-server kafka:29092 \
     --topic finsight.document.uploaded-dlt \
     --from-beginning --max-messages 1 | \
   docker exec -i finsight-kafka kafka-console-producer \
     --bootstrap-server kafka:29092 \
     --topic finsight.document.uploaded
   ```

---

## JWT Secret Rotation

**Warning:** Rotating the JWT secret immediately invalidates all existing access tokens. Users will need to log in again.

1. Generate a new secret:
   ```bash
   openssl rand -base64 48 | tr -d '\n'
   ```
2. Update `.env`:
   ```
   JWT_SECRET=<new secret>
   ```
3. Restart `api-gateway` and `auth-service`:
   ```bash
   make rebuild SERVICE=api-gateway
   make rebuild SERVICE=auth-service
   ```
4. All refresh tokens in Redis are also invalidated implicitly (they can't produce valid JWTs).
5. Inform users they will need to log in again.

---

## Scaling a Service

To scale `workflow-service` to 3 instances:
```bash
docker compose up -d --scale workflow-service=3
```

Note: Kafka consumer group (`workflow-service`) will rebalance automatically.
Ensure the Eureka instance `prefer-ip-address: true` is set (it is by default).

---

## Database Backup

```bash
docker exec finsight-postgres pg_dump \
  -U finsight_user finsight_db \
  > backup_$(date +%Y%m%d_%H%M%S).sql
```

## Database Restore

```bash
docker exec -i finsight-postgres psql \
  -U finsight_user finsight_db < backup_file.sql
```

---

## Grafana Dashboards

After `make up`:
1. Open http://localhost:3001 (admin/admin)
2. Navigate to Dashboards → workflow-service
3. Available panels:
   - Request rate and latency (p50/p95/p99)
   - Kafka consumer lag per topic
   - JVM heap and GC pressure
   - Workflow throughput by terminal state (APPROVED/REJECTED/FAILED)
   - Extraction confidence score distribution

---

## Checking Distributed Traces

1. Open Zipkin: http://localhost:9411
2. Find traces by:
   - Service name: `api-gateway`, `workflow-service`, etc.
   - Duration > Xms (useful for finding slow LLM calls)
3. A full upload → review flow creates a trace spanning:
   `api-gateway → auth-service → workflow-service → document-service → ai-service → notification-service`

---

## Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `JWT_SECRET: No value present` | .env not loaded | `cp .env.example .env && make up` |
| `Connection to postgres refused` | Postgres not healthy yet | Wait 30s and retry |
| `LLM extraction timeout` | Groq API slow/down | Check GROQ_API_KEY; ai-service has 30s circuit breaker |
| `ClamAV EICAR test failed` | Upload filename contains "eicar" | Rename file — EICAR detection is intentional |
| `404 on /api/v1/ai/**` | Gateway route mismatch | Verify gateway routes ai-service to `/api/v1/ai/**` |
