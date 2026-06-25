-- Enable pgvector extension (no-op if already created by Postgres init script)
CREATE EXTENSION IF NOT EXISTS vector;

-- Loan application extraction (deterministic regex extractor output)
CREATE TABLE IF NOT EXISTS loan_application_extraction (
    id              BIGSERIAL PRIMARY KEY,
    document_id     BIGINT NOT NULL,
    applicant_name  VARCHAR(255),
    income          VARCHAR(64),
    address         VARCHAR(512),
    loan_amount     VARCHAR(64),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_loan_application_extraction_document_id
    ON loan_application_extraction(document_id);

-- Document chunk embeddings for RAG (Spring AI PgVectorStore-compatible)
CREATE TABLE IF NOT EXISTS document_embedding (
    id        UUID PRIMARY KEY,
    content   TEXT,
    metadata  JSONB,
    embedding VECTOR(1536) -- text-embedding-3-small dimension
);

CREATE INDEX IF NOT EXISTS document_embedding_hnsw_idx
    ON document_embedding USING hnsw (embedding vector_cosine_ops);
