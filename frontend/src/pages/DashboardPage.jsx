import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.jsx'

/**
 * Dashboard — landing page after login.
 * Milestone 1: shell only. Stats and reviews added in later milestones.
 */
export default function DashboardPage() {
  const { user } = useAuth()
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'

  return (
    <AppShell>
      {/* Page header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-1">
          {greeting},{' '}
          <span className="gradient-text">@{user?.username}</span> 👋
        </h1>
        <p className="text-gray-400">Here&apos;s an overview of your AI-powered code reviews.</p>
      </div>

      {/* Stats grid — placeholder data for Milestone 1 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Repositories',    value: '—', icon: '◫', color: 'text-brand-400' },
          { label: 'Pull Requests',   value: '—', icon: '⤵', color: 'text-purple-400' },
          { label: 'Reviews',         value: '—', icon: '◈', color: 'text-emerald-400' },
          { label: 'Comments Posted', value: '—', icon: '◇', color: 'text-amber-400' },
        ].map(stat => (
          <div key={stat.label} className="glass-card">
            <div className={`text-2xl mb-2 ${stat.color}`}>{stat.icon}</div>
            <div className="text-2xl font-bold text-white mb-1">{stat.value}</div>
            <div className="text-xs text-gray-500">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Getting started card */}
      <div className="glass-card">
        <h2 className="text-lg font-semibold text-white mb-4">🚀 Getting Started</h2>
        <ol className="space-y-3 text-sm text-gray-400">
          {[
            'Connect your GitHub account (done ✅)',
            'Select a repository to enable AI review',
            'Open a Pull Request — Gemini will review it automatically',
            'Inline comments will appear directly on your PR',
          ].map((step, i) => (
            <li key={i} className="flex items-start gap-3">
              <span className="w-5 h-5 rounded-full bg-brand-600/30 text-brand-300 border border-brand-500/30 text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
                {i + 1}
              </span>
              {step}
            </li>
          ))}
        </ol>
      </div>
    </AppShell>
  )
}
