-- FinSight-AI — Dev Seed Data
-- Run after Flyway migrations via: make seed
-- Safe to re-run (uses ON CONFLICT DO NOTHING)

-- ─── Demo users (all password: 'Password1!', BCrypt cost 12) ─────────────
-- Pre-computed hash, generated once for dev/CI only.
-- DO NOT use this hash in production!

-- Demo users are inserted by the auth-service V3 migration.
-- This file seeds workflow and document data for UI testing.

-- ─── Demo Workflows ──────────────────────────────────────────────────────
INSERT INTO workflow_instances (document_id, user_id, status, current_step, created_at, updated_at)
VALUES
  (1, 1, 'REVIEW_PENDING', 'RECOMMEND', NOW() - INTERVAL '2 hours',  NOW() - INTERVAL '30 minutes'),
  (2, 1, 'APPROVED',       'RECOMMEND', NOW() - INTERVAL '1 day',    NOW() - INTERVAL '6 hours'),
  (3, 2, 'FAILED',         'EXTRACT',   NOW() - INTERVAL '3 hours',  NOW() - INTERVAL '2 hours'),
  (4, 2, 'UPLOADING',      'UPLOAD',    NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '5 minutes')
ON CONFLICT DO NOTHING;
