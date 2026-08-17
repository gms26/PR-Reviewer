import AppShell from '../components/AppShell.jsx'

/**
 * Settings page — stub for Milestone 1.
 * Full implementation in Milestone 12d.
 */
export default function SettingsPage() {
  return (
    <AppShell>
      <div className="mb-8 pb-6 border-b border-subtle">
        <h1 className="text-5xl font-display font-bold text-ink-900 mb-2 tracking-tight">Settings</h1>
        <p className="text-ink-600 text-lg">Configure your PR Reviewer preferences.</p>
      </div>
      <div className="glass-card text-center py-16 bg-white">
        <div className="text-4xl mb-4 text-ink-300">◎</div>
        <p className="text-ink-600 text-lg font-display">Settings coming in Milestone 12d.</p>
      </div>
    </AppShell>
  )
}
