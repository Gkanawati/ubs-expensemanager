-- Remove REQUIRES_REVISION from check constraint
ALTER TABLE expenses DROP CONSTRAINT IF EXISTS expenses_status_check;

ALTER TABLE expenses ADD CONSTRAINT expenses_status_check
CHECK (status IN ('PENDING', 'APPROVED_BY_MANAGER', 'APPROVED_BY_FINANCE', 'REJECTED'));

-- Update existing expenses with REQUIRES_REVISION to PENDING
UPDATE expenses SET status = 'PENDING' WHERE status = 'REQUIRES_REVISION';

-- Update audit table similarly (no constraint, but for data cleanliness)
UPDATE expenses_AUD SET status = 'PENDING' WHERE status = 'REQUIRES_REVISION';
