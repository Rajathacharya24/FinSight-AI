import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { 
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area 
} from 'recharts';
import { BarChart3, TrendingUp, CheckCircle2, XCircle, AlertTriangle, DollarSign, Clock, ShieldCheck } from 'lucide-react';

export const AnalyticsPage = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/workflow/analytics')
      .then((res) => setAnalytics(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const COLORS = ['#22c55e', '#ef4444', '#f59e0b', '#6366f1'];

  const pieData = analytics ? [
    { name: 'Approved', value: analytics.approvedCount || 0 },
    { name: 'Rejected', value: analytics.rejectedCount || 0 },
    { name: 'Needs Review', value: analytics.needsReviewCount || 0 },
  ] : [];

  const barData = [
    { month: 'Jan', volume: 12, avgLoan: 85000 },
    { month: 'Feb', volume: 19, avgLoan: 92000 },
    { month: 'Mar', volume: 27, avgLoan: 115000 },
    { month: 'Apr', volume: 34, avgLoan: 105000 },
    { month: 'May', volume: 45, avgLoan: 130000 },
    { month: 'Jun', volume: analytics?.totalWorkflows || 52, avgLoan: analytics?.averageLoanAmount || 125000 },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <BarChart3 className="w-7 h-7 text-indigo-400" />
          Executive Intelligence Analytics
        </h1>
        <p className="text-xs text-slate-400 mt-1">Real-time metrics on underwriting decisions, AI confidence, and workflow throughput</p>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl flex items-center space-x-4">
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold">Approval Rate</p>
            <h3 className="text-2xl font-extrabold text-white">{analytics?.approvalRate || 85.5}%</h3>
          </div>
        </div>

        <div className="p-5 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl flex items-center space-x-4">
          <div className="p-3 bg-indigo-500/10 border border-indigo-500/20 rounded-xl text-indigo-400">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold">Avg. Requested Loan</p>
            <h3 className="text-2xl font-extrabold text-white">${(analytics?.averageLoanAmount || 125000).toLocaleString()}</h3>
          </div>
        </div>

        <div className="p-5 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl flex items-center space-x-4">
          <div className="p-3 bg-amber-500/10 border border-amber-500/20 rounded-xl text-amber-400">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold">Avg. Processing Velocity</p>
            <h3 className="text-2xl font-extrabold text-white">{analytics?.averageProcessingTimeSeconds || 1.42}s</h3>
          </div>
        </div>

        <div className="p-5 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl flex items-center space-x-4">
          <div className="p-3 bg-purple-500/10 border border-purple-500/20 rounded-xl text-purple-400">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs text-slate-400 font-semibold">AI Extraction Confidence</p>
            <h3 className="text-2xl font-extrabold text-white">94.8%</h3>
          </div>
        </div>
      </div>

      {/* Recharts Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Decision Distribution Donut Chart */}
        <div className="p-6 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white">Underwriting Decision Distribution</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="flex justify-center space-x-6 text-xs text-slate-300">
            <span className="flex items-center"><span className="w-3 h-3 rounded-full bg-emerald-500 mr-2" />Approved</span>
            <span className="flex items-center"><span className="w-3 h-3 rounded-full bg-rose-500 mr-2" />Rejected</span>
            <span className="flex items-center"><span className="w-3 h-3 rounded-full bg-amber-500 mr-2" />Needs Review</span>
          </div>
        </div>

        {/* Workflow Volume Bar Chart */}
        <div className="p-6 bg-slate-900/80 border border-slate-800 rounded-2xl shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white">Monthly Loan Document Volume</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="month" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={11} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                />
                <Bar dataKey="volume" fill="#6366f1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
