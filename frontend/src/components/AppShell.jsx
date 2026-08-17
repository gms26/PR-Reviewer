import Sidebar from './Sidebar.jsx'
import { motion } from 'framer-motion'

/**
 * Application shell — wraps all authenticated pages with the sidebar layout.
 */
export default function AppShell({ children }) {
  return (
    <div className="flex min-h-screen relative overflow-hidden bg-transparent">
      
      {/* Subtle Background Elements if needed, though body has paper texture */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] rounded-full bg-brand-500/5 blur-[120px] mix-blend-multiply"></div>
        <div className="absolute top-[20%] -right-[10%] w-[40%] h-[40%] rounded-full bg-sage-500/5 blur-[120px] mix-blend-multiply"></div>
      </div>

      <Sidebar />
      <main className="flex-1 ml-72 min-h-screen relative z-10">
        <motion.div 
          className="max-w-6xl mx-auto px-8 py-10"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -20 }}
          transition={{ duration: 0.5, ease: "easeOut" }}
        >
          {children}
        </motion.div>
      </main>
    </div>
  )
}
