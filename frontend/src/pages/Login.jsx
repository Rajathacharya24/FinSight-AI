import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../api/client.js';
import { useAuth } from '../hooks/useAuth.js';

export default function Login() {
  const [email, setEmail]       = useState('demo@finsight.ai');
  const [password, setPassword] = useState('demo');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);
  const { login }               = useAuth();
  const navigate                = useNavigate();

  async function handle(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await auth.login(email, password);
      const token = data.token || data.accessToken;
      if (!token) throw new Error('No token in response');
      login(token);
      navigate('/upload');
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center animated-bg relative overflow-hidden">
      {/* Decorative glow blobs */}
      <div className="glow-dot w-96 h-96 bg-brand-700/25 top-[-100px] left-[-60px]" />
      <div className="glow-dot w-80 h-80 bg-navy-600/20 bottom-[-80px] right-[-60px]" />
      <div className="glow-dot w-64 h-64 bg-brand-500/10 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" />

      <div className="relative z-10 w-full max-w-sm mx-4 animate-slide-up">
        {/* Card */}
        <div className="glass-card-solid p-8 space-y-6 shadow-[0_0_80px_rgba(124,58,237,0.2)]">

          {/* Header */}
          <div className="flex flex-col items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-gradient-brand flex items-center justify-center shadow-glow-brand animate-pulse-brand">
              <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div className="text-center">
              <h1 className="text-xl font-bold text-white">FinSight AI</h1>
              <p className="text-sm text-slate-400 mt-0.5">Sign in to your console</p>
            </div>
          </div>

          {/* Form */}
          <form id="login-form" onSubmit={handle} className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs font-medium text-slate-400 uppercase tracking-wider">Email</label>
              <input
                id="input-email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                type="email"
                required
                autoComplete="email"
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-medium text-slate-400 uppercase tracking-wider">Password</label>
              <input
                id="input-password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                type="password"
                required
                autoComplete="current-password"
              />
            </div>

            {error && (
              <div className="flex items-center gap-2 bg-danger-500/10 border border-danger-500/30 rounded-lg px-3 py-2 text-sm text-danger-400 animate-fade-in">
                <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
                {error}
              </div>
            )}

            <button
              id="btn-login"
              type="submit"
              disabled={loading}
              className="btn-primary w-full mt-2"
            >
              {loading ? (
                <>
                  <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Signing in…
                </>
              ) : 'Continue →'}
            </button>
          </form>

          {/* Demo hint */}
          <p className="text-center text-xs text-slate-600">
            Dev credentials pre-filled · demo@finsight.ai / demo
          </p>
        </div>
      </div>
    </div>
  );
}
