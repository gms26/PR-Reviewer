import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.jsx'
import { motion } from 'framer-motion'
import { Server, FolderGit2, GitPullRequestDraft, MessageSquare, Rocket } from 'lucide-react'

const containerVariants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.1 }
  }
}

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0, transition: { type: "spring", stiffness: 300, damping: 24 } }
}

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
      <motion.div 
        className="mb-12 flex flex-col md:flex-row md:items-end justify-between gap-6 pb-6 border-b border-subtle"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: "easeOut" }}
      >
        <div>
          <h1 className="text-5xl lg:text-7xl font-display font-bold text-ink-900 mb-2 tracking-tighter leading-none">
            {greeting},<br/>
            <span className="text-brand-600 italic font-display">
              @{user?.username}
            </span> 
          </h1>
        </div>
        <div className="glass-panel px-5 py-3 flex items-center gap-4 bg-white">
          <div className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-sage-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-sage-500"></span>
          </div>
          <div className="font-mono text-xs text-sage-600 uppercase tracking-widest font-bold">
            System Online<br/>
            <span className="text-ink-600 font-normal">AI Processing Active</span>
          </div>
        </div>
      </motion.div>

      {/* Stats grid */}
      <motion.div 
        className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12"
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        {/* Repositories - Large Feature Box */}
        <motion.div variants={itemVariants} className="glass-card md:col-span-2 row-span-2 flex flex-col justify-between group overflow-hidden bg-paper-800">
          <div className="flex justify-between items-start mb-12 relative z-10">
            <div className="w-16 h-16 flex items-center justify-center bg-ink-900 text-white rounded-sm shadow-btn">
              <FolderGit2 size={32} />
            </div>
            <span className="badge badge-success">Active Sync</span>
          </div>
          <div className="relative z-10">
            <div className="text-7xl lg:text-9xl font-display font-bold text-ink-900 mb-2 tracking-tighter leading-none transition-colors">—</div>
            <div className="text-xl font-bold uppercase tracking-widest text-ink-600">Repositories</div>
          </div>
        </motion.div>

        {/* Pull Requests */}
        <motion.div variants={itemVariants} className="glass-card flex flex-col justify-between group overflow-hidden bg-white">
          <div className="flex justify-between items-start mb-8 relative z-10">
            <div className="w-12 h-12 flex items-center justify-center bg-brand-600 text-white rounded-sm shadow-btn">
              <GitPullRequestDraft size={24} />
            </div>
          </div>
          <div className="relative z-10">
            <div className="text-5xl font-display font-bold text-ink-900 mb-1 tracking-tighter transition-colors">—</div>
            <div className="text-sm font-bold uppercase tracking-widest text-ink-600">Pull Requests</div>
          </div>
        </motion.div>

        {/* Reviews */}
        <motion.div variants={itemVariants} className="glass-card flex flex-col justify-between group overflow-hidden bg-white">
          <div className="flex justify-between items-start mb-8 relative z-10">
            <div className="w-12 h-12 flex items-center justify-center bg-sage-600 text-white rounded-sm shadow-btn">
              <MessageSquare size={24} />
            </div>
          </div>
          <div className="relative z-10">
            <div className="text-5xl font-display font-bold text-ink-900 mb-1 tracking-tighter transition-colors">—</div>
            <div className="text-sm font-bold uppercase tracking-widest text-ink-600">Reviews</div>
          </div>
        </motion.div>

      </motion.div>

      {/* Getting started card */}
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4, duration: 0.5 }}
        className="glass-panel p-8 md:p-10 border border-subtle overflow-hidden bg-white"
      >
        <div className="relative z-10 flex flex-col md:flex-row gap-12 items-start md:items-center">
          <div className="flex-1">
            <h2 className="text-3xl font-display font-bold text-ink-900 mb-8 tracking-tight flex items-center gap-4">
              <div className="w-12 h-12 bg-paper-800 flex items-center justify-center border border-subtle rounded-sm">
                <Rocket className="text-brand-600" size={24} />
              </div>
              Initialization Guide
            </h2>
            <ol className="space-y-6 font-mono text-sm text-ink-700">
              {[
                'Connect GitHub account (Authorized ✅)',
                'Select target repository for AI injection',
                'Open a Pull Request — System engages',
                'Inline code reviews appear automatically',
              ].map((step, i) => (
                <li key={i} className="flex items-center gap-5 group">
                  <span className="w-10 h-10 rounded-sm bg-white border border-subtle text-ink-900 font-bold flex items-center justify-center flex-shrink-0 transition-all shadow-btn group-hover:border-brand-400 group-hover:text-brand-600">
                    0{i + 1}
                  </span>
                  <span className="group-hover:text-ink-900 transition-colors text-base">{step}</span>
                </li>
              ))}
            </ol>
          </div>
          
          <div className="hidden md:flex w-64 h-64 relative items-center justify-center">
            {/* Elegant decorative element instead of neon rings */}
            <div className="absolute inset-0 border border-subtle rounded-sm"></div>
            <div className="absolute inset-4 border border-subtle rounded-sm bg-paper-800"></div>
            <div className="absolute inset-12 border border-subtle rounded-sm bg-white flex items-center justify-center shadow-card">
              <Server size={48} className="text-brand-600 opacity-80" />
            </div>
          </div>
        </div>
      </motion.div>
    </AppShell>
  )
}
