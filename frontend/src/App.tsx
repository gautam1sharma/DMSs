import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import LoginPage from './auth/LoginPage'
import RegisterPage from './auth/RegisterPage'
import AdminLayout from './layouts/AdminLayout'
import DealerLayout from './layouts/DealerLayout'
import AdminDashboard from './pages/admin/AdminDashboard'
import ManageDealers from './pages/admin/ManageDealers'
import ManageCustomers from './pages/admin/ManageCustomers'
import ManageProducts from './pages/admin/ManageProducts'
import ManageOrders from './pages/admin/ManageOrders'
import ManageUsers from './pages/admin/ManageUsers'
import ManageAuditLogs from './pages/admin/ManageAuditLogs'
import SettingsPage from './pages/SettingsPage'
import DealerDashboard from './pages/dealer/DealerDashboard'
import MyCustomers from './pages/dealer/MyCustomers'
import DealerProducts from './pages/dealer/DealerProducts'
import MyOrders from './pages/dealer/MyOrders'
import DealerProfile from './pages/dealer/DealerProfile'

function HomeRedirect() {
  const { token, user } = useAuth()
  if (!token || !user) {
    return <Navigate to="/login" replace />
  }
  if (user.roles?.includes('ADMIN')) {
    return <Navigate to="/admin" replace />
  }
  if (user.roles?.includes('DEALER')) {
    return <Navigate to="/dealer" replace />
  }
  return <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<HomeRedirect />} />

      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="dealers" element={<ManageDealers />} />
          <Route path="customers" element={<ManageCustomers />} />
          <Route path="products" element={<ManageProducts />} />
          <Route path="orders" element={<ManageOrders />} />
          <Route path="users" element={<ManageUsers />} />
          <Route path="audit-logs" element={<ManageAuditLogs />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['DEALER']} />}>
        <Route path="/dealer" element={<DealerLayout />}>
          <Route index element={<DealerDashboard />} />
          <Route path="customers" element={<MyCustomers />} />
          <Route path="products" element={<DealerProducts />} />
          <Route path="orders" element={<MyOrders />} />
          <Route path="settings" element={<SettingsPage />} />
          <Route path="profile" element={<DealerProfile />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
