import { motion } from 'framer-motion'
import { Code2 } from 'lucide-react'

/**
 * Shared spinner component with classic editorial design.
 * @param {boolean} fullScreen - centers the spinner on the full viewport
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const wrapper = fullScreen
    ? 'min-h-screen flex flex-col items-center justify-center bg-paper-800/90 backdrop-blur-md relative z-50 overflow-hidden'
    : 'flex flex-col items-center justify-center py-12 relative overflow-hidden'

  return (
    <div className={wrapper}>
      <div className="relative z-10 flex flex-col items-center">
        {/* Core Animated Spinner */}
        <div className="relative w-24 h-24 mb-8 flex items-center justify-center">
          {/* Outer Ring */}
          <motion.div
            className="absolute inset-0 rounded-full border-t-2 border-r-2 border-brand-600"
            animate={{ rotate: 360 }}
            transition={{ repeat: Infinity, duration: 2, ease: "linear" }}
          />
          {/* Middle Ring */}
          <motion.div
            className="absolute inset-2 rounded-full border-b-2 border-l-2 border-sage-500"
            animate={{ rotate: -360 }}
            transition={{ repeat: Infinity, duration: 3, ease: "linear" }}
          />
          {/* Inner Ring */}
          <motion.div
            className="absolute inset-4 rounded-full border-t-2 border-l-2 border-terracotta-500"
            animate={{ rotate: 360 }}
            transition={{ repeat: Infinity, duration: 1.5, ease: "linear" }}
          />
          
          {/* Center Icon */}
          <motion.div
            animate={{ scale: [0.9, 1.1, 0.9] }}
            transition={{ repeat: Infinity, duration: 2, ease: "easeInOut" }}
            className="text-ink-900"
          >
            <Code2 size={24} />
          </motion.div>
        </div>

        {/* Text */}
        <motion.div
          animate={{ opacity: [0.4, 1, 0.4] }}
          transition={{ repeat: Infinity, duration: 2, ease: "easeInOut" }}
          className="font-display font-bold text-sm uppercase tracking-[0.3em] text-ink-900"
        >
          Initializing...
        </motion.div>
      </div>
    </div>
  )
}
