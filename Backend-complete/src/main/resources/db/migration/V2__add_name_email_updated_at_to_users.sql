-- Migration V2: Add name, email, updated_at to existing users table and alter phone to be nullable

-- 1. Add columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;

-- 2. Make phone nullable for email-based CRUD users
ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;

-- 3. Backfill any existing users so they satisfy NOT NULL constraints
UPDATE users SET name = COALESCE(name, 'User ' || id) WHERE name IS NULL;
UPDATE users SET email = COALESCE(email, 'user' || id || '@onncall.local') WHERE email IS NULL;
UPDATE users SET updated_at = COALESCE(updated_at, created_at, NOW()) WHERE updated_at IS NULL;

-- 4. Apply NOT NULL constraints
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL;

-- 5. Add unique constraint on email
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_email'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
    END IF;
END $$;
