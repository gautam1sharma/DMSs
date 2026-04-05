import React, { useEffect, useState } from 'react';
import { Building2, Users, TrendingUp, DollarSign, BarChart3, Car, ShoppingCart, MessageSquare } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { dashboardApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar
} from 'recharts';

interface Stats {
  totalDealers?: number;
  activeDealers?: number;
  totalUsers?: number;
  totalRevenue?: number;
  totalCustomers?: number;
  totalVehicles?: number;
  totalOrders?: number;
  pendingOrders?: number;
  completedOrders?: number;
  openInquiries?: number;
  dealerRevenue?: number;
}

// Mock sparkline data
const revenueData = [
  { name: 'Jan', value: 420000 }, { name: 'Feb', value: 680000 },
  { name: 'Mar', value: 520000 }, { name: 'Apr', value: 890000 },
  { name: 'May', value: 760000 }, { name: 'Jun', value: 1100000 },
  { name: 'Jul', value: 950000 },
];
const ordersData = [
  { name: 'Mon', orders: 12 }, { name: 'Tue', orders: 19 },
  { name: 'Wed', orders: 8  }, { name: 'Thu', orders: 25 },
  { name: 'Fri', orders: 31 }, { name: 'Sat', orders: 14 },
  { name: 'Sun', orders: 7  },
];

const fmt = (n?: number) => {
  if (!n) return '0';
  if (n >= 10000000) return `₹${(n / 10000000).toFixed(1)}Cr`;
  if (n >= 100000)   return `₹${(n / 100000).toFixed(1)}L`;
  return n.toLocaleString('en-IN');
};

export default function DashboardPage() {
  const { isAdmin, user } = useAuth();
  const [stats, setStats] = useState<Stats>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        if (isAdmin) {
          const res = await dashboardApi.getAdminStats();
          setStats(res.data);
        } else if (user?.dealerId) {
          const res = await dashboardApi.getDealerStats(user.dealerId);
          setStats(res.data);
        }
      } catch {
        toast.error('Failed to load dashboard stats');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [isAdmin, user]);

  const adminKpis = [
    { label: 'Total Dealers',   value: stats.totalDealers ?? 0,  icon: Building2,   color: '#818cf8', suffix: '' },
    { label: 'Active Dealers',  value: stats.activeDealers ?? 0, icon: Building2,   color: '#22c55e', suffix: '' },
    { label: 'Total Users',     value: stats.totalUsers ?? 0,    icon: Users,        color: '#38bdf8', suffix: '' },
    { label: 'Total Revenue',   value: stats.totalRevenue ?? 0,  icon: DollarSign,   color: '#f59e0b', suffix: '', format: fmt },
  ];

  const dealerKpis = [
    { label: 'Customers',       value: stats.totalCustomers ?? 0,  icon: Users,          color: '#818cf8' },
    { label: 'Available Cars',  value: stats.totalVehicles ?? 0,   icon: Car,            color: '#22c55e' },
    { label: 'Total Orders',    value: stats.totalOrders ?? 0,     icon: ShoppingCart,   color: '#38bdf8' },
    { label: 'Open Inquiries',  value: stats.openInquiries ?? 0,   icon: MessageSquare,  color: '#f59e0b' },
    { label: 'Revenue Earned',  value: stats.dealerRevenue ?? 0,   icon: TrendingUp,     color: '#a855f7', format: fmt },
  ];

  const kpis = isAdmin ? adminKpis : dealerKpis;

  return (
    <AppLayout title="Dashboard">
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Good {new Date().getHours() < 12 ? 'Morning' : new Date().getHours() < 17 ? 'Afternoon' : 'Evening'}, {user?.firstName} 👋
          </h1>
          <p className="page-subtitle">
            {isAdmin ? 'Here\'s what\'s happening across all dealers today.' : 'Here\'s your dealership overview.'}
          </p>
        </div>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner spinner-lg" /></div>
      ) : (
        <>
          {/* KPI Cards */}
          <div className="kpi-grid">
            {kpis.map((kpi) => {
              const Icon = kpi.icon;
              const display = kpi.format ? kpi.format(kpi.value) : kpi.value.toLocaleString();
              return (
                <div key={kpi.label} className="kpi-card" style={{ '--kpi-color': kpi.color } as React.CSSProperties}>
                  <div className="kpi-icon" style={{ background: `${kpi.color}18`, color: kpi.color }}>
                    <Icon size={20} />
                  </div>
                  <div className="kpi-value">{display}</div>
                  <div className="kpi-label">{kpi.label}</div>
                </div>
              );
            })}
          </div>

          {/* Charts */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
            <div className="card chart-card">
              <div className="section-title" style={{ marginBottom: 20 }}>
                <TrendingUp size={17} /> Revenue Trend
              </div>
              <ResponsiveContainer width="100%" height={220}>
                <AreaChart data={revenueData}>
                  <defs>
                    <linearGradient id="revGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#6366f1" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0}   />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                  <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false}
                    tickFormatter={(v) => `₹${(v/100000).toFixed(0)}L`} />
                  <Tooltip
                    contentStyle={{ background: '#141628', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 8, fontSize: 12 }}
                    labelStyle={{ color: '#94a3b8' }}
                    itemStyle={{ color: '#818cf8' }}
                    formatter={(v: any) => [`₹${(v/100000).toFixed(1)}L`, 'Revenue']}
                  />
                  <Area type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2} fill="url(#revGrad)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>

            <div className="card chart-card">
              <div className="section-title" style={{ marginBottom: 20 }}>
                <ShoppingCart size={17} /> Weekly Orders
              </div>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={ordersData} barSize={24}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                  <XAxis dataKey="name" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{ background: '#141628', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 8, fontSize: 12 }}
                    labelStyle={{ color: '#94a3b8' }}
                    cursor={{ fill: 'rgba(255,255,255,0.03)' }}
                  />
                  <Bar dataKey="orders" fill="#6366f1" radius={[4,4,0,0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Order status summary (dealer) */}
          {!isAdmin && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16, marginTop: 20 }}>
              {[
                { label: 'Pending Orders',   val: stats.pendingOrders ?? 0,   color: '#f59e0b' },
                { label: 'Completed Orders', val: stats.completedOrders ?? 0, color: '#22c55e' },
                { label: 'Open Inquiries',   val: stats.openInquiries ?? 0,   color: '#818cf8' },
              ].map(item => (
                <div key={item.label} className="card" style={{ padding: 20 }}>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: item.color }}>{item.val}</div>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: 4 }}>{item.label}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </AppLayout>
  );
}
