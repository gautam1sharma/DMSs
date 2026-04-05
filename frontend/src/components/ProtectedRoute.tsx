import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  roles?: string[];
}

export default function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { isAuthenticated, hasRole } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (roles && !roles.some(r => hasRole(r))) return <Navigate to="/unauthorized" replace />;

  return <Outlet />;
}
