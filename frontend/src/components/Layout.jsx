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
  Building2
} from 'lucide-react';

export const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const navItems = [
    { label: 'Documents', path: '/documents', icon: FileText },
    { label: 'Workflows', path: '/workflows', icon: GitMerge },
    { label: 'AI RAG Assistant', path: '/rag', icon: Bot },
    { label: 'Analytics', path: '/analytics', icon: BarChart3 },
    { label: 'Notifications', path: '/notifications', icon: Bell },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row">
      {/* Sidebar Navigation */}
      <aside className="w-full md:w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between p-4 shrink-0">
        <div>
          {/* Brand Logo */}
          <div className="flex items-center space-x-3 px-2 py-4 mb-6 border-b border-slate-800">
            <div className="p-2 bg-indigo-600 rounded-xl shadow-lg shadow-indigo-500/30">
              <Building2 className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-lg text-white tracking-wide">FinSight AI</h1>
              <p className="text-xs text-indigo-400 font-medium">Enterprise Intelligence</p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                    active
                      ? 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className={`w-5 h-5 ${active ? 'text-indigo-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Card */}
        <div className="pt-4 border-t border-slate-800">
          <div className="flex items-center justify-between p-2 rounded-lg bg-slate-850">
            <div className="flex items-center space-x-3 overflow-hidden">
              <div className="w-9 h-9 rounded-full bg-indigo-600/30 border border-indigo-500/40 flex items-center justify-center font-bold text-indigo-300">
                {user?.username ? user.username.charAt(0).toUpperCase() : 'D'}
              </div>
              <div className="truncate">
                <p className="text-sm font-semibold text-white truncate">{user?.username || 'demo'}</p>
                <p className="text-xs text-slate-400 flex items-center space-x-1">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 inline" />
                  <span>{user?.roles?.[0] || 'USER'}</span>
                </p>
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Sign Out"
              className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto p-6 md:p-8 bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
        {children}
      </main>
    </div>
  );
};
