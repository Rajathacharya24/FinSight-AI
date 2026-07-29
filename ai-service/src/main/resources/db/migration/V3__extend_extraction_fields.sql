-- V3: Extend loan_application_extraction with new fields for Phase 5
ALTER TABLE loan_application_extraction
    ADD COLUMN IF NOT EXISTS employment_status       VARCHAR(64),
    ADD COLUMN IF NOT EXISTS existing_debts          TEXT,
    ADD COLUMN IF NOT EXISTS field_confidence_scores TEXT,
    ADD COLUMN IF NOT EXISTS extraction_duration_ms  BIGINT,
    ADD COLUMN IF NOT EXISTS tokens_used             INTEGER,
    ADD COLUMN IF NOT EXISTS workflow_id             BIGINT;

CREATE INDEX IF NOT EXISTS idx_extraction_workflow_id
    ON loan_application_extraction(workflow_id);
