CREATE TABLE IF NOT EXISTS workflow_instances (
    id              BIGSERIAL PRIMARY KEY,
    document_id     BIGINT,
    user_id         BIGINT NOT NULL DEFAULT 0,
    status          VARCHAR(50) NOT NULL,
    current_step    VARCHAR(50),
    extraction_data TEXT,
    compliance_data TEXT,
    decision_data   TEXT,
    error_message   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    workflow_id     BIGINT NOT NULL REFERENCES workflow_instances(id) ON DELETE CASCADE,
    step            VARCHAR(50) NOT NULL,
    agent           VARCHAR(50) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    input_summary   TEXT,
    output_summary  TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_audit_workflow_id ON workflow_audit_logs(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_instances_status ON workflow_instances(status);
