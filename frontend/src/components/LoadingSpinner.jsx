/**
 * Shared spinner component.
 * @param {boolean} fullScreen - centers the spinner on the full viewport
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const wrapper = fullScreen
    ? 'min-h-screen flex items-center justify-center bg-transparent backdrop-blur-md relative z-50'
    : 'flex items-center justify-center py-12'

  return (
    <div className={wrapper}>
      <div className="relative flex items-center justify-center">
        {/* Glowing background blob */}
        <div className="absolute inset-0 bg-brand-500/20 blur-xl rounded-full animate-pulse-slow"></div>
        
        {/* Creative SVG Spinner */}
        <svg className="w-16 h-16 animate-spin text-brand-500 relative z-10" viewBox="0 0 100 100" fill="none" stroke="currentColor" strokeWidth="4">
          <circle cx="50" cy="50" r="40" stroke="rgba(255,255,255,0.1)" strokeWidth="4" />
          <path d="M50 10 A 40 40 0 0 1 90 50" strokeLinecap="round" className="opacity-80" />
          <path d="M50 90 A 40 40 0 0 1 10 50" strokeLinecap="round" className="text-cyan-400 opacity-80" />
        </svg>
        
        {/* Inner pulsing dot */}
        <div className="absolute w-3 h-3 bg-white rounded-full animate-ping z-20"></div>
      </div>
    </div>
  )
}

