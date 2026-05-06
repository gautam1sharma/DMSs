import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { LoginResponse } from '../types/models'
import { authService } from '../api/authService'

const TOKEN_KEY = 'sems_token'
const USER_KEY = 'sems_user'

export type AuthUser = Omit<LoginResponse, 'token' | 'type'>

interface AuthContextValue {
  token: string | null
  user: AuthUser | null
  loading: boolean
  mergeUser: (patch: Partial<AuthUser>) => void
  login: (username: string, password: string) => Promise<LoginResponse>
  registerDealer: (body: {
    username: string
    email: string
    password: string
    companyName: string
    phone?: string
    address?: string
    countryCode?: string
    stateCode?: string
    city?: string
  }) => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: string) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function normalizeUser(u: AuthUser): AuthUser {
  return { ...u, hasAvatar: Boolean(u.hasAvatar) }
}

function loadStored(): { token: string | null; user: AuthUser | null } {
  const token = localStorage.getItem(TOKEN_KEY)
  const raw = localStorage.getItem(USER_KEY)
  if (!token || !raw) return { token: null, user: null }
  try {
    return { token, user: normalizeUser(JSON.parse(raw) as AuthUser) }
  } catch {
    return { token: null, user: null }
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const initial = loadStored()
  const [token, setToken] = useState<string | null>(initial.token)
  const [user, setUser] = useState<AuthUser | null>(initial.user)

  const persist = useCallback((res: LoginResponse) => {
    const { token: t, ...rest } = res
    const next = normalizeUser(rest as AuthUser)
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(USER_KEY, JSON.stringify(next))
    setToken(t)
    setUser(next)
  }, [])

  const mergeUser = useCallback((patch: Partial<AuthUser>) => {
    setUser((prev) => {
      if (!prev) return prev
      const next = normalizeUser({ ...prev, ...patch })
      localStorage.setItem(USER_KEY, JSON.stringify(next))
      return next
    })
  }, [])

  const login = useCallback(
    async (username: string, password: string) => {
      const res = await authService.login(username, password)
      persist(res)
      return res
    },
    [persist],
  )

  const registerDealer = useCallback(
    async (body: Parameters<AuthContextValue['registerDealer']>[0]) => {
      const res = await authService.registerDealer(body)
      persist(res)
    },
    [persist],
  )

  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } catch {
      /* still clear session */
    } finally {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      setToken(null)
      setUser(null)
    }
  }, [])

  const hasRole = useCallback(
    (role: string) => {
      return !!user?.roles?.includes(role)
    },
    [user],
  )

  const value = useMemo(
    () => ({
      token,
      user,
      loading: false,
      mergeUser,
      login,
      registerDealer,
      logout,
      hasRole,
    }),
    [token, user, mergeUser, login, registerDealer, logout, hasRole],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
