CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS loan_application_extractions (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(36) UNIQUE NOT NULL,
    borrower_name VARCHAR(100),
    loan_amount NUMERIC(15, 2),
    annual_income NUMERIC(15, 2),
    application_date VARCHAR(50),
    loan_purpose VARCHAR(255),
    employment_status VARCHAR(100),
    confidence_score NUMERIC(5, 2),
    raw_extraction_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding vector(384),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_doc_chunks_doc_id ON document_chunks(document_id);
