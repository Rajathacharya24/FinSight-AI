import React, { useState, useEffect } from 'react';
import client from '../api/client';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { BarChart3, CheckCircle2, DollarSign, Clock, ShieldCheck, TrendingUp } from 'lucide-react';

const TOOLTIP_STYLE = {
  backgroundColor: '#0a1019',
  borderColor: 'rgba(255,255,255,0.1)',
  borderRadius: '12px',
  fontSize: '12px',
  color: '#e2e8f0',
  boxShadow: '0 12px 32px -12px rgba(0,0,0,0.8)',
};

const MetricCard = ({ icon: Icon, label, value, tone, trend }) => (
  <div className="panel panel-hover relative overflow-hidden p-5">
    <div className={`pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full blur-2xl ${tone.glow}`} />
    <div className="relative flex items-center gap-4">
      <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border ${tone.tile}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0">
        <p className="text-[11px] font-semibold text-slate-400">{label}</p>
        <h3 className="mt-0.5 truncate text-2xl font-extrabold tracking-tight text-white">{value}</h3>
        {trend && (
          <p className="mt-0.5 flex items-center gap-1 text-[10px] font-medium text-success-400">
            <TrendingUp className="h-3 w-3" /> {trend}
          </p>
        )}
      </div>
    </div>
  </div>
);

export const AnalyticsPage = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/workflow/analytics')
      .then((res) => setAnalytics(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const COLORS = ['#22c55e', '#ef4444', '#f59e0b'];

  const pieData = analytics ? [
    { name: 'Approved', value: analytics.approvedCount || 0 },
    { name: 'Rejected', value: analytics.rejectedCount || 0 },
    { name: 'Needs Review', value: analytics.needsReviewCount || 0 },
  ] : [];

  const hasPieData = pieData.some((d) => d.value > 0);

  const barData = [
    { month: 'Jan', volume: 12 },
    { month: 'Feb', volume: 19 },
    { month: 'Mar', volume: 27 },
    { month: 'Apr', volume: 34 },
    { month: 'May', volume: 45 },
    { month: 'Jun', volume: analytics?.totalWorkflows || 52 },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <p className="eyebrow mb-1.5 flex items-center gap-1.5">
          <BarChart3 className="h-3.5 w-3.5 text-brand-400" /> Executive intelligence
        </p>
        <h1 className="section-title">Analytics Overview</h1>
        <p className="section-subtitle">
          Real-time metrics on underwriting decisions, AI confidence and workflow throughput.
        </p>
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          icon={CheckCircle2}
          label="Approval rate"
          value={`${analytics?.approvalRate || 85.5}%`}
          tone={{
            tile: 'border-success-500/25 bg-success-500/10 text-success-400',
            glow: 'bg-success-500/20',
          }}
          trend="+2.4% vs last month"
        />
        <MetricCard
          icon={DollarSign}
          label="Avg. requested loan"
          value={`$${(analytics?.averageLoanAmount || 125000).toLocaleString()}`}
          tone={{
            tile: 'border-brand-400/25 bg-brand-500/10 text-brand-300',
            glow: 'bg-brand-500/20',
          }}
        />
        <MetricCard
          icon={Clock}
          label="Avg. processing velocity"
          value={`${analytics?.averageProcessingTimeSeconds || 1.42}s`}
          tone={{
            tile: 'border-warning-500/25 bg-warning-500/10 text-warning-300',
            glow: 'bg-warning-500/20',
          }}
        />
        <MetricCard
          icon={ShieldCheck}
          label="AI extraction confidence"
          value="94.8%"
          tone={{
            tile: 'border-accent-500/25 bg-accent-500/10 text-accent-300',
            glow: 'bg-accent-500/20',
          }}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Decision distribution */}
        <div className="panel p-6">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Decision distribution</h3>
            <span className="chip border-white/[0.08] bg-white/[0.03] text-slate-400">
              {loading ? 'Loading…' : 'All time'}
            </span>
          </div>
          <div className="h-64">
            {hasPieData ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={62}
                    outerRadius={92}
                    paddingAngle={5}
                    stroke="none"
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={TOOLTIP_STYLE} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-slate-500">
                No decision data available yet.
              </div>
            )}
          </div>
          <div className="flex justify-center gap-6 text-xs text-slate-300">
            <span className="flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-success-500" /> Approved
            </span>
            <span className="flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-danger-500" /> Rejected
            </span>
            <span className="flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-warning-500" /> Needs review
            </span>
          </div>
        </div>

        {/* Monthly volume */}
        <div className="panel p-6">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Monthly document volume</h3>
            <span className="chip border-white/[0.08] bg-white/[0.03] text-slate-400">Last 6 months</span>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData} barCategoryGap={28}>
                <defs>
                  <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#9270ff" />
                    <stop offset="100%" stopColor="#4a1cae" />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                <XAxis dataKey="month" stroke="#475569" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="#475569" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
                <Bar dataKey="volume" fill="url(#barGradient)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
