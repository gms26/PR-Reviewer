-- =============================================================
-- V6 — PR Metadata Extraction (Milestone 6)
-- PR Reviewer Application
-- =============================================================
--
-- Assumptions:
--   * Development database — no existing rows in pull_requests.
--   * repository_id remains NOT NULL: every webhook arrives only for
--     a repository that was persisted via the UI (Milestone 3/4).
--
-- Changes:
--   1. New nullable columns on pull_requests:
--        head_sha          — HEAD commit SHA, updated on every synchronize
--        github_pr_node_id — GraphQL node id, for future use
--
--   2. New table pull_request_events:
--        One row per webhook delivery, keyed by delivery_id (idempotency).
--        Foreign keys to both pull_requests and webhook_delivery.
-- =============================================================

-- -------------------------------------------------------
-- 1. Extend pull_requests with fields extracted from the
--    webhook payload.
-- -------------------------------------------------------

ALTER TABLE pull_requests
    ADD COLUMN IF NOT EXISTS head_sha          VARCHAR(40),
    ADD COLUMN IF NOT EXISTS github_pr_node_id VARCHAR(255);

-- -------------------------------------------------------
-- 2. New table: pull_request_events
--    One row per webhook delivery for a pull_request event.
-- -------------------------------------------------------

CREATE TABLE pull_request_events (
    id              BIGSERIAL    PRIMARY KEY,

    -- Parent PR — never null (repository was already persisted when webhook fired)
    pull_request_id BIGINT       NOT NULL
                    REFERENCES pull_requests (id) ON DELETE CASCADE,

    -- Natural idempotency key: the X-GitHub-Delivery UUID
    -- Also a logical FK into webhook_delivery, but not enforced as a hard FK
    -- because delivery rows may be cleaned up independently in future.
    delivery_id     VARCHAR(36)  NOT NULL,

    -- The X-GitHub-Event header value (always "pull_request" in Phase 1;
    -- stored for forward compatibility with other event types).
    event           VARCHAR(64)  NOT NULL,

    -- The "action" field from the payload (opened, synchronize, …)
    action          VARCHAR(64)  NOT NULL,

    -- GitHub login of the user who triggered the event
    sender_login    VARCHAR(255) NOT NULL,

    -- Present only for GitHub App webhooks; null for OAuth token webhooks
    installation_id BIGINT,

    received_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Unique constraint on delivery_id is the authoritative idempotency guard.
-- The application also has a pre-check, but this DB constraint is the
-- final safety net against race conditions.
CREATE UNIQUE INDEX uq_pre_delivery_id  ON pull_request_events (delivery_id);
CREATE        INDEX idx_pre_pull_request ON pull_request_events (pull_request_id);
