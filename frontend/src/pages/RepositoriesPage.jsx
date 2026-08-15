import { useState, useEffect, useMemo } from 'react'
import AppShell from '../components/AppShell.jsx'
import { enableMonitoring, disableMonitoring, getRepositories, selectRepository } from '../api/repositoryApi.js'

// ─── Repo Card ────────────────────────────────────────────────────────────────

/**
 * Single repository card. Handles its own loading and error state
 * so the rest of the list keeps working if one selection fails.
 */
function RepoCard({ repo, onSelected }) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  function handleError(err) {
    if (!err.response) {
      setError('Unable to connect to the server. Please check your connection and try again.')
    } else {
      const status = err.response.status
      if (status === 403) {
        setError('You do not have permission to monitor this repository.')
      } else if (status === 404) {
        setError('This repository is no longer available.')
      } else if (status === 503) {
        setError('GitHub is temporarily unavailable. Please try again in a few minutes.')
      } else {
        setError('Something went wrong. Please try again.')
      }
    }
  }

  async function handleTrack() {
    if (loading) return
    setLoading(true)
    setError(null)
    try {
      const persisted = await selectRepository(repo.githubRepoId)
      onSelected(persisted)
    } catch (err) {
      handleError(err)
    } finally {
      setLoading(false)
    }
  }

  async function handleEnable() {
    if (loading) return
    setLoading(true)
    setError(null)
    try {
      const updated = await enableMonitoring(repo.id)
      onSelected(updated)
    } catch (err) {
      handleError(err)
    } finally {
      setLoading(false)
    }
  }

  async function handleDisable() {
    if (loading) return
    setLoading(true)
    setError(null)
    try {
      const updated = await disableMonitoring(repo.id)
      onSelected(updated)
    } catch (err) {
      handleError(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`glass-card relative overflow-hidden flex flex-col gap-3 group transition-all duration-300 animate-slide-up ${
      repo.webhookEnabled ? 'ring-2 ring-brand-500/50 shadow-[0_0_20px_rgba(91,110,243,0.15)]' : ''
    }`}>
      {repo.webhookEnabled && (
        <div className="absolute top-0 right-0 w-32 h-32 bg-brand-500/10 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-500"></div>
      )}

      {/* Header row */}
      <div className="flex items-start justify-between gap-3 relative z-10">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-semibold text-white text-base truncate group-hover:text-brand-300 transition-colors">{repo.fullName}</span>
            <span className={`badge ${repo.privateRepo ? 'badge-warning' : 'badge-info'}`}>
              {repo.privateRepo ? '🔒 Private' : '🌐 Public'}
            </span>
            {repo.webhookEnabled && (
              <span className="badge badge-success animate-pulse-slow">✓ Monitoring</span>
            )}
          </div>
        </div>

        {/* GitHub link */}
        {repo.htmlUrl && (
          <a
            href={repo.htmlUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-gray-500 hover:text-white bg-white/5 hover:bg-white/10 p-2 rounded-lg transition-all flex-shrink-0"
            title="View on GitHub"
            aria-label={`View ${repo.fullName} on GitHub`}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
            </svg>
          </a>
        )}
      </div>

      {/* Description */}
      {repo.description && (
        <p className="text-gray-400 text-sm leading-relaxed line-clamp-2 relative z-10">{repo.description}</p>
      )}

      {/* Error */}
      {error && (
        <p className="text-xs text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg px-3 py-2 relative z-10">
          {error}
        </p>
      )}

      {/* Action buttons */}
      <div className="mt-auto pt-4 relative z-10">
        {!repo.selected ? (
          <button
            id={`btn-repo-track-${repo.githubRepoId}`}
            onClick={handleTrack}
            disabled={loading}
            className="btn-secondary py-2.5 text-xs w-full disabled:opacity-60 disabled:cursor-not-allowed group-hover:bg-white/10"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-3 h-3 border-2 border-brand-400/40 border-t-brand-400 rounded-full animate-spin" />
                Tracking…
              </span>
            ) : 'Track Repository'}
          </button>
        ) : repo.webhookEnabled ? (
          <button
            id={`btn-repo-disable-${repo.id}`}
            onClick={handleDisable}
            disabled={loading}
            className="btn-danger py-2.5 text-xs w-full disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-3 h-3 border-2 border-red-400/40 border-t-red-400 rounded-full animate-spin" />
                Disabling…
              </span>
            ) : 'Disable Monitoring'}
          </button>
        ) : (
          <button
            id={`btn-repo-enable-${repo.id}`}
            onClick={handleEnable}
            disabled={loading}
            className="btn-primary py-2.5 text-xs w-full disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-3 h-3 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                Enabling…
              </span>
            ) : 'Enable Monitoring'}
          </button>
        )}
      </div>
    </div>
  )
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

