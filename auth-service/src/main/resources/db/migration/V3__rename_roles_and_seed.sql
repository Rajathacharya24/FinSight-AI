-- V3: Rename roles to match APPLICANT/REVIEWER/ADMIN spec, and fix default
UPDATE users SET role = 'ROLE_APPLICANT' WHERE role = 'ROLE_USER';
UPDATE users SET role = 'ROLE_REVIEWER'  WHERE role = 'ROLE_ANALYST';
-- ROLE_ADMIN stays the same

ALTER TABLE users ALTER COLUMN role SET DEFAULT 'ROLE_APPLICANT';

-- V3: Add seed demo users (idempotent)
-- Password: 'Password1!' → BCrypt cost 12 (pre-computed hash for dev seeding only)
INSERT INTO users (username, email, password, role)
VALUES
  ('applicant_demo',  'applicant@finsight.ai',  '$2a$12$xkHMN2.zKoX9RCDllzB.mO5I7I0/3LDkMqcDXYwsqHK4eFBZe7a2y', 'ROLE_APPLICANT'),
  ('reviewer_demo',   'reviewer@finsight.ai',   '$2a$12$xkHMN2.zKoX9RCDllzB.mO5I7I0/3LDkMqcDXYwsqHK4eFBZe7a2y', 'ROLE_REVIEWER'),
  ('admin_demo',      'admin@finsight.ai',       '$2a$12$xkHMN2.zKoX9RCDllzB.mO5I7I0/3LDkMqcDXYwsqHK4eFBZe7a2y', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;
