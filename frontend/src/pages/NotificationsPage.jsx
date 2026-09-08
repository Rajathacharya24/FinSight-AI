import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Bell, Mail, CheckCircle2, AlertCircle, Eye, ExternalLink } from 'lucide-react';

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
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Bell className="w-7 h-7 text-indigo-400" />
            Email Notification Center
          </h1>
          <p className="text-xs text-slate-400 mt-1">Audit log of customer decision emails dispatched via Mailpit SMTP server</p>
        </div>
        <a
          href="http://localhost:8025"
          target="_blank"
          rel="noopener noreferrer"
          className="px-3.5 py-2 bg-indigo-600/20 hover:bg-indigo-600/40 text-indigo-300 border border-indigo-500/30 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-colors"
        >
          <span>Open Mailpit Web UI</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>

      {/* Notifications Table */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 border-b border-slate-800 uppercase font-semibold">
              <tr>
                <th className="p-4">Recipient</th>
                <th className="p-4">Subject</th>
                <th className="p-4">Workflow ID</th>
                <th className="p-4">Status</th>
                <th className="p-4">Sent At</th>
                <th className="p-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {loading ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-slate-500">Loading notification history...</td>
                </tr>
              ) : notifications.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-slate-500">No notifications dispatched yet. Process a workflow to trigger emails.</td>
                </tr>
              ) : (
                notifications.map((notif) => (
                  <tr key={notif.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="p-4 font-semibold text-white flex items-center space-x-2">
                      <Mail className="w-4 h-4 text-indigo-400" />
                      <span>{notif.recipientEmail}</span>
                    </td>
                    <td className="p-4 text-slate-200">{notif.subject}</td>
                    <td className="p-4 font-mono text-[11px] text-slate-400">{notif.workflowId.substring(0, 10)}...</td>
                    <td className="p-4">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-bold ${
                        notif.status === 'SENT' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}>
                        {notif.status}
                      </span>
                    </td>
                    <td className="p-4 text-slate-400">{new Date(notif.sentAt).toLocaleString()}</td>
                    <td className="p-4 text-right">
                      <button
                        onClick={() => setSelectedNotif(notif)}
                        className="px-3 py-1.5 bg-slate-800 hover:bg-slate-750 text-slate-300 text-xs font-medium rounded-lg transition-colors inline-flex items-center space-x-1"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Preview HTML</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* HTML Email Preview Modal */}
      {selectedNotif && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl max-h-[80vh] flex flex-col shadow-2xl">
            <div className="p-4 border-b border-slate-800 flex justify-between items-center">
              <div>
                <h3 className="text-sm font-bold text-white">Email Preview</h3>
                <p className="text-[11px] text-slate-400">To: {selectedNotif.recipientEmail}</p>
              </div>
              <button
                onClick={() => setSelectedNotif(null)}
                className="text-slate-400 hover:text-white text-xs px-2 py-1 bg-slate-800 rounded-lg"
              >
                Close
              </button>
            </div>
            <div className="p-6 bg-white overflow-y-auto rounded-b-2xl">
              <div dangerouslySetInnerHTML={{ __html: selectedNotif.message }} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
