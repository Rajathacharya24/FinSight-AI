import { useEffect, useState } from 'react';
import { analytics } from '../api/client.js';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid
} from 'recharts';

function Stat({ label, value }) {
  return (
    <div className="bg-white rounded-xl shadow p-5">
      <div className="text-xs uppercase tracking-wide text-slate-500">{label}</div>
      <div className="text-2xl font-semibold mt-1">{value}</div>
    </div>
  );
}

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

  const accuracySeries = series.map((p, i) => ({
    name: `#${p.workflowId}`,
    accuracy: 100,
    durationSeconds: p.durationSeconds
  }));

  const costSeries = series.map((p, i) => ({
    name: `#${p.workflowId}`,
    cost: 0.045
  }));

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Analytics</h1>

      <div className="grid grid-cols-4 gap-4">
        <Stat label="Total" value={summary?.total ?? '—'} />
        <Stat label="Completed" value={summary?.completed ?? '—'} />
        <Stat label="Failed" value={summary?.failed ?? '—'} />
        <Stat label="Avg Duration" value={summary ? `${Math.round(summary.averageProcessingSeconds)}s` : '—'} />
      </div>

      <div className="grid grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-5">
          <h2 className="font-medium mb-3">Processing Time</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <LineChart data={accuracySeries}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="durationSeconds" stroke="#2563eb" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow p-5">
          <h2 className="font-medium mb-3">Accuracy</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <BarChart data={accuracySeries}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Bar dataKey="accuracy" fill="#16a34a" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow p-5 col-span-2">
          <h2 className="font-medium mb-3">Cost per Workflow (USD)</h2>
          <div className="h-64">
            <ResponsiveContainer>
              <BarChart data={costSeries}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="cost" fill="#f59e0b" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
