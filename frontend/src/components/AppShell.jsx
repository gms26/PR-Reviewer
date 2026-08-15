import Sidebar from './Sidebar.jsx'

/**
 * Application shell — wraps all authenticated pages with the sidebar layout.
 */
export default function AppShell({ children }) {
  return (
    <div className="flex min-h-screen relative overflow-hidden">
      <Sidebar />
      <main className="flex-1 ml-72 min-h-screen relative z-10">
        <div className="max-w-6xl mx-auto px-8 py-10 animate-fade-in">
          {children}
        </div>
      </main>
    </div>
  )
}

