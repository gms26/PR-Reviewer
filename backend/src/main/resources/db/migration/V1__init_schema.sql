-- =============================================================
-- V1 — Initial Schema
-- PR Reviewer Application
-- =============================================================

-- Users: GitHub-authenticated users
CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    github_id    VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(255) NOT NULL,
    email        VARCHAR(255),
    access_token TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_github_id ON users (github_id);

-- Repositories: Selected repositories for review monitoring
CREATE TABLE repositories (
    id              BIGSERIAL    PRIMARY KEY,
    github_repo_id  BIGINT       NOT NULL UNIQUE,
    owner           VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    webhook_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    user_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_repos_github_repo_id ON repositories (github_repo_id);
CREATE INDEX idx_repos_user_id        ON repositories (user_id);

-- Pull Requests: Received via GitHub webhooks
CREATE TABLE pull_requests (
    id               BIGSERIAL    PRIMARY KEY,
    github_pr_number INTEGER      NOT NULL,
    repository_id    BIGINT       NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    title            TEXT         NOT NULL,
    description      TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    -- A PR number is unique within a repository
    UNIQUE (repository_id, github_pr_number)
);

CREATE INDEX idx_pr_repo_id ON pull_requests (repository_id);

-- Reviews: AI-generated review for a pull request
CREATE TABLE reviews (
    id              BIGSERIAL    PRIMARY KEY,
    pull_request_id BIGINT       NOT NULL REFERENCES pull_requests (id) ON DELETE CASCADE,
    ai_model        VARCHAR(255) NOT NULL,
    delivery_id     VARCHAR(255) NOT NULL UNIQUE, -- GitHub X-GitHub-Delivery header (idempotency key)
    review_time_ms  BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_pull_request_id ON reviews (pull_request_id);
CREATE INDEX idx_reviews_delivery_id     ON reviews (delivery_id);

-- Comments: Individual inline review comments
CREATE TABLE comments (
    id        BIGSERIAL    PRIMARY KEY,
    review_id BIGINT       NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    file      TEXT         NOT NULL,
    line      INTEGER      NOT NULL,
    severity  VARCHAR(50)  NOT NULL,
    category  VARCHAR(100) NOT NULL,
    message   TEXT         NOT NULL
);

CREATE INDEX idx_comments_review_id ON comments (review_id);
