-- Add confidence_score column to track AI extraction quality
ALTER TABLE loan_application_extraction ADD COLUMN IF NOT EXISTS confidence_score DOUBLE PRECISION;
