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
      <div className="mb-10 animate-slide-up flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-5xl lg:text-6xl font-display font-bold text-white mb-2 tracking-tighter uppercase leading-none">
            {greeting},<br/>
            <span className="text-brand-500">@{user?.username}</span> 
            <span className="animate-[pulse_3s_ease-in-out_infinite] origin-bottom-right inline-block hover:animate-[spin_1s_ease-in-out] ml-4">👋</span>
          </h1>
        </div>
        <div className="text-gray-400 font-mono text-sm max-w-xs border-l-2 border-brand-500 pl-4 py-1">
          SYSTEM STATUS: ONLINE<br/>
          OVERVIEW OF AI-POWERED CODE REVIEWS
        </div>
      </div>

      {/* Stats grid — Asymmetrical Bento Box */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        
        {/* Repositories - Large Feature Box */}
        <div className="bento-card acid md:col-span-2 row-span-2 bg-charcoal-800 flex flex-col justify-between group animate-slide-up" style={{ animationDelay: `100ms` }}>
          <div className="flex justify-between items-start mb-12">
            <div className="w-16 h-16 rounded-xl flex items-center justify-center bg-acid-500 text-charcoal-900 shadow-solid-sm shadow-black border-2 border-black group-hover:-translate-y-1 transition-transform">
              <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="square" strokeLinejoin="miter" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" /></svg>
            </div>
            <span className="badge badge-success">Active</span>
          </div>
          <div>
            <div className="text-7xl lg:text-8xl font-display font-bold text-white mb-2 tracking-tighter leading-none group-hover:text-acid-400 transition-colors">—</div>
            <div className="text-xl font-bold uppercase tracking-widest text-gray-500">Repositories</div>
          </div>
        </div>

        {/* Pull Requests */}
        <div className="bento-card primary bg-charcoal-800 flex flex-col justify-between group animate-slide-up" style={{ animationDelay: `200ms` }}>
          <div className="flex justify-between items-start mb-8">
            <div className="w-12 h-12 rounded-lg flex items-center justify-center bg-brand-500 text-charcoal-900 border-2 border-black">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="square" strokeLinejoin="miter" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" /></svg>
            </div>
          </div>
          <div>
            <div className="text-5xl font-display font-bold text-white mb-1 tracking-tighter group-hover:text-brand-400 transition-colors">—</div>
            <div className="text-sm font-bold uppercase tracking-widest text-gray-500">Pull Requests</div>
          </div>
        </div>

        {/* Reviews */}
        <div className="bento-card bg-charcoal-800 flex flex-col justify-between group animate-slide-up" style={{ animationDelay: `300ms`, '--border-hover': 'var(--burnt)' }}>
          <div className="flex justify-between items-start mb-8">
            <div className="w-12 h-12 rounded-lg flex items-center justify-center bg-burnt-500 text-charcoal-900 border-2 border-black">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5"><path strokeLinecap="square" strokeLinejoin="miter" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            </div>
          </div>
          <div>
            <div className="text-5xl font-display font-bold text-white mb-1 tracking-tighter group-hover:text-burnt-400 transition-colors">—</div>
            <div className="text-sm font-bold uppercase tracking-widest text-gray-500">Reviews</div>
          </div>
        </div>

      </div>

      {/* Getting started card */}
      <div className="bento-card primary bg-brand-900/30 border-brand-500/30 animate-slide-up" style={{ animationDelay: '400ms' }}>
        <div className="relative z-10 flex flex-col md:flex-row gap-8 items-start md:items-center">
          <div className="flex-1">
            <h2 className="text-2xl font-display font-bold text-white mb-6 uppercase tracking-wider flex items-center gap-3">
              <span className="text-3xl">🚀</span> Getting Started
            </h2>
            <ol className="space-y-4 font-mono text-sm text-gray-300">
              {[
                'Connect your GitHub account (done ✅)',
                'Select a repository to enable AI review',
                'Open a Pull Request — PR Reviewer takes over',
                'Inline comments appear directly on your PR',
              ].map((step, i) => (
                <li key={i} className="flex items-center gap-4 group">
                  <span className="w-8 h-8 rounded-lg bg-charcoal-900 border-2 border-brand-500 text-brand-400 font-bold flex items-center justify-center flex-shrink-0 group-hover:bg-brand-500 group-hover:text-charcoal-900 transition-colors shadow-solid-sm shadow-black">
                    0{i + 1}
                  </span>
                  <span className="group-hover:text-white transition-colors">{step}</span>
                </li>
              ))}
            </ol>
          </div>
          <div className="hidden md:block w-48 h-48 opacity-20 group-hover:opacity-100 transition-opacity">
             <svg viewBox="0 0 100 100" className="w-full h-full text-brand-500 animate-spin" style={{animationDuration: '20s'}}>
               <path fill="currentColor" d="M50 0 A 50 50 0 1 1 49.99 0 M 50 10 A 40 40 0 1 0 50.01 10 M 50 20 A 30 30 0 1 1 49.99 20" />
             </svg>
          </div>
        </div>
      </div>
    </AppShell>
  )
}

