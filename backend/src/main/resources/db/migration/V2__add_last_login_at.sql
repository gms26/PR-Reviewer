-- =============================================================
-- V2 — Add last_login_at to users
-- PR Reviewer Application
-- =============================================================
--
-- Design rationale:
--   last_login_at is NOT NULL because the authentication policy
--   guarantees every user has a lastLoginAt — it is set on first
--   login and updated on every subsequent login. Making it NOT NULL
--   lets the database enforce that invariant rather than relying
--   solely on application code.
--
-- Migration strategy (three steps):
--   1. ADD COLUMN as nullable — so existing rows are not immediately
--      in violation. PostgreSQL cannot add a NOT NULL column without
--      a DEFAULT unless there are zero rows, and a DEFAULT would
--      hardcode a timestamp that means nothing.
--   2. UPDATE to backfill — for any existing users, use created_at
--      as a reasonable proxy for their last login. This is the most
--      accurate value we have; NULL would be semantically wrong.
--   3. ALTER COLUMN SET NOT NULL — applied only after every row has
--      a value. The database will enforce the constraint from here on.
--
-- Type: TIMESTAMP WITH TIME ZONE (TIMESTAMPTZ)
--   Matches the Java entity field type (java.time.Instant), which is
--   always UTC. Using TIMESTAMPTZ avoids timezone-related surprises
--   when reading timestamps across different database clients or regions.
-- =============================================================

-- Step 1: Add the column as nullable
ALTER TABLE users
    ADD COLUMN last_login_at TIMESTAMPTZ;

-- Step 2: Backfill existing rows
--   For any user that existed before this migration, use created_at
--   as a proxy for last_login_at. This is semantically correct —
--   created_at records when they first logged in, which IS also their
--   last known login before this column existed.
UPDATE users
    SET last_login_at = created_at
    WHERE last_login_at IS NULL;

-- Step 3: Enforce NOT NULL now that all rows have a value
ALTER TABLE users
    ALTER COLUMN last_login_at SET NOT NULL;
