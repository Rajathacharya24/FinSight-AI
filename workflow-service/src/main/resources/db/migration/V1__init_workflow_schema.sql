CREATE TABLE IF NOT EXISTS workflow_instances (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) UNIQUE NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    borrower_name VARCHAR(100),
    loan_amount NUMERIC(15, 2),
    annual_income NUMERIC(15, 2),
    status VARCHAR(50) NOT NULL,
    decision VARCHAR(50),
    decision_reason TEXT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_steps (
    id BIGSERIAL PRIMARY KEY,
    workflow_id VARCHAR(36) REFERENCES workflow_instances(id) ON DELETE CASCADE,
    agent_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    output_details TEXT,
    executed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_audit_log (
    id BIGSERIAL PRIMARY KEY,
    workflow_id VARCHAR(36) REFERENCES workflow_instances(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    performed_by VARCHAR(100) DEFAULT 'SYSTEM',
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
