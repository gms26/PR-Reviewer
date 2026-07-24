import Sidebar from './Sidebar.jsx'

/**
 * Application shell — wraps all authenticated pages with the sidebar layout.
 */
export default function AppShell({ children }) {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="flex-1 ml-64 min-h-screen">
        <div className="max-w-6xl mx-auto px-8 py-8 animate-fade-in">
          {children}
        </div>
      </main>
    </div>
  )
}
