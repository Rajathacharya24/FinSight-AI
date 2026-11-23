import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FileText,
  GitMerge,
  Bot,
  BarChart3,
  Bell,
  LogOut,
  ShieldCheck,
  Building2,
  Sparkles,
} from 'lucide-react';

const NAV_ITEMS = [
  { label: 'Documents', path: '/documents', icon: FileText },
  { label: 'Workflows', path: '/workflows', icon: GitMerge },
  { label: 'AI RAG Assistant', path: '/rag', icon: Bot },
  { label: 'Analytics', path: '/analytics', icon: BarChart3 },
  { label: 'Notifications', path: '/notifications', icon: Bell },
];

const BrandMark = () => (
  <div className="flex items-center gap-3">
    <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-brand shadow-glow-brand">
      <Building2 className="h-5 w-5 text-white" />
      <span className="absolute inset-0 rounded-xl bg-glass-sheen" />
    </div>
    <div className="leading-tight">
      <h1 className="text-base font-bold tracking-tight text-white">
        FinSight <span className="text-gradient">AI</span>
      </h1>
      <p className="text-[10px] font-medium uppercase tracking-[0.16em] text-slate-500">
        Enterprise Intelligence
      </p>
    </div>
  </div>
);

const NavLinks = ({ location, variant = 'sidebar' }) => (
  <nav className={variant === 'sidebar' ? 'space-y-1' : 'flex items-center gap-1 overflow-x-auto'}>
    {NAV_ITEMS.map((item) => {
      const Icon = item.icon;
      const active = location.pathname === item.path;
      return (
        <Link
          key={item.path}
          to={item.path}
          className={
            variant === 'sidebar'
              ? `nav-link ${active ? 'active' : ''}`
              : `flex shrink-0 items-center gap-2 rounded-xl px-3 py-2 text-xs font-semibold transition-colors ${
                  active
                    ? 'bg-gradient-brand-soft text-white'
                    : 'text-slate-400 hover:bg-white/5 hover:text-white'
                }`
          }
        >
          <Icon className={`h-4 w-4 shrink-0 ${active ? 'text-brand-300' : 'text-slate-500'}`} />
          <span className="whitespace-nowrap">{item.label}</span>
        </Link>
      );
    })}
  </nav>
);

export const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="relative min-h-screen text-slate-200">
      {/* Ambient background */}
      <div className="pointer-events-none fixed inset-0 -z-10 app-bg" />
      <div className="pointer-events-none fixed inset-x-0 top-0 -z-10 h-[520px] app-grid" />
      <div className="glow-dot -left-24 top-24 h-64 w-64 bg-brand-600/20" />

      <div className="flex min-h-screen">
        {/* ── Desktop sidebar ── */}
        <aside className="sticky top-0 hidden h-screen w-64 shrink-0 flex-col justify-between border-r border-white/[0.06] bg-dark-900/70 p-4 backdrop-blur-xl md:flex">
          <div>
            <div className="mb-8 rounded-2xl border border-white/[0.06] bg-white/[0.02] px-3 py-4">
              <BrandMark />
            </div>

            <p className="eyebrow mb-2 px-3">Workspace</p>
            <NavLinks location={location} />
          </div>

          <div className="space-y-3">
            <div className="rounded-xl border border-brand-500/20 bg-gradient-brand-soft p-3">
              <p className="flex items-center gap-1.5 text-[11px] font-semibold text-brand-200">
                <Sparkles className="h-3.5 w-3.5" /> AI Pipeline Online
              </p>
              <p className="mt-1 text-[10px] leading-relaxed text-slate-400">
                Groq extraction, compliance rules & pgvector retrieval active.
              </p>
            </div>

            <div className="flex items-center justify-between rounded-xl border border-white/[0.06] bg-white/[0.03] p-2.5">
              <div className="flex min-w-0 items-center gap-3">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-brand-400/30 bg-brand-500/20 text-sm font-bold text-brand-200">
                  {user?.username ? user.username.charAt(0).toUpperCase() : 'D'}
                </div>
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-white">
                    {user?.username || 'demo'}
                  </p>
                  <p className="flex items-center gap-1 text-[10px] text-slate-400">
                    <ShieldCheck className="h-3 w-3 text-success-400" />
                    {user?.roles?.[0] || 'USER'}
                  </p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                title="Sign out"
                className="rounded-lg p-2 text-slate-500 transition-colors hover:bg-danger-500/10 hover:text-danger-400"
              >
                <LogOut className="h-4 w-4" />
              </button>
            </div>
          </div>
        </aside>

        {/* ── Main column ── */}
        <div className="flex min-w-0 flex-1 flex-col">
          {/* Mobile header */}
          <header className="sticky top-0 z-30 border-b border-white/[0.06] bg-dark-900/80 backdrop-blur-xl md:hidden">
            <div className="flex items-center justify-between px-4 py-3">
              <BrandMark />
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full border border-brand-400/30 bg-brand-500/20 text-xs font-bold text-brand-200">
                  {user?.username ? user.username.charAt(0).toUpperCase() : 'D'}
                </div>
                <button
                  onClick={handleLogout}
                  title="Sign out"
                  className="rounded-lg p-2 text-slate-500 transition-colors hover:bg-danger-500/10 hover:text-danger-400"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            </div>
            <div className="border-t border-white/[0.05] px-3 py-2">
              <NavLinks location={location} variant="mobile" />
            </div>
          </header>

          <main className="flex-1 p-5 md:p-8 lg:p-10">
            <div className="mx-auto w-full max-w-7xl">{children}</div>
          </main>
        </div>
      </div>
    </div>
  );
};
