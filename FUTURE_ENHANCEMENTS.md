# Future Enhancements

Documented limitations and deferred decisions. None of these are bugs — they are
conscious scope boundaries for Phase 1.

---

## Webhook Secret Rotation

**Context:**
GitHub does not expose the configured webhook secret through the REST API. When you
fetch existing webhooks via `GET /repos/{owner}/{repo}/hooks`, the response includes
the webhook `config.url` and `content_type`, but the `secret` field is always
omitted for security reasons.

**Consequence:**
If `GITHUB_WEBHOOK_SECRET` changes (e.g., a required rotation), the application
cannot detect that the existing webhook was registered with the old secret. Incoming
webhook payloads will fail HMAC-SHA256 signature verification silently — the webhook
appears active on GitHub but the application will reject all payloads.

**Recommended strategy when rotating the secret:**

1. Delete the existing webhook via `DELETE /repos/{owner}/{repo}/hooks/{hook_id}`.
2. Recreate it via `POST /repos/{owner}/{repo}/hooks` with the new secret.
3. Persist the new `webhook_id` returned by GitHub.
4. Set `webhook_enabled = true`.

This is equivalent to a full disable + re-enable cycle for every monitored repository.

**Phase 1 does not automate secret rotation.**

A future phase may introduce an admin endpoint or a startup check that compares stored
webhook metadata against expected configuration and re-registers webhooks when a
mismatch is detected.

---

## Server-Side Repository Pagination

`GET /repos` currently fetches all pages from GitHub transparently and returns a
single flat list. This is acceptable for a resume/demo project where users are
unlikely to have thousands of repositories.

If a user has a very large number of repositories, this endpoint may be slow or
hit GitHub's rate limits. A future phase may introduce server-side pagination on the
`GET /repos` endpoint, exposing `page` and `per_page` query parameters to the client.
