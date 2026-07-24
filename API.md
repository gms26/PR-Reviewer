# API Documentation

## Authentication Endpoints
- `GET /oauth2/authorization/github`: Initiates the GitHub OAuth2 login flow.
- `GET /auth/me`: Returns the currently authenticated user's profile.
- `POST /logout`: Invalidates the current session.

## Repository Endpoints
- `GET /api/repositories`: Lists repositories available to the authenticated user (supports pagination).
- `POST /api/repositories/select`: Marks a specific repository for active monitoring.

## Webhook Endpoints
- `POST /api/webhook/github`: The primary ingestion point for GitHub Pull Request events. Requires a valid `X-Hub-Signature-256` header.

## Health
- `GET /actuator/health`: Returns application health status.
