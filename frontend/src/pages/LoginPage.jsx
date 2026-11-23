import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Building2,
  Lock,
  User,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
  Sparkles,
  ScanText,
  GitMerge,
  BarChart3,
} from 'lucide-react';

const HIGHLIGHTS = [
  { icon: ScanText, title: 'AI document extraction', text: 'PDFBox + Groq LLM turn loan PDFs into structured data.' },
  { icon: GitMerge, title: 'Compliance workflows', text: 'Deterministic rules trigger approvals or manual review.' },
  { icon: BarChart3, title: 'Real-time analytics', text: 'Track approvals, velocity and confidence as they happen.' },
];

export const LoginPage = () => {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('demo');
  const [password, setPassword] = useState('demo123');
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    const result = await login(username, password);
    if (result.success) {
      navigate('/documents');
    } else {
      setError(result.message);
    }
  };

  const fillDemo = () => {
    setUsername('demo');
    setPassword('demo123');
  };

  return (
    <div className="relative min-h-screen overflow-hidden text-slate-200">
      <div className="absolute inset-0 -z-10 animated-bg" />
      <div className="pointer-events-none absolute inset-0 -z-10 app-grid opacity-60" />
      <div className="glow-dot -left-20 top-1/4 h-72 w-72 bg-brand-600/25" />
      <div className="glow-dot -right-10 bottom-1/4 h-72 w-72 bg-accent-500/15" />

      <div className="mx-auto flex min-h-screen w-full max-w-6xl flex-col items-stretch justify-center gap-10 p-6 lg:flex-row lg:items-center lg:gap-16 lg:p-10">
        {/* ── Brand panel ── */}
        <div className="hidden flex-1 lg:block">
          <div className="mb-8 inline-flex items-center gap-3">
            <div className="relative flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-brand shadow-glow-brand">
              <Building2 className="h-6 w-6 text-white" />
              <span className="absolute inset-0 rounded-2xl bg-glass-sheen" />
            </div>
            <div>
              <h1 className="text-2xl font-extrabold tracking-tight text-white">
                FinSight <span className="text-gradient">AI</span>
              </h1>
              <p className="text-[10px] font-semibold uppercase tracking-[0.22em] text-slate-500">
                Enterprise Intelligence
              </p>
            </div>
          </div>

          <div className="max-w-md">
            <span className="chip border-brand-400/30 bg-brand-500/10 text-brand-200">
              <Sparkles className="h-3 w-3" /> AI-native loan operations
            </span>
            <h2 className="mt-5 text-4xl font-extrabold leading-[1.1] tracking-tight text-white">
              Underwrite faster with
              <span className="block text-gradient">intelligent automation.</span>
            </h2>
            <p className="mt-4 text-sm leading-relaxed text-slate-400">
              Upload financial documents, extract every field with AI, validate compliance, and
              route decisions — all inside one secure control plane.
            </p>
          </div>

          <div className="mt-10 grid gap-3">
            {HIGHLIGHTS.map(({ icon: Icon, title, text }) => (
              <div
                key={title}
                className="flex items-start gap-3 rounded-xl border border-white/[0.06] bg-white/[0.025] p-3.5 transition-colors hover:border-white/[0.12] hover:bg-white/[0.04]"
              >
                <div className="icon-tile h-9 w-9 shrink-0 text-brand-300">
                  <Icon className="h-4 w-4" />
                </div>
                <div>
                  <p className="text-xs font-semibold text-white">{title}</p>
                  <p className="mt-0.5 text-[11px] leading-relaxed text-slate-400">{text}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* ── Auth card ── */}
        <div className="w-full max-w-md lg:flex-1">
          {/* Compact brand for mobile */}
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-brand shadow-glow-brand">
              <Building2 className="h-5 w-5 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-white">
                FinSight <span className="text-gradient">AI</span>
              </h1>
              <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500">
                Enterprise Intelligence
              </p>
            </div>
          </div>

          <div className="relative overflow-hidden rounded-3xl border border-white/[0.08] bg-dark-850/80 p-7 shadow-glass backdrop-blur-xl md:p-8">
            <div className="pointer-events-none absolute -right-16 -top-16 h-40 w-40 rounded-full bg-brand-500/20 blur-3xl" />

            <div className="relative">
              <h2 className="text-xl font-bold tracking-tight text-white">Welcome back</h2>
              <p className="mt-1 text-xs text-slate-400">
                Sign in to access the document intelligence workspace.
              </p>

              {error && (
                <div className="mt-5 rounded-xl border border-danger-500/30 bg-danger-500/10 px-3.5 py-2.5 text-xs text-danger-300">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="mt-6 space-y-4">
                <div>
                  <label className="mb-1.5 block text-[11px] font-semibold text-slate-300">
                    Username
                  </label>
                  <div className="relative">
                    <User className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
                    <input
                      type="text"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      className="form-input pl-10"
                      placeholder="Enter username"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="mb-1.5 block text-[11px] font-semibold text-slate-300">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="form-input pl-10"
                      placeholder="Enter password"
                      required
                    />
                  </div>
                </div>

                <button type="submit" disabled={loading} className="btn-primary w-full">
                  <span>{loading ? 'Authenticating…' : 'Sign in'}</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </form>

              <div className="mt-6 rounded-xl border border-white/[0.07] bg-dark-950/60 p-3">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="flex items-center gap-1.5 text-[11px] font-semibold text-success-400">
                      <CheckCircle2 className="h-3.5 w-3.5" /> Demo account ready
                    </p>
                    <p className="mt-0.5 text-[11px] text-slate-400">
                      <span className="font-mono text-slate-200">demo</span>
                      <span className="mx-1 text-slate-600">/</span>
                      <span className="font-mono text-slate-200">demo123</span>
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={fillDemo}
                    className="rounded-lg border border-brand-400/30 bg-brand-500/15 px-2.5 py-1 text-[11px] font-semibold text-brand-200 transition-colors hover:bg-brand-500/25 hover:text-white"
                  >
                    Auto fill
                  </button>
                </div>
              </div>
            </div>
          </div>

          <p className="mt-5 flex items-center justify-center gap-1.5 text-[11px] text-slate-500">
            <ShieldCheck className="h-3.5 w-3.5 text-success-500" />
            JWT authenticated · Redis rate-limited · Audit logged
          </p>
        </div>
      </div>
    </div>
  );
};
