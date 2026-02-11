-- V3: Add snapshot_values column to approval_logs table to store submission snapshots in JSONB format
ALTER TABLE approval_logs ADD COLUMN snapshot_values JSONB;
