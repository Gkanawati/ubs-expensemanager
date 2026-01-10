-- 1. Add columns WITHOUT NOT NULL constraints
ALTER TABLE users
    ADD COLUMN name VARCHAR(255),
ADD COLUMN active BOOLEAN DEFAULT TRUE,
ADD COLUMN manager_id BIGINT;

-- 2. Backfill existing records
UPDATE users
SET
    name = COALESCE(name, email),
    active = COALESCE(active, TRUE);

-- 3. Apply NOT NULL constraints after data is valid
ALTER TABLE users
    ALTER COLUMN name SET NOT NULL,
ALTER COLUMN active SET NOT NULL;

-- 4. Add self-referencing foreign key
ALTER TABLE users
    ADD CONSTRAINT fk_users_manager
        FOREIGN KEY (manager_id)
            REFERENCES users(id);
