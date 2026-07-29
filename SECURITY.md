# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| `main`  | :white_check_mark: |
| Others  | :x:                |

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Please report security issues by emailing **security@finsight.ai** (or the project maintainer's contact).
Include:
- A description of the vulnerability
- Steps to reproduce
- Potential impact assessment
- Suggested remediation (optional)

You will receive a response within **48 hours**, and a fix will be targeted within **7 days** for critical issues.

## Security Architecture

### Authentication
- JWT access tokens (24h TTL) signed with HMAC-SHA256
- Refresh tokens stored in Redis with TTL (7 days) — server-side revocable
- BCrypt password hashing (cost factor 12)
- Rate limiting on `/auth/login` and `/auth/refresh` (5 req/min/IP)

### Transport
- All production traffic must be served over HTTPS (nginx TLS termination)
- HSTS header enforced at nginx layer
- CORS origin allowlist is env-driven (`CORS_ALLOWED_ORIGINS`) — never `*` in production

### API Gateway
- All requests pass through JWT validation before routing
- Rate limiting per endpoint
- Request logging (no headers/body to prevent credential leakage)

### Document Security
- PDF magic-byte validation (`%PDF-` header) before processing
- ClamAV stub integration (upgrade to real daemon for production)
- Files stored in MinIO with bucket-level ACL (private)
- Pre-signed download URLs (60-second TTL)

### Secrets Management
- All secrets via environment variables (never hard-coded)
- Generate JWT_SECRET: `openssl rand -base64 48 | tr -d '\n'`
- Rotate JWT_SECRET by updating the env var and redeploying (all existing tokens immediately invalidated)
- MinIO credentials rotated via MinIO admin API

### LLM Security
- Document text is always in the **user message only** — never concatenated into system prompts (prevents prompt injection)
- API keys stored in environment variables, never logged

## Known Limitations (MVP)

- ClamAV is implemented as a production-grade stub (magic-byte + EICAR detection) — upgrade to real daemon for production
- No mTLS between microservices (services trust inbound connections from the Docker bridge network)
- Kafka is unauthenticated in the default compose file — enable SASL/SCRAM for production
