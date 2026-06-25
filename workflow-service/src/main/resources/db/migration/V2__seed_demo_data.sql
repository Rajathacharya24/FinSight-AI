-- Demo seed data for the FinSight workflow service.
-- Safe to apply to dev environments; production environments should skip this migration.

INSERT INTO workflow_instances (document_id, user_id, status, current_step,
    extraction_data, compliance_data, decision_data,
    created_at, updated_at)
VALUES
    (1001, 1, 'COMPLETED', 'RECOMMEND',
        '{"documentId":1001,"applicantName":"Alice Johnson","income":"95000","loanAmount":"180000","address":"742 Evergreen Ter"}',
        '{"compliant":true,"violations":[],"warnings":[],"summary":"OK"}',
        '{"recommendation":"APPROVE","rationale":"Strong applicant","confidenceScore":0.92}',
        NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '14 seconds'),
    (1002, 1, 'COMPLETED', 'RECOMMEND',
        '{"documentId":1002,"applicantName":"Bob Smith","income":"42000","loanAmount":"200000","address":"1 Oak Ave"}',
        '{"compliant":false,"violations":["Loan-to-income ratio 4.76 exceeds maximum 3.00"],"warnings":[],"summary":"Failed"}',
        '{"recommendation":"REJECT","rationale":"Ratio too high","confidenceScore":0.95}',
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '11 seconds'),
    (1003, 2, 'FAILED', 'EXTRACT', NULL, NULL, NULL,
        NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours' + INTERVAL '3 seconds');
