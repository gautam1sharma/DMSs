import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function ProtectedRoute({ roles }: { roles: string[] }) {
  const { token, user, hasRole } = useAuth()
  const location = useLocation()

  if (!token || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  const ok = roles.some((r) => hasRole(r))
  if (!ok) {
    if (hasRole('ADMIN')) return <Navigate to="/admin" replace />
    if (hasRole('DEALER')) return <Navigate to="/dealer" replace />
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
