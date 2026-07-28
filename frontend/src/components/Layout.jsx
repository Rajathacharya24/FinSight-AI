import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.js';

const NAV_LINKS = [
  {
    to: '/upload',
    label: 'Upload',
    icon: (
      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
      </svg>
    ),
  },
  {
    to: '/documents',
    label: 'Workflows',
    icon: (
      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
  },
  {
    to: '/analytics',
    label: 'Analytics',
    icon: (
      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
    ),
  },
];

export default function Layout() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex animated-bg">
      {/* Decorative glow blobs */}
      <div className="glow-dot w-96 h-96 bg-brand-700/20 top-[-100px] left-[-80px]" style={{ position: 'fixed' }} />
      <div className="glow-dot w-72 h-72 bg-navy-600/15 bottom-[-60px] right-[-40px]" style={{ position: 'fixed' }} />

      {/* ── Sidebar ── */}
      <aside className="relative z-10 w-64 flex flex-col flex-shrink-0 border-r border-white/8"
             style={{ background: 'rgba(9,14,22,0.85)', backdropFilter: 'blur(20px)' }}>

        {/* Logo */}
        <div className="px-5 py-6 border-b border-white/8">
          <div className="flex items-center gap-2.5">
            {/* Icon */}
            <div className="w-8 h-8 rounded-lg bg-gradient-brand flex items-center justify-center shadow-glow-brand">
              <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <div className="text-white font-bold text-base leading-none">FinSight AI</div>
              <div className="text-xs text-slate-500 mt-0.5 leading-none">Workflow Console</div>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-600 px-3 mb-2">Navigation</p>
          {NAV_LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              id={`nav-${link.label.toLowerCase()}`}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
            >
              {link.icon}
              {link.label}
            </NavLink>
          ))}
        </nav>

        {/* Bottom: status + sign out */}
        <div className="p-3 border-t border-white/8 space-y-2">
          {/* System status indicator */}
          <div className="flex items-center gap-2 px-3 py-2">
            <span className="w-2 h-2 rounded-full bg-success-400 animate-pulse" />
            <span className="text-xs text-slate-500">System Online</span>
          </div>
          <button
            id="btn-sign-out"
            onClick={() => { logout(); navigate('/login'); }}
            className="btn-ghost w-full text-slate-400"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Sign out
          </button>
        </div>
      </aside>

      {/* ── Main content ── */}
      <main className="relative z-10 flex-1 overflow-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
