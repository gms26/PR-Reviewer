import api from './axiosConfig.js'

/**
 * Repository API client.
 *
 * All calls go through the pre-configured Axios instance which:
 * - Targets /api base path (proxied to the Spring Boot backend by Vite)
 * - Sends session cookie with every request (withCredentials: true)
 * - Redirects to /login on 401 responses (via interceptor)
 *
 * API response policy:
 * Each function returns response.data exactly as received from the backend.
 * No field renaming, no client-side mapping, no adapter layer.
 * The backend RepositoryResponse DTO is the frontend contract.
 * If the contract changes, update the backend DTO — not this file.
 */

/**
 * Fetches all GitHub repositories accessible to the authenticated user.
 * The backend transparently handles GitHub pagination and returns a
 * single flat list. Each entry includes a `selected` flag indicating
 * whether the user is already tracking that repository.
 *
 * @returns {Promise<RepositoryResponse[]>} array of RepositoryResponse objects
 */
export function getRepositories() {
  return api.get('/repos').then(res => res.data)
}

/**
 * Adds a repository to the user's tracked list.
 * The backend validates ownership against a fresh GitHub API call
 * before persisting. Returns the full RepositoryResponse on success.
 *
 * @param {number} githubRepoId  the GitHub numeric repository ID
 * @returns {Promise<RepositoryResponse>} the persisted RepositoryResponse
 */
export function selectRepository(githubRepoId) {
  return api.post('/repos/select', { githubRepoId }).then(res => res.data)
}

/**
 * Enables GitHub webhook monitoring for a tracked repository.
 * The backend verifies ownership, detects any existing webhook for this
 * application's URL, and creates one if none exists.
 *
 * Returns the updated RepositoryResponse with:
 *   webhookEnabled: true
 *   webhookId: <GitHub-assigned ID>
 *
 * Idempotent: calling this when monitoring is already enabled returns
 * the current state immediately without creating a duplicate webhook.
 *
 * @param {number} repositoryId  the internal database ID of the repository
 *                               (NOT the GitHub repo ID)
 * @returns {Promise<RepositoryResponse>} updated RepositoryResponse
 */
export function enableMonitoring(repositoryId) {
  return api.post(`/repos/${repositoryId}/enable`).then(res => res.data)
}

/**
 * Disables GitHub webhook monitoring for a tracked repository.
 * The backend verifies ownership and deletes the webhook from GitHub,
 * then clears webhookEnabled and webhookId in the database.
 *
 * Returns the updated RepositoryResponse with:
 *   webhookEnabled: false
 *   webhookId: null
 *
 * Idempotent: calling this when monitoring is already disabled returns
 * the current state immediately without calling GitHub.
 *
 * @param {number} repositoryId  the internal database ID of the repository
 *                               (NOT the GitHub repo ID)
 * @returns {Promise<RepositoryResponse>} updated RepositoryResponse
 */
export function disableMonitoring(repositoryId) {
  return api.post(`/repos/${repositoryId}/disable`).then(res => res.data)
}
