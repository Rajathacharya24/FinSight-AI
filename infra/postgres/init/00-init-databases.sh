#!/bin/bash
set -e

# All FinSight services share a single database `finsight_db` and isolate state
# via per-service Postgres schemas (auth, document, ai, workflow). Flyway in each
# service is configured with `create-schema: true`, so this script only needs to
# enable the pgvector extension that the `ai` schema depends on.

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS vector;
EOSQL
