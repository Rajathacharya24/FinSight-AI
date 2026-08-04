import { useEffect, useState, useCallback } from 'react';
import { notifications as notificationsApi } from '../api/client.js';

// Decode JWT payload to extract user ID (sub claim) without external libraries
function getUserIdFromToken() {
  try {
    const token = localStorage.getItem('finsight.token');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || payload.userId || payload.id || null;
  } catch {
    return null;
  }
}

function BellIcon({ unread }) {
  return (
    <div className="relative inline-flex">
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round"
          d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
      </svg>
      {unread > 0 && (
        <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-danger-500 text-[9px] font-bold text-white">
          {unread > 9 ? '9+' : unread}
        </span>
      )}
    </div>
  );
}

function NotificationItem({ notification, onMarkRead }) {
  const isUnread = !notification.readAt;
  const date = new Date(notification.createdAt).toLocaleString();

  const channelColors = {
    EMAIL:   'bg-brand-500/15 text-brand-300',
    WEBHOOK: 'bg-warning-500/15 text-warning-300',
    IN_APP:  'bg-success-500/15 text-success-300',
  };
  const channelCls = channelColors[notification.channel] || 'bg-white/8 text-slate-400';

  return (
    <li
      className={`flex items-start gap-3 p-4 rounded-xl border transition-all duration-200 ${
        isUnread
          ? 'bg-brand-500/5 border-brand-500/20'
          : 'bg-white/3 border-white/8'
      }`}
    >
      {/* Unread dot */}
      <div className={`mt-1.5 w-2 h-2 rounded-full shrink-0 ${isUnread ? 'bg-brand-400' : 'bg-transparent'}`} />

      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap mb-0.5">
          <span className="text-sm font-semibold text-white truncate">{notification.title || 'Notification'}</span>
          <span className={`text-[10px] font-semibold px-1.5 py-0.5 rounded-full uppercase tracking-wide ${channelCls}`}>
            {notification.channel}
          </span>
          {notification.workflowId && (
            <span className="text-[10px] text-slate-500 font-mono">#{notification.workflowId}</span>
          )}
        </div>
        <p className="text-xs text-slate-400 line-clamp-2">{notification.message}</p>
        <span className="text-[10px] text-slate-600 mt-1 block">{date}</span>
      </div>

      {isUnread && (
        <button
          onClick={() => onMarkRead(notification.id)}
          className="shrink-0 text-[10px] text-brand-400 hover:text-brand-200 font-medium transition-colors px-2 py-1 rounded hover:bg-brand-500/10"
        >
          Mark read
        </button>
      )}
    </li>
  );
}

export default function Notifications() {
  const [userId, setUserId] = useState(null);

  const [items, setItems]       = useState([]);
  const [unread, setUnread]     = useState(0);
  const [page, setPage]         = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');

  // Derive userId from JWT on mount
  useEffect(() => {
    const uid = getUserIdFromToken();
    setUserId(uid);
  }, []);

  const load = useCallback(async (p = 0) => {
    if (!userId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const res = await notificationsApi.getByUser(userId, p, 20);
      const data = res.data;
      setItems(data.content || (Array.isArray(data) ? data : []));
      setUnread(data.unreadCount || 0);
      setTotalPages(data.totalPages || 1);
      setPage(data.page || 0);
    } catch (e) {
      // 404 or empty is fine — notifications may not exist yet
      if (e?.response?.status === 404) {
        setItems([]);
        setUnread(0);
      } else {
        setError(e?.response?.data?.message || e.message);
      }
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => { load(0); }, [load]);

  const handleMarkRead = async (id) => {
    await notificationsApi.markRead(id);
    setItems(prev => prev.map(n => n.id === id ? { ...n, readAt: new Date().toISOString() } : n));
    setUnread(prev => Math.max(0, prev - 1));
  };

  const handleMarkAllRead = async () => {
    if (!userId) return;
    await notificationsApi.markAllRead(userId);
    setItems(prev => prev.map(n => ({ ...n, readAt: n.readAt || new Date().toISOString() })));
    setUnread(0);
  };

  return (
    <div className="space-y-5 max-w-3xl animate-slide-up">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-3">
          <BellIcon unread={unread} />
          <h1 className="section-title">Notifications</h1>
          {unread > 0 && (
            <span className="status-badge bg-danger-500/15 text-danger-300 border border-danger-500/25">
              {unread} unread
            </span>
          )}
        </div>
        {unread > 0 && (
          <button
            onClick={handleMarkAllRead}
            className="text-xs text-brand-400 hover:text-brand-200 font-medium transition-colors px-3 py-1.5 rounded-lg bg-brand-500/10 hover:bg-brand-500/20"
          >
            Mark all as read
          </button>
        )}
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex items-center justify-center h-48 gap-3 text-slate-400">
          <svg className="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          Loading notifications…
        </div>
      ) : error ? (
        <div className="text-danger-300 text-sm p-4 bg-danger-500/10 rounded-xl border border-danger-500/25">{error}</div>
      ) : items.length === 0 ? (
        <div className="glass-card-solid p-12 flex flex-col items-center gap-3 text-center">
          <BellIcon unread={0} />
          <p className="text-slate-400 text-sm">No notifications yet</p>
          <p className="text-slate-600 text-xs">Notifications will appear here when workflows complete or need attention.</p>
        </div>
      ) : (
        <div className="glass-card-solid p-4 space-y-2">
          <ul className="space-y-2">
            {items.map(n => (
              <NotificationItem key={n.id} notification={n} onMarkRead={handleMarkRead} />
            ))}
          </ul>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-3 border-t border-white/8">
              <button
                disabled={page === 0}
                onClick={() => load(page - 1)}
                className="px-3 py-1.5 text-xs rounded-lg bg-white/8 hover:bg-white/12 disabled:opacity-30 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                ← Prev
              </button>
              <span className="text-xs text-slate-500">Page {page + 1} of {totalPages}</span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => load(page + 1)}
                className="px-3 py-1.5 text-xs rounded-lg bg-white/8 hover:bg-white/12 disabled:opacity-30 disabled:cursor-not-allowed text-slate-300 transition-colors"
              >
                Next →
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
