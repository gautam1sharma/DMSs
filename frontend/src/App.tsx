import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import LoginPage       from './pages/LoginPage';
import DashboardPage   from './pages/DashboardPage';
import DealersPage     from './pages/DealersPage';
import CustomersPage   from './pages/CustomersPage';
import VehiclesPage    from './pages/VehiclesPage';
import OrdersPage      from './pages/OrdersPage';
import InquiriesPage   from './pages/InquiriesPage';
import UsersPage       from './pages/UsersPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Admin routes */}
          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin/dashboard" element={<DashboardPage />} />
            <Route path="/admin/dealers"   element={<DealersPage />} />
            <Route path="/admin/users"     element={<UsersPage />} />
            <Route path="/admin/reports"   element={<DashboardPage />} />
            <Route path="/admin/settings"  element={<DashboardPage />} />
          </Route>

          {/* Dealer routes */}
          <Route element={<ProtectedRoute roles={['DEALER']} />}>
            <Route path="/dealer/dashboard"  element={<DashboardPage />} />
            <Route path="/dealer/customers"  element={<CustomersPage />} />
            <Route path="/dealer/vehicles"   element={<VehiclesPage />} />
            <Route path="/dealer/orders"     element={<OrdersPage />} />
            <Route path="/dealer/inquiries"  element={<InquiriesPage />} />
            <Route path="/dealer/profile"    element={<DashboardPage />} />
          </Route>

          {/* Unauthorized */}
          <Route path="/unauthorized" element={
            <div style={{ display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', minHeight:'100vh', textAlign:'center' }}>
              <h1 style={{ fontSize:'4rem', fontWeight:800, color:'var(--brand-500)' }}>403</h1>
              <p style={{ color:'var(--text-secondary)', marginBottom:24 }}>You don't have permission to access this page.</p>
              <a href="/login" className="btn btn-primary">Go to Login</a>
            </div>
          } />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>

      {/* Global toast notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#1e2035',
            color: '#f8fafc',
            border: '1px solid rgba(255,255,255,0.1)',
            borderRadius: '12px',
            fontSize: '0.875rem',
          },
          success: { iconTheme: { primary: '#22c55e', secondary: '#0a0b14' } },
          error:   { iconTheme: { primary: '#ef4444', secondary: '#0a0b14' } },
          duration: 3500,
        }}
      />
    </AuthProvider>
  );
}

export default App;
