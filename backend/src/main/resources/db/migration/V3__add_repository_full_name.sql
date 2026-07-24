-- =============================================================
-- V3 — Add full_name to repositories
-- PR Reviewer Application
-- =============================================================
--
-- Design rationale:
--   Later milestones (webhook registration, PR fetch, review
--   comment posting) constantly need the "owner/repo" string.
--   Storing it avoids repeatedly reconstructing it from the
--   owner and name columns.
--
-- Migration strategy (two steps):
--   1. ADD COLUMN as nullable — existing rows won't violate NOT NULL.
--   2. Backfill: concatenate the existing owner and name columns.
--   3. SET NOT NULL — enforced after all rows have a value.
--
-- Length: 500 characters.
--   GitHub enforces owner max 39 chars and repo max 100 chars
--   (total ~141 with slash). 500 gives ample headroom.
-- =============================================================

-- Step 1: Add column as nullable
ALTER TABLE repositories
    ADD COLUMN full_name VARCHAR(500);

-- Step 2: Backfill from existing owner + name columns
UPDATE repositories
    SET full_name = owner || '/' || name
    WHERE full_name IS NULL;

-- Step 3: Enforce NOT NULL constraint
ALTER TABLE repositories
    ALTER COLUMN full_name SET NOT NULL;
