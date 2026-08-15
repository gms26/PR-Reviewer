/**
 * Shared spinner component.
 * @param {boolean} fullScreen - centers the spinner on the full viewport
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const wrapper = fullScreen
    ? 'min-h-screen flex flex-col items-center justify-center bg-charcoal-900/80 backdrop-blur-sm relative z-50'
    : 'flex flex-col items-center justify-center py-12'

  return (
    <div className={wrapper}>
      <div className="relative w-16 h-16 mb-4">
        {/* Geometric Spinner */}
        <div className="absolute inset-0 border-4 border-charcoal-700 shadow-solid-sm shadow-black"></div>
        <div className="absolute inset-0 border-4 border-t-acid-500 border-r-brand-500 border-b-burnt-500 border-l-transparent animate-spin shadow-solid-sm shadow-black" style={{ animationDuration: '1.5s' }}></div>
      </div>
      <div className="font-mono text-xs uppercase tracking-widest text-gray-500 animate-pulse">
        Processing...
      </div>
    </div>
  )
}

