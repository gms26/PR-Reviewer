import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, FolderGit2, GitPullRequestDraft, Settings, LogOut, Code2 } from 'lucide-react'
import { useAuth } from '../hooks/useAuth.jsx'
import clsx from 'clsx'
import { motion } from 'framer-motion'

const navItems = [
  { path: '/',             label: 'Dashboard',     icon: LayoutDashboard },
  { path: '/repositories', label: 'Repositories',  icon: FolderGit2 },
  { path: '/reviews',      label: 'Reviews',       icon: GitPullRequestDraft },
  { path: '/settings',     label: 'Settings',      icon: Settings },
]

/**
 * Main navigation sidebar.
 */
export default function Sidebar() {
  const { user, logout } = useAuth()
  const location = useLocation()

  return (
    <aside className="fixed left-6 top-6 bottom-6 w-64 flex flex-col glass-panel z-40 bg-white">
      {/* Logo */}
      <div className="p-6 relative border-b border-subtle">
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 flex items-center justify-center bg-ink-900 text-white shadow-btn rounded-sm">
            <Code2 size={20} />
          </div>
          <div>
            <h1 className="font-display font-bold text-ink-900 text-lg tracking-tight leading-none uppercase">PR Reviewer</h1>
            <p className="text-[10px] text-brand-500 font-bold uppercase tracking-widest mt-1">v2.0 Beta</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
        {navItems.map(item => {
          const active = location.pathname === item.path
          const Icon = item.icon
          return (
            <Link
              key={item.path}
              to={item.path}
              id={`nav-${item.label.toLowerCase()}`}
              className="relative group block"
            >
              {active && (
                <motion.div
                  layoutId="activeNavBackground"
                  className="absolute inset-0 bg-paper-800 rounded-sm border border-subtle"
                  initial={false}
                  transition={{ type: "spring", stiffness: 400, damping: 30 }}
                />
              )}
              <div className={clsx(
                "relative flex items-center gap-3 px-4 py-3 rounded-sm text-sm font-display font-bold uppercase tracking-wider transition-all duration-300 z-10",
                active 
                  ? "text-brand-600" 
                  : "text-ink-600 hover:text-ink-900 hover:bg-paper-900 border border-transparent"
              )}>
                <Icon size={18} className={clsx(
                  "transition-transform duration-300",
                  active ? "scale-110" : "group-hover:-translate-y-0.5"
                )} />
                <span>{item.label}</span>
              </div>
            </Link>
          )
        })}
      </nav>

      {/* User profile */}
      <div className="p-4 m-4 bg-paper-800 rounded-sm border border-subtle">
        <div className="flex items-center gap-3 mb-4">
          <div className="relative">
            <img
              src={`https://github.com/${user?.username}.png?size=32`}
              alt={user?.username}
              className="w-10 h-10 rounded-sm border border-subtle shadow-sm"
            />
            <div className="absolute -bottom-1 -right-1 w-3.5 h-3.5 bg-sage-500 border-2 border-white rounded-full"></div>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-display font-bold text-ink-900 truncate">@{user?.username}</p>
            <p className="text-xs text-ink-600 truncate font-mono">{user?.email || 'SYSTEM.OP'}</p>
          </div>
        </div>
        <button
          id="btn-logout"
          onClick={logout}
          className="w-full flex items-center justify-center gap-2 text-xs py-2 uppercase tracking-widest text-terracotta-600 hover:text-terracotta-500 hover:bg-terracotta-50 rounded-sm transition-colors border border-transparent hover:border-terracotta-400"
        >
          <LogOut size={14} />
          <span>Disconnect</span>
        </button>
      </div>
    </aside>
  )
}

