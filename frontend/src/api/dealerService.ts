import api from './axiosInstance'
import type { Dealer, SpringPage } from '../types/models'

type DealerLocation = {
  countryCode?: string
  stateCode?: string
  city?: string
}

export const dealerService = {
  adminList: (page: number, size: number, sort?: string, q?: string) =>
    api
      .get<SpringPage<Dealer>>('/admin/dealers', { params: { page, size, sort, q } })
      .then((r) => r.data),

  adminGet: (id: number) => api.get<Dealer>(`/admin/dealers/${id}`).then((r) => r.data),

  adminCreate: (body: {
    username: string
    email: string
    password: string
    companyName: string
    phone?: string
    address?: string
    active?: boolean
  } & DealerLocation) => api.post<Dealer>('/admin/dealers', body).then((r) => r.data),

  adminUpdate: (
    id: number,
    body: {
      email?: string
      companyName?: string
      phone?: string
      address?: string
      active?: boolean
    } & DealerLocation,
  ) => api.put<Dealer>(`/admin/dealers/${id}`, body).then((r) => r.data),

  adminDelete: (id: number) => api.delete(`/admin/dealers/${id}`),

  profile: () => api.get<Dealer>('/dealer/profile').then((r) => r.data),

  updateProfile: (body: {
    email?: string
    companyName?: string
    phone?: string
    address?: string
  } & DealerLocation) => api.put<Dealer>('/dealer/profile', body).then((r) => r.data),
}
