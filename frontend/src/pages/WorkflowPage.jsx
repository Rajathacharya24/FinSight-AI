import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import client from '../api/client';
import {
  GitMerge,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  Activity,
  UserCheck,
  Loader2,
  X,
  CircleDot,
} from 'lucide-react';

export const WorkflowPage = () => {
  const [searchParams] = useSearchParams();
  const docIdFilter = searchParams.get('docId');

  const [workflows, setWorkflows] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);

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
        reason: reviewReason,
      });
      setReviewModalOpen(false);
      setReviewReason('');
      fetchWorkflows();
    } catch (err) {
      console.error('Failed to submit manual review.', err);
    } finally {
      setSubmittingReview(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return (
          <span className="chip border-success-500/25 bg-success-500/10 text-success-400">
            <CheckCircle2 className="h-3 w-3" /> Approved
          </span>
        );
      case 'REJECTED':
        return (
          <span className="chip border-danger-500/25 bg-danger-500/10 text-danger-300">
            <XCircle className="h-3 w-3" /> Rejected
          </span>
        );
      case 'NEEDS_REVIEW':
        return (
          <span className="chip animate-pulse border-warning-500/25 bg-warning-500/10 text-warning-300">
            <AlertTriangle className="h-3 w-3" /> Review
          </span>
        );
      default:
        return (
          <span className="chip border-brand-400/25 bg-brand-500/10 text-brand-300">
            <Clock className="h-3 w-3" /> Processing
          </span>
        );
    }
  };

  const stages = selectedWorkflow
    ? [
        { label: 'Uploaded', sub: 'MinIO + PDFBox' },
        { label: 'AI Extraction', sub: 'Groq LLM DTO' },
        { label: 'Compliance', sub: 'DTI & Rules' },
        { label: 'Decision', sub: selectedWorkflow.status },
        { label: 'Notification', sub: 'Mailpit SMTP' },
      ]
    : [];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <p className="eyebrow mb-1.5 flex items-center gap-1.5">
          <GitMerge className="h-3.5 w-3.5 text-brand-400" /> Orchestration
        </p>
        <h1 className="section-title">Multi-Agent Workflow Tracker</h1>
        <p className="section-subtitle">Real-time asynchronous compliance decision engine.</p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Workflow list */}
        <div className="panel flex h-[700px] flex-col p-4">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="eyebrow">Workflow instances</h2>
            <span className="chip border-white/[0.08] bg-white/[0.03] text-slate-400">
              {workflows.length}
            </span>
          </div>
          <div className="flex-1 space-y-2 overflow-y-auto pr-1">
            {loading ? (
              <p className="flex items-center justify-center gap-2 p-6 text-xs text-slate-500">
                <Loader2 className="h-4 w-4 animate-spin text-brand-400" /> Loading workflows…
              </p>
            ) : workflows.length === 0 ? (
              <p className="p-6 text-center text-xs text-slate-500">No active workflows found.</p>
            ) : (
              workflows.map((wf) => {
                const active = selectedWorkflow?.id === wf.id;
                return (
                  <button
                    key={wf.id}
                    onClick={() => selectWorkflowItem(wf)}
                    className={`w-full rounded-xl border p-3.5 text-left transition-all duration-200 ${
                      active
                        ? 'border-brand-400/40 bg-gradient-brand-soft shadow-inset-line'
                        : 'border-white/[0.06] bg-dark-950/40 hover:border-white/[0.14] hover:bg-dark-900/60'
                    }`}
                  >
                    <div className="mb-1.5 flex items-start justify-between gap-2">
                      <p className="max-w-[150px] truncate text-sm font-semibold text-white">
                        {wf.borrowerName || 'Applicant'}
                      </p>
                      {getStatusBadge(wf.status)}
                    </div>
                    <p className="font-mono text-[11px] text-slate-500">
                      Doc ID: {wf.documentId.substring(0, 10)}…
                    </p>
                    <div className="mt-2 flex justify-between text-[11px] text-slate-500">
                      <span className="font-medium text-slate-300">
                        ${wf.loanAmount?.toLocaleString()}
                      </span>
                      <span>{new Date(wf.startedAt).toLocaleTimeString()}</span>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Detail column */}
        {selectedWorkflow ? (
          <div className="space-y-6 lg:col-span-2">
            <div className="panel p-6">
              <div className="flex flex-col justify-between gap-4 border-b border-white/[0.07] pb-4 sm:flex-row sm:items-center">
                <div>
                  <h3 className="text-lg font-bold text-white">
                    {selectedWorkflow.borrowerName || 'Applicant'}
                  </h3>
                  <p className="text-xs text-slate-400">
                    Workflow ID:{' '}
                    <span className="font-mono text-slate-300">{selectedWorkflow.id}</span>
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  {getStatusBadge(selectedWorkflow.status)}
                  {selectedWorkflow.status === 'NEEDS_REVIEW' && (
                    <button
                      onClick={() => setReviewModalOpen(true)}
                      className="btn-primary px-3.5 py-1.5 text-xs"
                    >
                      <UserCheck className="h-3.5 w-3.5" /> Manual override
                    </button>
                  )}
                </div>
              </div>

              {/* Pipeline */}
              <div className="my-6">
                <p className="eyebrow mb-4">Pipeline execution stage</p>
                <div className="grid grid-cols-2 gap-2 text-center text-xs sm:grid-cols-5">
                  {stages.map((stage, i) => {
                    const isDecision = i === 3;
                    const tone = !isDecision
                      ? 'border-success-500/25 bg-success-500/[0.08] text-success-400'
                      : selectedWorkflow.status === 'APPROVED'
                      ? 'border-success-500/25 bg-success-500/[0.08] text-success-400'
                      : selectedWorkflow.status === 'REJECTED'
                      ? 'border-danger-500/25 bg-danger-500/[0.08] text-danger-300'
                      : 'border-warning-500/25 bg-warning-500/[0.08] text-warning-300';
                    return (
                      <div key={stage.label} className={`rounded-xl border p-3 ${tone}`}>
                        <p className="font-bold">
                          {!isDecision && '✓ '}
                          {stage.label}
                        </p>
                        <p className="mt-1 text-[10px] text-slate-400">{stage.sub}</p>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="panel-inset p-4">
                <p className="eyebrow mb-1">Decision rationale</p>
                <p className="text-xs leading-relaxed text-slate-200">
                  {selectedWorkflow.decisionReason || 'Processing decision…'}
                </p>
              </div>
            </div>

            {/* Audit trail */}
            <div className="panel p-6">
              <h3 className="mb-4 flex items-center gap-2 text-sm font-bold text-white">
                <Activity className="h-4 w-4 text-brand-400" /> Audit log timeline
              </h3>
              {auditLogs.length === 0 ? (
                <p className="text-xs text-slate-500">No audit events recorded yet.</p>
              ) : (
                <div className="relative space-y-4 before:absolute before:inset-0 before:left-[7px] before:w-px before:bg-white/[0.08]">
                  {auditLogs.map((log) => (
                    <div key={log.id} className="relative flex items-start justify-between gap-3 pl-7 text-xs">
                      <span className="absolute left-0 top-1 flex h-4 w-4 items-center justify-center rounded-full border border-brand-400/40 bg-brand-500/20">
                        <CircleDot className="h-2.5 w-2.5 text-brand-300" />
                      </span>
                      <div>
                        <p className="font-bold text-slate-200">{log.action}</p>
                        <p className="mt-0.5 text-slate-400">{log.description}</p>
                        <p className="mt-1 text-[10px] text-brand-300">Agent: {log.performedBy}</p>
                      </div>
                      <span className="shrink-0 text-[10px] text-slate-500">
                        {new Date(log.timestamp).toLocaleTimeString()}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="panel flex items-center justify-center p-8 text-center text-sm text-slate-500 lg:col-span-2">
            Select a workflow to view its pipeline execution and audit log.
          </div>
        )}
      </div>

      {/* Review modal */}
      {reviewModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md space-y-4 rounded-2xl border border-white/10 bg-dark-850 p-6 shadow-glass">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white">Manual underwriter decision</h3>
              <button onClick={() => setReviewModalOpen(false)} className="btn-icon">
                <X className="h-4 w-4" />
              </button>
            </div>
            <form onSubmit={handleReviewSubmit} className="space-y-4">
              <div>
                <label className="mb-1 block text-[11px] font-semibold text-slate-300">Decision</label>
                <select
                  value={reviewAction}
                  onChange={(e) => setReviewAction(e.target.value)}
                  className="form-select"
                >
                  <option value="APPROVE">Override &amp; approve</option>
                  <option value="REJECT">Override &amp; reject</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-semibold text-slate-300">
                  Underwriter reason
                </label>
                <textarea
                  value={reviewReason}
                  onChange={(e) => setReviewReason(e.target.value)}
                  placeholder="Provide an audit reason for the manual override…"
                  className="form-input h-24 resize-none text-xs"
                  required
                />
              </div>
              <div className="flex justify-end gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setReviewModalOpen(false)}
                  className="btn-ghost px-4 py-2 text-xs"
                >
                  Cancel
                </button>
                <button type="submit" disabled={submittingReview} className="btn-primary px-4 py-2 text-xs">
                  {submittingReview ? 'Submitting…' : 'Submit decision'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
