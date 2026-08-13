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
    <div className="min-h-screen flex items-center justify-center relative bg-black overflow-hidden">

      {/* Clean Grid Background (Vercel-like subtle texture) */}
      <div className="absolute inset-0 z-0 opacity-[0.03]" 
           style={{ backgroundImage: 'linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)', backgroundSize: '32px 32px' }} />

      {/* Login card */}
      <div className="relative z-10 w-full max-w-sm px-6 animate-slide-up">
        <div className="bg-[#0a0a0a] rounded-2xl p-8 text-center border border-[#222]">

          {/* Logo */}
          <div className="flex justify-center mb-6">
            <div className="w-12 h-12 rounded-xl flex items-center justify-center text-xl bg-[#111] border border-[#333]">
              ⚡
            </div>
          </div>

          <h1 className="text-xl font-semibold text-white mb-2 tracking-tight">Welcome to PR Reviewer</h1>
          <p className="text-gray-400 text-sm mb-8 leading-relaxed">
            AI-powered code review for your GitHub Pull Requests.
          </p>

          {/* GitHub OAuth button */}
          <button
            id="btn-github-login"
            onClick={handleGitHubLogin}
            className="w-full flex items-center justify-center gap-3 px-5 py-2.5 rounded-lg font-medium text-white text-sm transition-colors hover:bg-[#222]"
            style={{ background: '#111', border: '1px solid #333' }}
          >
            {/* GitHub SVG icon */}
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
            </svg>
            Continue with GitHub
          </button>

          <p className="mt-5 text-[11px] text-gray-500">
            By signing in, you agree to let PR Reviewer read your repositories
            and post review comments on your behalf.
          </p>
        </div>

        {/* Footer */}
        <p className="text-center text-[11px] text-[#444] mt-6">
          PR Reviewer — AI Code Review Platform
        </p>
      </div>
    </div>
  )
}
