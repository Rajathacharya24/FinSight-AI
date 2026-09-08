import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import client from '../api/client';
import { 
  GitMerge, 
  CheckCircle2, 
  XCircle, 
  AlertTriangle, 
  Clock, 
  ShieldCheck, 
  FileText, 
  User, 
  DollarSign, 
  Activity,
  UserCheck
} from 'lucide-react';

export const WorkflowPage = () => {
  const [searchParams] = useSearchParams();
  const docIdFilter = searchParams.get('docId');

  const [workflows, setWorkflows] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Review Modal State
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewAction, setReviewAction] = useState('APPROVE');
  const [reviewReason, setReviewReason] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  const fetchWorkflows = async () => {
    try {
      setLoading(true);
      const res = await client.get('/workflow/instances');
      const list = res.data || [];
      setWorkflows(list);

      if (list.length > 0) {
        let current = list[0];
        if (docIdFilter) {
          const found = list.find(w => w.documentId === docIdFilter);
          if (found) current = found;
        }
        setSelectedWorkflow(current);
        fetchAuditLogs(current.id);
      }
    } catch (err) {
      console.error('Failed to fetch workflows', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchAuditLogs = async (workflowId) => {
    try {
      const res = await client.get(`/workflow/instances/${workflowId}/audit`);
      setAuditLogs(res.data || []);
    } catch (err) {
      console.error('Failed to fetch audit logs', err);
    }
  };

  useEffect(() => {
    fetchWorkflows();
  }, [docIdFilter]);

  const selectWorkflowItem = (wf) => {
    setSelectedWorkflow(wf);
    fetchAuditLogs(wf.id);
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!selectedWorkflow) return;

    setSubmittingReview(true);
    try {
      await client.post(`/workflow/instances/${selectedWorkflow.id}/review`, {
        action: reviewAction,
        reason: reviewReason
      });
      setReviewModalOpen(false);
      setReviewReason('');
      fetchWorkflows();
    } catch (err) {
      alert('Failed to submit manual review.');
    } finally {
      setSubmittingReview(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return (
          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" /> Approved
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-rose-500/10 text-rose-400 border border-rose-500/30">
            <XCircle className="w-3.5 h-3.5 mr-1.5" /> Rejected
          </span>
        );
      case 'NEEDS_REVIEW':
        return (
          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-amber-500/10 text-amber-400 border border-amber-500/30 animate-pulse">
            <AlertTriangle className="w-3.5 h-3.5 mr-1.5" /> Underwriter Review
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">
            <Clock className="w-3.5 h-3.5 mr-1.5" /> Processing...
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <GitMerge className="w-7 h-7 text-indigo-400" />
          Multi-Agent Workflow Tracker
        </h1>
        <p className="text-xs text-slate-400 mt-1">Real-time asynchronous compliance decision engine</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Workflow List */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-4 flex flex-col h-[700px]">
          <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Workflow Instances</h2>
          <div className="overflow-y-auto space-y-2 flex-1 pr-1">
            {loading ? (
              <p className="text-xs text-slate-500 p-4 text-center">Loading workflows...</p>
            ) : workflows.length === 0 ? (
              <p className="text-xs text-slate-500 p-4 text-center">No active workflows found.</p>
            ) : (
              workflows.map((wf) => (
                <div
                  key={wf.id}
                  onClick={() => selectWorkflowItem(wf)}
                  className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
                    selectedWorkflow?.id === wf.id
                      ? 'bg-indigo-600/15 border-indigo-500/50 shadow-md shadow-indigo-500/10'
                      : 'bg-slate-950/60 border-slate-800/80 hover:bg-slate-850'
                  }`}
                >
                  <div className="flex justify-between items-start mb-1.5">
                    <p className="text-sm font-semibold text-white truncate max-w-[150px]">{wf.borrowerName || 'Applicant'}</p>
                    {getStatusBadge(wf.status)}
                  </div>
                  <p className="text-xs text-slate-400 font-mono">Doc ID: {wf.documentId.substring(0, 10)}...</p>
                  <div className="mt-2 text-[11px] text-slate-500 flex justify-between">
                    <span>${wf.loanAmount?.toLocaleString()}</span>
                    <span>{new Date(wf.startedAt).toLocaleTimeString()}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right Column: Workflow Stage Visualization & Audit Log */}
        {selectedWorkflow ? (
          <div className="lg:col-span-2 space-y-6">
            {/* Active Workflow Card */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl">
              <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4 pb-4 border-b border-slate-800">
                <div>
                  <h3 className="text-lg font-bold text-white">{selectedWorkflow.borrowerName || 'Applicant'}</h3>
                  <p className="text-xs text-slate-400">Workflow ID: <span className="font-mono text-slate-300">{selectedWorkflow.id}</span></p>
                </div>
                <div className="flex items-center space-x-3">
                  {getStatusBadge(selectedWorkflow.status)}
                  {selectedWorkflow.status === 'NEEDS_REVIEW' && (
                    <button
                      onClick={() => setReviewModalOpen(true)}
                      className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-lg shadow-md transition-colors flex items-center gap-1"
                    >
                      <UserCheck className="w-3.5 h-3.5" /> Manual Override
                    </button>
                  )}
                </div>
              </div>

              {/* Workflow Pipeline Progression Bar */}
              <div className="my-6">
                <p className="text-xs font-semibold text-slate-400 mb-4">Pipeline Execution Stage</p>
                <div className="grid grid-cols-5 gap-2 text-center text-xs">
                  <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl">
                    <p className="font-bold">✓ Uploaded</p>
                    <p className="text-[10px] text-slate-400 mt-1">MinIO + PDFBox</p>
                  </div>
                  <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl">
                    <p className="font-bold">✓ AI Extraction</p>
                    <p className="text-[10px] text-slate-400 mt-1">Groq LLM DTO</p>
                  </div>
                  <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl">
                    <p className="font-bold">✓ Compliance</p>
                    <p className="text-[10px] text-slate-400 mt-1">DTI & Rules</p>
                  </div>
                  <div className={`p-3 rounded-xl border ${
                    selectedWorkflow.status === 'APPROVED' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' :
                    selectedWorkflow.status === 'REJECTED' ? 'bg-rose-500/10 border-rose-500/30 text-rose-400' : 'bg-amber-500/10 border-amber-500/30 text-amber-400'
                  }`}>
                    <p className="font-bold">Decision</p>
                    <p className="text-[10px] text-slate-400 mt-1">{selectedWorkflow.status}</p>
                  </div>
                  <div className="p-3 bg-indigo-500/10 border border-indigo-500/30 text-indigo-400 rounded-xl">
                    <p className="font-bold">✓ Notification</p>
                    <p className="text-[10px] text-slate-400 mt-1">Mailpit SMTP</p>
                  </div>
                </div>
              </div>

              {/* Rationale Details */}
              <div className="p-4 bg-slate-950/80 border border-slate-800 rounded-xl">
                <p className="text-xs font-semibold text-slate-400 mb-1">Decision Rationale</p>
                <p className="text-xs text-slate-200">{selectedWorkflow.decisionReason || 'Processing decision...'}</p>
              </div>
            </div>

            {/* Audit Trail Component */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl">
              <h3 className="text-sm font-bold text-white mb-4 flex items-center gap-2">
                <Activity className="w-4 h-4 text-indigo-400" /> Audit Log Timeline
              </h3>
              <div className="space-y-4 relative before:absolute before:inset-0 before:left-3.5 before:w-0.5 before:bg-slate-800">
                {auditLogs.map((log) => (
                  <div key={log.id} className="relative pl-8 flex items-start justify-between text-xs">
                    <div className="absolute left-1.5 top-1 w-4 h-4 rounded-full bg-indigo-600 border-2 border-slate-900" />
                    <div>
                      <p className="font-bold text-slate-200">{log.action}</p>
                      <p className="text-slate-400 mt-0.5">{log.description}</p>
                      <p className="text-[10px] text-indigo-400 mt-1">Agent: {log.performedBy}</p>
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0">{new Date(log.timestamp).toLocaleTimeString()}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="lg:col-span-2 bg-slate-900/40 border border-slate-800 rounded-2xl p-8 text-center text-slate-500 flex items-center justify-center">
            Select a workflow to view its pipeline execution and audit log.
          </div>
        )}
      </div>

      {/* Underwriter Manual Override Modal */}
      {reviewModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl space-y-4">
            <h3 className="text-base font-bold text-white">Manual Underwriter Decision</h3>
            <form onSubmit={handleReviewSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Decision</label>
                <select
                  value={reviewAction}
                  onChange={(e) => setReviewAction(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-white"
                >
                  <option value="APPROVE">Override & Approve</option>
                  <option value="REJECT">Override & Reject</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Underwriter Reason</label>
                <textarea
                  value={reviewReason}
                  onChange={(e) => setReviewReason(e.target.value)}
                  placeholder="Provide audit reason for manual override..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-white h-24"
                  required
                />
              </div>
              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setReviewModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 text-xs font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingReview}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl"
                >
                  Submit Decision
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