function RepoCardSkeleton() {
  return (
    <div className="glass-card flex flex-col gap-3 relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/5 to-transparent animate-shimmer" style={{ backgroundSize: '200% 100%' }}></div>
      <div className="h-5 bg-white/10 rounded w-2/3 animate-pulse" />
      <div className="h-4 bg-white/5 rounded w-full mt-2 animate-pulse" />
      <div className="h-4 bg-white/5 rounded w-3/4 animate-pulse" />
      <div className="h-10 bg-white/10 rounded-lg mt-auto animate-pulse" />
    </div>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────

/**
 * Repositories page.
 *
 * Fetches all accessible GitHub repositories from the backend on mount,
 * lets the user filter them, and lets them add repos to PR monitoring.
 */
export default function RepositoriesPage() {
  const [repos,   setRepos]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)
  const [search,  setSearch]  = useState('')

  async function loadRepos() {
    setLoading(true)
    setError(null)
    try {
      const data = await getRepositories()
      setRepos(data)
    } catch (err) {
      const status = err?.response?.status
      if (status === 503) {
        setError('GitHub rate limit reached. Please try again in a few minutes.')
      } else if (status === 502) {
        setError('Could not reach GitHub. Please try again shortly.')
      } else {
        setError('Failed to load repositories. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadRepos() }, [])

  // Client-side filter on fullName or description
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return repos
    return repos.filter(r =>
      (r.fullName   || '').toLowerCase().includes(q) ||
      (r.description || '').toLowerCase().includes(q)
    )
  }, [repos, search])

  const monitoredCount = repos.filter(r => r.selected).length

  // When a repo is successfully updated (tracked, enabled, disabled), update its entry in the list
  function handleSelected(persisted) {
    setRepos(prev => prev.map(r =>
      r.githubRepoId === persisted.githubRepoId
        ? { ...r, ...persisted }
        : r
    ))
  }

  return (
    <AppShell>
      {/* Page header */}
      <div className="mb-8 animate-slide-up">
        <h1 className="text-4xl font-bold text-white mb-2 tracking-tight">Repositories</h1>
        <p className="text-gray-400 text-lg">
          Select repositories to enable AI-powered PR review.
          {!loading && repos.length > 0 && (
            <span className="ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-brand-500/20 text-brand-300 border border-brand-500/30 shadow-[0_0_10px_rgba(91,110,243,0.2)]">
              {monitoredCount} of {repos.length} monitored
            </span>
          )}
        </p>
      </div>

      {/* Error state */}
      {error && !loading && (
        <div className="glass-card mb-6 flex items-start gap-4 border-red-500/20 bg-red-500/5">
          <span className="text-2xl flex-shrink-0">⚠️</span>
          <div className="flex-1">
            <p className="text-red-400 font-medium mb-1">Error loading repositories</p>
            <p className="text-gray-400 text-sm mb-3">{error}</p>
            <button id="btn-retry-repos" onClick={loadRepos} className="btn-secondary text-xs py-2 px-4">
              Try again
            </button>
          </div>
        </div>
      )}

      {/* Search bar */}
      {!error && (
        <div className="mb-8 animate-slide-up" style={{ animationDelay: '100ms' }}>
          <div className="relative group">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-brand-400 transition-colors pointer-events-none">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
            </span>
            <input
              id="input-repo-search"
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search repositories…"
              className="w-full glass rounded-xl pl-12 pr-12 py-4 text-base text-white placeholder-gray-500 outline-none focus:ring-2 focus:ring-brand-500/50 focus:border-brand-500/50 transition-all shadow-lg"
            />
            {search && (
              <button
                onClick={() => setSearch('')}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white bg-white/5 hover:bg-white/10 p-1.5 rounded-md transition-all"
                aria-label="Clear search"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
              </button>
            )}
          </div>
        </div>
      )}

      {/* Loading skeleton */}
      {loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => <RepoCardSkeleton key={i} />)}
        </div>
      )}

      {/* Repository grid */}
      {!loading && !error && filtered.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(repo => (
            <RepoCard
              key={repo.githubRepoId}
              repo={repo}
              onSelected={handleSelected}
            />
          ))}
        </div>
      )}

      {/* No search results */}
      {!loading && !error && repos.length > 0 && filtered.length === 0 && (
        <div className="glass-card text-center py-12">
          <div className="text-4xl mb-3">🔍</div>
          <p className="text-white font-medium mb-1">No repositories match &quot;{search}&quot;</p>
          <p className="text-gray-500 text-sm">Try a different search term.</p>
        </div>
      )}

      {/* Empty state — no repos at all */}
      {!loading && !error && repos.length === 0 && (
        <div className="glass-card text-center py-16">
          <div className="text-5xl mb-4">◫</div>
          <p className="text-white font-semibold mb-2">No repositories found</p>
          <p className="text-gray-400 text-sm mb-6">
            Make sure your GitHub account has repositories accessible with the{' '}
            <code className="text-brand-400">repo</code> OAuth scope.
          </p>
          <a
            href="https://github.com/new"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-primary text-sm"
          >
            Create a repository on GitHub
          </a>
        </div>
      )}
    </AppShell>
  )
}
