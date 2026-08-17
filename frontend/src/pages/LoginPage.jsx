/**
 * Login Page
 *
 * Single entry point for GitHub OAuth. The button redirects to the
 * Spring Boot OAuth2 authorization endpoint which handles the full
 * OAuth dance and session creation.
 */
export default function LoginPage() {
  const handleGitHubLogin = () => {
    // Redirect to backend OAuth2 initiation endpoint
    const backend =
      import.meta.env.MODE === "development"
        ? ""
        : (import.meta.env.VITE_API_BASE_URL || "");
    window.location.href = `${backend}/oauth2/authorization/github`;
  }

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-paper-800 relative overflow-hidden">
      
      {/* Decorative Grid Background - subtle */}
      <div className="absolute inset-0 z-0 opacity-10 pointer-events-none bg-[linear-gradient(rgba(0,0,0,0.05)_1px,transparent_1px),linear-gradient(90deg,rgba(0,0,0,0.05)_1px,transparent_1px)] [background-size:40px_40px] [background-position:center_center]"></div>

      {/* Left side: Typographic Hero */}
      <div className="flex-1 flex flex-col justify-center p-12 lg:p-24 relative z-10 animate-slide-up">
        <div className="inline-flex items-center gap-4 mb-8">
          <div className="w-16 h-16 rounded-sm flex items-center justify-center bg-ink-900 text-white shadow-btn">
            <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="square" strokeLinejoin="miter" strokeWidth={2.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div className="font-mono text-ink-900 text-sm tracking-widest uppercase font-bold">
            System<br/>Ready
          </div>
        </div>
        
        <h1 className="text-6xl md:text-8xl lg:text-9xl font-display font-bold text-ink-900 tracking-tighter leading-none mb-6">
          Automate<br/>
          <span className="text-brand-600 italic font-display">Reviews</span>
        </h1>
        <p className="text-xl md:text-2xl text-ink-600 max-w-2xl font-sans font-light leading-relaxed">
          The ultimate AI-powered code review companion. Ship code faster, with precise efficiency and deep insight.
        </p>

        {/* Decorative elements */}
        <div className="absolute bottom-12 left-12 flex gap-4">
           <div className="w-3 h-3 bg-brand-600 rounded-full"></div>
           <div className="w-3 h-3 bg-sage-600 rounded-full"></div>
           <div className="w-3 h-3 bg-terracotta-600 rounded-full"></div>
        </div>
      </div>

      {/* Right side: Login Panel */}
      <div className="w-full md:w-[480px] bg-white border-l border-subtle flex flex-col justify-center p-8 lg:p-12 relative z-10 shadow-[-10px_0_40px_rgba(0,0,0,0.03)]">
        
        <div className="glass-card animate-slide-up bg-paper-800" style={{ animationDelay: '200ms' }}>
          <h2 className="text-3xl font-display font-bold text-ink-900 mb-2 tracking-tight">Initialize</h2>
          <p className="text-ink-600 text-sm mb-8 font-mono">Authenticate via GitHub to access the console.</p>

          <button
            id="btn-github-login"
            onClick={handleGitHubLogin}
            className="btn-primary w-full shadow-btn bg-brand-600 hover:bg-brand-500 group"
          >
            <svg className="w-5 h-5 group-hover:-translate-y-0.5 transition-transform" fill="currentColor" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
            </svg>
            Continue with GitHub
          </button>

          <p className="mt-6 text-xs text-ink-500 font-mono">
            {"// By proceeding, you authorize PR Reviewer to read repositories and post comments."}
          </p>
        </div>

        <p className="absolute bottom-8 left-0 right-0 text-center text-xs text-ink-400 font-mono uppercase tracking-widest font-bold">
          PR Reviewer v2.0
        </p>
      </div>

    </div>
  )
}

