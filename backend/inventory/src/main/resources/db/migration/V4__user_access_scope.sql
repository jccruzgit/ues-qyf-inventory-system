ALTER TABLE users
    ADD COLUMN IF NOT EXISTS access_scope VARCHAR(30) NOT NULL DEFAULT 'ALL_LABS';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_users_access_scope'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_access_scope
                CHECK (access_scope IN ('ASSIGNED_ONLY', 'MULTI_LAB', 'ALL_LABS'));
    END IF;
END $$;
