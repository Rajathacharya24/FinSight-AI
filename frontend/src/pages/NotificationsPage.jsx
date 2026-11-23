import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Bell, Mail, CheckCircle2, AlertCircle, Eye, ExternalLink, X, Loader2 } from 'lucide-react';

export const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [selectedNotif, setSelectedNotif] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/notifications')
      .then((res) => setNotifications(res.data || []))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="eyebrow mb-1.5 flex items-center gap-1.5">
            <Bell className="h-3.5 w-3.5 text-brand-400" /> Communications
          </p>
          <h1 className="section-title">Email Notification Center</h1>
          <p className="section-subtitle">
            Audit log of customer decision emails dispatched via the Mailpit SMTP server.
          </p>
        </div>
        <a
          href="http://localhost:8025"
          target="_blank"
          rel="noopener noreferrer"
          className="btn-accent self-start sm:self-auto"
        >
          <span>Open Mailpit Web UI</span>
          <ExternalLink className="h-3.5 w-3.5" />
        </a>
      </div>

      {/* Table */}
      <div className="table-shell">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="table-head">
              <tr>
                <th className="p-4 font-semibold">Recipient</th>
                <th className="p-4 font-semibold">Subject</th>
                <th className="p-4 font-semibold">Workflow ID</th>
                <th className="p-4 font-semibold">Status</th>
                <th className="p-4 font-semibold">Sent At</th>
                <th className="p-4 text-right font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04] text-slate-300">
              {loading ? (
                <tr>
                  <td colSpan="6" className="p-10 text-center text-slate-500">
                    <Loader2 className="mx-auto mb-2 h-5 w-5 animate-spin text-brand-400" />
                    Loading notification history…
                  </td>
                </tr>
              ) : notifications.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-10 text-center text-slate-500">
                    No notifications dispatched yet. Process a workflow to trigger emails.
                  </td>
                </tr>
              ) : (
                notifications.map((notif) => (
                  <tr key={notif.id} className="trow">
                    <td className="p-4">
                      <div className="flex items-center gap-2.5">
                        <div className="icon-tile h-8 w-8 shrink-0 border-brand-400/20 bg-brand-500/10 text-brand-300">
                          <Mail className="h-4 w-4" />
                        </div>
                        <span className="font-semibold text-white">{notif.recipientEmail}</span>
                      </div>
                    </td>
                    <td className="p-4 text-slate-200">{notif.subject}</td>
                    <td className="p-4 font-mono text-[11px] text-slate-400">
                      {notif.workflowId.substring(0, 10)}…
                    </td>
                    <td className="p-4">
                      <span
                        className={`chip ${
                          notif.status === 'SENT'
                            ? 'border-success-500/25 bg-success-500/10 text-success-400'
                            : 'border-danger-500/25 bg-danger-500/10 text-danger-300'
                        }`}
                      >
                        {notif.status === 'SENT' ? (
                          <CheckCircle2 className="h-3 w-3" />
                        ) : (
                          <AlertCircle className="h-3 w-3" />
                        )}
                        {notif.status}
                      </span>
                    </td>
                    <td className="p-4 text-slate-400">{new Date(notif.sentAt).toLocaleString()}</td>
                    <td className="p-4">
                      <div className="flex justify-end">
                        <button
                          onClick={() => setSelectedNotif(notif)}
                          className="btn-ghost px-3 py-1.5 text-[11px]"
                        >
                          <Eye className="h-3.5 w-3.5" />
                          <span>Preview HTML</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Preview modal */}
      {selectedNotif && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">
          <div className="flex max-h-[80vh] w-full max-w-xl flex-col overflow-hidden rounded-2xl border border-white/10 bg-dark-850 shadow-glass">
            <div className="flex items-center justify-between border-b border-white/[0.08] p-4">
              <div>
                <h3 className="text-sm font-bold text-white">Email preview</h3>
                <p className="text-[11px] text-slate-400">To: {selectedNotif.recipientEmail}</p>
              </div>
              <button onClick={() => setSelectedNotif(null)} className="btn-icon">
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="overflow-y-auto bg-white p-6">
              <div dangerouslySetInnerHTML={{ __html: selectedNotif.message }} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
