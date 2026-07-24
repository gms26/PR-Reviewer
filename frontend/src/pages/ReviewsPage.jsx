import AppShell from '../components/AppShell.jsx'

/**
 * Reviews page — stub for Milestone 1.
 * Full implementation in Milestone 12c.
 */
export default function ReviewsPage() {
  return (
    <AppShell>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-1">Reviews</h1>
        <p className="text-gray-400">View AI-generated code reviews for your Pull Requests.</p>
      </div>
      <div className="glass-card text-center py-16">
        <div className="text-4xl mb-4">◈</div>
        <p className="text-gray-400">Review history coming in Milestone 12c.</p>
      </div>
    </AppShell>
  )
}
