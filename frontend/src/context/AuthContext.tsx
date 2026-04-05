import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { UserInfo } from '../api/endpoints';

interface AuthContextType {
  user: UserInfo | null;
  token: string | null;
  login: (token: string, user: UserInfo) => void;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
  isAdmin: boolean;
  isDealer: boolean;
  isCustomer: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(() => {
    const stored = localStorage.getItem('userInfo');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('accessToken'));

  const login = (accessToken: string, userInfo: UserInfo) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
    setToken(accessToken);
    setUser(userInfo);
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userInfo');
    setToken(null);
    setUser(null);
  };

  const hasRole  = (role: string) => user?.roles?.includes(role) ?? false;
  const isAdmin   = hasRole('ADMIN');
  const isDealer  = hasRole('DEALER');
  const isCustomer = hasRole('CUSTOMER');

  return (
    <AuthContext.Provider value={{
      user, token, login, logout,
      isAuthenticated: !!token,
      hasRole, isAdmin, isDealer, isCustomer,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
