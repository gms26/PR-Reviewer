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
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden">

      {/* Background glow effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-96 h-96 rounded-full opacity-20 blur-3xl"
           style={{ background: 'var(--gradient-primary)' }} />
      <div className="absolute bottom-1/4 right-1/3 w-64 h-64 bg-purple-600 rounded-full opacity-10 blur-3xl" />

      {/* Login card */}
      <div className="relative z-10 w-full max-w-md px-6 animate-slide-up">
        <div className="glass-card text-center">

          {/* Logo */}
          <div className="flex justify-center mb-6">
            <div className="w-16 h-16 rounded-2xl flex items-center justify-center text-3xl"
                 style={{ background: 'var(--gradient-primary)', boxShadow: '0 8px 32px rgba(69,80,231,0.4)' }}>
              ⚡
            </div>
          </div>

          <h1 className="text-2xl font-bold text-white mb-2">Welcome to PR Reviewer</h1>
          <p className="text-gray-400 text-sm mb-8 leading-relaxed">
            AI-powered code review for your GitHub Pull Requests.<br />
            Powered by Google Gemini.
          </p>

          {/* GitHub OAuth button */}
          <button
            id="btn-github-login"
            onClick={handleGitHubLogin}
            className="w-full flex items-center justify-center gap-3 px-6 py-3.5 rounded-xl font-semibold text-white text-sm transition-all duration-200 hover:-translate-y-0.5"
            style={{ background: '#24292e', border: '1px solid rgba(255,255,255,0.1)' }}
          >
            {/* GitHub SVG icon */}
            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
            </svg>
            Continue with GitHub
          </button>

          <p className="mt-6 text-xs text-gray-600">
            By signing in, you agree to let PR Reviewer read your repositories
            and post review comments on your behalf.
          </p>
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-gray-700 mt-4">
          PR Reviewer — AI Code Review Platform
        </p>
      </div>
    </div>
  )
}
