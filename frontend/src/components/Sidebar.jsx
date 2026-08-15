import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.jsx'

// Premium SVG Icons
const icons = {
  dashboard: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="square" strokeLinejoin="miter">
      <rect x="3" y="3" width="7" height="9" />
      <rect x="14" y="3" width="7" height="5" />
      <rect x="14" y="12" width="7" height="9" />
      <rect x="3" y="16" width="7" height="5" />
    </svg>
  ),
  repositories: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="square" strokeLinejoin="miter">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
    </svg>
  ),
  reviews: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="square" strokeLinejoin="miter">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
      <line x1="16" y1="13" x2="8" y2="13" />
      <line x1="16" y1="17" x2="8" y2="17" />
      <polyline points="10 9 9 9 8 9" />
    </svg>
  ),
  settings: (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="square" strokeLinejoin="miter">
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </svg>
  )
}

const navItems = [
  { path: '/',             label: 'Dashboard',     icon: icons.dashboard },
  { path: '/repositories', label: 'Repositories',  icon: icons.repositories },
  { path: '/reviews',      label: 'Reviews',       icon: icons.reviews },
  { path: '/settings',     label: 'Settings',      icon: icons.settings },
]

/**
 * Main navigation sidebar.
 */
export default function Sidebar() {
  const { user, logout } = useAuth()
  const location = useLocation()

  return (
    <aside className="fixed left-6 top-6 bottom-6 w-64 flex flex-col bg-charcoal-800 border-2 border-charcoal-700 rounded-xl z-40 overflow-hidden shadow-solid">
      {/* Logo */}
      <div className="p-6 relative border-b-2 border-charcoal-700">
        <div className="absolute top-0 left-0 w-full h-1.5 bg-brand-500"></div>
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 rounded-lg flex items-center justify-center bg-brand-500 text-charcoal-900 shadow-solid-sm shadow-black border-2 border-black">
            <svg className="w-6 h-6 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="square" strokeLinejoin="miter" strokeWidth={2.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div>
            <h1 className="font-display font-bold text-white text-lg tracking-tight leading-none uppercase">PR Reviewer</h1>
            <p className="text-xs text-brand-400 font-bold uppercase tracking-wider mt-1">Control Panel</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-4 py-4 space-y-3">
        {navItems.map(item => {
          const active = location.pathname === item.path
          return (
            <Link
              key={item.path}
              to={item.path}
              id={`nav-${item.label.toLowerCase()}`}
              className={`group flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-display font-bold uppercase tracking-wider transition-all duration-200 border-2 ${
                active
                  ? 'bg-acid-500 text-charcoal-900 border-acid-500 shadow-solid-sm shadow-black'
                  : 'text-gray-400 border-transparent hover:text-white hover:border-charcoal-700 hover:bg-charcoal-700'
              }`}
            >
              <span className={`transition-transform duration-200 ${active ? 'text-charcoal-900' : 'group-hover:-translate-y-0.5 group-hover:text-acid-400'}`}>
                {item.icon}
              </span>
              <span>{item.label}</span>
            </Link>
          )
        })}
      </nav>

      {/* User profile */}
      <div className="p-4 m-4 bg-charcoal-900 rounded-lg border-2 border-charcoal-700 shadow-inner">
        <div className="flex items-center gap-3 mb-4">
          <img
            src={`https://github.com/${user?.username}.png?size=32`}
            alt={user?.username}
            className="w-10 h-10 rounded border-2 border-brand-500 shadow-solid-sm shadow-brand-500"
          />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-display font-bold text-white truncate">@{user?.username}</p>
            <p className="text-xs text-gray-500 truncate font-mono">{user?.email || 'SYSTEM.OP'}</p>
          </div>
        </div>
        <button
          id="btn-logout"
          onClick={logout}
          className="w-full btn-secondary text-xs py-2 uppercase tracking-widest"
        >
          Disconnect
        </button>
      </div>
    </aside>
  )
}

