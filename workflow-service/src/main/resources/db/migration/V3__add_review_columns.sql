-- V3: Add review columns, failure reason, and indexes for new workflow states

ALTER TABLE workflow_instances
    ADD COLUMN IF NOT EXISTS failure_reason     TEXT,
    ADD COLUMN IF NOT EXISTS review_action      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS review_reason      TEXT,
    ADD COLUMN IF NOT EXISTS reviewed_by        VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reviewed_at        TIMESTAMP WITH TIME ZONE;

-- Add audit log message column (for human-readable descriptions)
ALTER TABLE workflow_audit_logs
    ADD COLUMN IF NOT EXISTS message            TEXT;

-- Add actor column to audit log (for review decisions)
ALTER TABLE workflow_audit_logs
    ADD COLUMN IF NOT EXISTS actor              VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_workflow_instances_user_id   ON workflow_instances(user_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_created_at ON workflow_instances(created_at);
