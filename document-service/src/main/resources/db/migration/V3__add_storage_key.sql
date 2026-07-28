-- Add storage_key column to support both MinIO object paths and local disk paths
ALTER TABLE documents ADD COLUMN IF NOT EXISTS storage_key VARCHAR(1024);
