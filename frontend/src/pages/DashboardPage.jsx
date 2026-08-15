import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.jsx'

/**
 * Dashboard — landing page after login.
 */
export default function DashboardPage() {
  const { user } = useAuth()
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'

  return (
    <AppShell>
      {/* Page header with entrance animation */}
      <div className="mb-10 animate-slide-up">
        <h1 className="text-4xl font-bold text-white mb-2 tracking-tight flex items-center gap-3">
          {greeting},{' '}
          <span className="gradient-text">@{user?.username}</span> 
          <span className="animate-[pulse_3s_ease-in-out_infinite] origin-bottom-right inline-block hover:animate-[spin_1s_ease-in-out]">👋</span>
        </h1>
        <p className="text-gray-400 text-lg font-medium">Here&apos;s an overview of your AI-powered code reviews.</p>
      </div>

      {/* Stats grid — placeholder data for Milestone 1 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        {[
          { label: 'Repositories',    value: '—', color: 'text-brand-400',   bg: 'bg-brand-500/10',    shadow: 'hover:shadow-brand-500/20',
            icon: <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" /></svg> },
          { label: 'Pull Requests',   value: '—', color: 'text-purple-400',  bg: 'bg-purple-500/10',   shadow: 'hover:shadow-purple-500/20',
            icon: <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" /></svg> },
          { label: 'Reviews',         value: '—', color: 'text-emerald-400', bg: 'bg-emerald-500/10',  shadow: 'hover:shadow-emerald-500/20',
            icon: <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg> },
          { label: 'Comments Posted', value: '—', color: 'text-cyan-400',    bg: 'bg-cyan-500/10',     shadow: 'hover:shadow-cyan-500/20',
            icon: <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg> },
        ].map((stat, idx) => (
          <div key={stat.label} className={`glass-card relative overflow-hidden group ${stat.shadow} animate-slide-up`} style={{ animationDelay: `${idx * 100}ms` }}>
            <div className={`absolute -right-4 -top-4 w-24 h-24 rounded-full blur-2xl ${stat.bg} group-hover:scale-150 transition-transform duration-500`}></div>
            <div className="relative z-10">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${stat.bg} ${stat.color} border border-white/5 shadow-inner`}>
                {stat.icon}
              </div>
              <div className="text-3xl font-bold text-white mb-1 tracking-tight">{stat.value}</div>
              <div className="text-sm font-medium text-gray-400">{stat.label}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Getting started card */}
      <div className="glass-card relative overflow-hidden animate-slide-up" style={{ animationDelay: '400ms' }}>
        <div className="absolute top-0 right-0 w-64 h-64 bg-brand-500/5 rounded-full blur-3xl"></div>
        <div className="relative z-10">
          <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-3">
            <span className="text-2xl">🚀</span> Getting Started
          </h2>
          <ol className="space-y-4 text-sm text-gray-300">
            {[
              'Connect your GitHub account (done ✅)',
              'Select a repository to enable AI review',
              'Open a Pull Request — PR Reviewer will review it automatically',
              'Inline comments will appear directly on your PR',
            ].map((step, i) => (
              <li key={i} className="flex items-center gap-4 group">
                <span className="w-8 h-8 rounded-full bg-brand-500/10 text-brand-400 border border-brand-500/20 font-semibold flex items-center justify-center flex-shrink-0 group-hover:bg-brand-500 group-hover:text-white transition-colors duration-300 shadow-[0_0_10px_rgba(91,110,243,0.1)] group-hover:shadow-[0_0_15px_rgba(91,110,243,0.4)]">
                  {i + 1}
                </span>
                <span className="group-hover:text-white transition-colors duration-300 font-medium text-base">{step}</span>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </AppShell>
  )
}

