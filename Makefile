# FinSight-AI — Developer Makefile
# Usage: make <target>

COMPOSE = docker compose
SERVICES = config-server discovery-server api-gateway auth-service document-service ai-service workflow-service notification-service frontend

.PHONY: up down logs rebuild seed test clean ps help

## Start all services in detached mode
up:
	@cp -n .env.example .env 2>/dev/null && echo "Created .env from .env.example" || true
	$(COMPOSE) up --build -d
	@echo ""
	@echo "🚀 FinSight-AI is starting up. Service URLs:"
	@echo "  Frontend     http://localhost:3000"
	@echo "  API Gateway  http://localhost:8080"
	@echo "  Eureka       http://localhost:8761"
	@echo "  Kafka UI     http://localhost:8090"
	@echo "  Grafana      http://localhost:3001  (admin/admin)"
	@echo "  Zipkin       http://localhost:9411"
	@echo "  MinIO        http://localhost:9001  (minioadmin/minioadmin)"
	@echo "  Mailpit      http://localhost:8025"

## Stop all services
down:
	$(COMPOSE) down

## Stop and remove all volumes (destructive!)
clean:
	$(COMPOSE) down -v --remove-orphans
	@echo "⚠️  All volumes removed."

## Tail logs (all services, or: make logs SERVICE=auth-service)
logs:
ifdef SERVICE
	$(COMPOSE) logs -f $(SERVICE)
else
	$(COMPOSE) logs -f
endif

## Show running containers and health status
ps:
	$(COMPOSE) ps

## Rebuild and restart a specific service (make rebuild SERVICE=auth-service)
rebuild:
ifdef SERVICE
	$(COMPOSE) up --build -d $(SERVICE)
else
	$(COMPOSE) up --build -d $(SERVICES)
endif

## Seed the database with demo data (runs after 'up')
seed:
	@echo "Waiting for postgres to be ready..."
	@until docker exec finsight-postgres pg_isready -U finsight_user -d finsight_db; do sleep 2; done
	@echo "Seeding database..."
	@docker exec -i finsight-postgres psql -U finsight_user -d finsight_db < infra/scripts/seed-dev-data.sql
	@echo "✅ Database seeded."

## Run all backend tests (requires local JDK 21 + Maven)
test:
	./mvnw -B -ntp clean test

## Run frontend tests
test-frontend:
	cd frontend && npm ci && npm test -- --run

## Quick health check against the running stack
health:
	@curl -s http://localhost:8080/actuator/health | python3 -m json.tool || echo "Gateway not yet ready"

help:
	@grep -E '^## ' Makefile | sed 's/## //'
