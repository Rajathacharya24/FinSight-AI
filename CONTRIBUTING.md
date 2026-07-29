# Contributing to FinSight-AI

Thanks for wanting to contribute! This guide covers local setup, coding conventions, and the PR workflow.

## Prerequisites

| Tool       | Version     |
|------------|-------------|
| JDK        | 21 (Temurin) |
| Maven      | 3.9+        |
| Docker     | 24+         |
| Docker Compose | v2+    |
| Node.js    | 20+         |
| npm        | 10+         |

## Local Setup

```bash
# 1. Clone the repo
git clone https://github.com/your-org/finsight-ai.git
cd finsight-ai

# 2. Create .env from example
cp .env.example .env
# Edit .env — fill in GROQ_API_KEY and JWT_SECRET at minimum

# 3. Start the stack
make up

# 4. Verify all services are healthy
make health

# 5. Open the UI
open http://localhost:3000
```

## Running Tests

```bash
# All backend tests
make test

# Single service
mvn -pl auth-service clean test

# Frontend
make test-frontend
```

## Branch Naming

| Branch Type  | Pattern                     | Example                        |
|-------------|------------------------------|--------------------------------|
| Feature      | `feat/<short-description>`  | `feat/review-endpoint`         |
| Fix          | `fix/<short-description>`   | `fix/jwt-expiry-handling`      |
| Chore        | `chore/<short-description>` | `chore/update-deps`            |
| Docs         | `docs/<short-description>`  | `docs/api-guide`               |

## Adding a New Service

1. Create `<service-name>/` directory with `pom.xml`, `Dockerfile`, `src/`
2. Add the service as a module in the root `pom.xml`
3. Add a `docker-compose.yml` entry with `depends_on`, `healthcheck`, `restart`, `mem_limit`, `cpus`
4. Add a Prometheus scrape job to `infra/prometheus/prometheus.yml`
5. Add the service to the GitHub Actions Docker matrix in `.github/workflows/backend.yml`

## Commit Convention

Conventional Commits format:

```
feat(auth): add logout endpoint
fix(workflow): prevent state transition from FAILED
docs(readme): update port table with notification-service
chore(deps): bump spring-boot from 3.5.0-M1 to 3.5.1
```

## Flyway Migrations

- Never modify existing migration files — always create a new `V{n}__description.sql`
- Use `IF NOT EXISTS` for DDL statements to make migrations re-runnable in CI
- Test migrations by wiping the DB: `make clean && make up`

## Code Style

- Java: Google Java Style (enforced by Checkstyle — run `mvn checkstyle:check`)
- Frontend: ESLint + Prettier (run `cd frontend && npm run lint`)
- No `@SuppressWarnings` without a comment explaining why

## Pull Request Process

1. Open a draft PR early — discussion welcome
2. All CI checks must pass (build, test, lint)
3. At least 1 approving review required
4. Squash merge to main
