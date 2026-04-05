import api from './axiosInstance'
import type { LoginResponse } from '../types/models'

export const authService = {
  login: (username: string, password: string) =>
    api.post<LoginResponse>('/auth/login', { username, password }).then((r) => r.data),

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
  }) => api.post<LoginResponse>('/auth/register', body).then((r) => r.data),

  logout: () => api.post('/auth/logout').then(() => undefined),
}
