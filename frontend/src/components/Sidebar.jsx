import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.jsx'

const navItems = [
  { path: '/',             label: 'Dashboard',     icon: '⬡' },
  { path: '/repositories', label: 'Repositories',  icon: '◫' },
  { path: '/reviews',      label: 'Reviews',       icon: '◈' },
  { path: '/settings',     label: 'Settings',      icon: '◎' },
]

/**
 * Main navigation sidebar.
 */
export default function Sidebar() {
  const { user, logout } = useAuth()
  const location = useLocation()

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 flex flex-col glass border-r border-white/5 z-40">
      {/* Logo */}
      <div className="p-6 border-b border-white/5">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl flex items-center justify-center text-lg"
               style={{ background: 'var(--gradient-primary)' }}>
            ⚡
          </div>
          <div>
            <h1 className="font-bold text-white text-sm">PR Reviewer</h1>
            <p className="text-xs text-gray-500">AI Code Review</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map(item => {
          const active = location.pathname === item.path
          return (
            <Link
              key={item.path}
              to={item.path}
              id={`nav-${item.label.toLowerCase()}`}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                active
                  ? 'bg-brand-600/30 text-brand-300 border border-brand-500/30'
                  : 'text-gray-400 hover:text-white hover:bg-white/5'
              }`}
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </Link>
          )
        })}
      </nav>

      {/* User profile */}
      <div className="p-4 border-t border-white/5">
        <div className="flex items-center gap-3 mb-3">
          <img
            src={`https://github.com/${user?.username}.png?size=32`}
            alt={user?.username}
            className="w-8 h-8 rounded-full ring-2 ring-brand-500/40"
          />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">@{user?.username}</p>
            <p className="text-xs text-gray-500 truncate">{user?.email || 'GitHub User'}</p>
          </div>
        </div>
        <button
          id="btn-logout"
          onClick={logout}
          className="w-full btn-secondary text-xs py-2"
        >
          Sign out
        </button>
      </div>
    </aside>
  )
}
