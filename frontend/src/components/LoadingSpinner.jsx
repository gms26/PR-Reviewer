/**
 * Shared spinner component.
 * @param {boolean} fullScreen - centers the spinner on the full viewport
 */
export default function LoadingSpinner({ fullScreen = false }) {
  const wrapper = fullScreen
    ? 'min-h-screen flex items-center justify-center bg-gray-950'
    : 'flex items-center justify-center py-12'

  return (
    <div className={wrapper}>
      <div className="relative">
        {/* Outer ring */}
        <div className="w-12 h-12 rounded-full border-2 border-gray-800" />
        {/* Spinning arc */}
        <div className="absolute inset-0 w-12 h-12 rounded-full border-2 border-transparent border-t-brand-500 animate-spin" />
      </div>
    </div>
  )
}
