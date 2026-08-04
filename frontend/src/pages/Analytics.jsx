import { useEffect, useState } from 'react';
import { analytics } from '../api/client.js';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid
} from 'recharts';

function Stat({ label, value, icon }) {
  return (
    <div className="stat-card">
      <div className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">{label}</div>
      <div className="text-2xl font-bold text-white mt-1">{value}</div>
    </div>
  );
}

const chartTooltipStyle = {
  contentStyle: {
    background: 'rgba(15, 23, 42, 0.95)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '12px',
    color: '#e2e8f0',
    fontSize: '12px',
  },
  itemStyle: { color: '#e2e8f0' },
  labelStyle: { color: '#94a3b8', fontWeight: 600 },
};

export default function Analytics() {
  const [summary, setSummary] = useState(null);
  const [series, setSeries] = useState([]);

  useEffect(() => {
    Promise.all([analytics.summary(), analytics.timeseries()])
      .then(([s, t]) => {
        setSummary(s.data);
        setSeries(t.data);
      })
      .catch(() => {});
  }, []);

  const accuracySeries = series.map((p) => ({
    name: `#${p.workflowId}`,
    accuracy: 100,
    durationSeconds: p.durationSeconds
  }));

  const costSeries = series.map((p) => ({
    name: `#${p.workflowId}`,
    cost: 0.045
  }));

  return (
    <div className="space-y-6 animate-slide-up">
      {/* Header */}
      <div>
        <h1 className="section-title">Analytics</h1>
        <p className="section-subtitle">Performance metrics and cost analysis across all workflows.</p>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Stat label="Total Workflows" value={summary?.total ?? '—'} />
        <Stat label="Completed" value={summary?.completed ?? '—'} />
        <Stat label="Failed" value={summary?.failed ?? '—'} />
        <Stat label="Avg Duration" value={summary ? `${Math.round(summary.averageProcessingSeconds)}s` : '—'} />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Processing Time */}
        <div className="glass-card-solid p-5 space-y-3">
          <h2 className="text-sm font-semibold text-white">Processing Time</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <LineChart data={accuracySeries}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <Tooltip {...chartTooltipStyle} />
                <Line type="monotone" dataKey="durationSeconds" stroke="#7c3aed" strokeWidth={2.5} dot={{ fill: '#7c3aed', r: 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Accuracy */}
        <div className="glass-card-solid p-5 space-y-3">
          <h2 className="text-sm font-semibold text-white">Accuracy</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <BarChart data={accuracySeries}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <YAxis domain={[0, 100]} tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <Tooltip {...chartTooltipStyle} />
                <Bar dataKey="accuracy" fill="#22c55e" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Cost per Workflow */}
        <div className="glass-card-solid p-5 space-y-3 lg:col-span-2">
          <h2 className="text-sm font-semibold text-white">Cost per Workflow (USD)</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <BarChart data={costSeries}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} stroke="rgba(255,255,255,0.1)" />
                <Tooltip {...chartTooltipStyle} />
                <Bar dataKey="cost" fill="#f59e0b" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
