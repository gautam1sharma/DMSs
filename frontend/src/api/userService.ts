import api from './axiosInstance'
import type { SpringPage, User } from '../types/models'

export const userService = {
  list: (page: number, size: number, sort?: string) =>
    api
      .get<SpringPage<User>>('/admin/users', { params: { page, size, sort } })
      .then((r) => r.data),

  get: (id: number) => api.get<User>(`/admin/users/${id}`).then((r) => r.data),

  create: (body: {
    username: string
    email: string
    password: string
    enabled?: boolean
    roleNames: string[]
    accountExpiry?: string
  }) => api.post<User>('/admin/users', body).then((r) => r.data),

  update: (
    id: number,
    body: {
      email?: string
      password?: string
      enabled?: boolean
      roleNames?: string[]
      accountExpiry?: string
    },
  ) => api.put<User>(`/admin/users/${id}`, body).then((r) => r.data),

  remove: (id: number) => api.delete(`/admin/users/${id}`),

  unlock: (id: number) => api.post<User>(`/admin/users/${id}/unlock`).then((r) => r.data),
}
