import api from './axiosInstance'
import type { MenuItemDto } from '../types/models'

export type CreateMenuItemBody = {
  label: string
  path: string
  icon?: string
  sortOrder: number
  parentId?: number
  enabled: boolean
  roleNames: string[]
}

export type UpdateMenuItemBody = {
  label?: string
  path?: string
  icon?: string
  sortOrder?: number
  parentId?: number | null
  enabled?: boolean
  roleNames?: string[]
}

export const menuService = {
  listMyMenus: () => api.get<MenuItemDto[]>('/me/menus').then((r) => r.data),

  listAdmin: () => api.get<MenuItemDto[]>('/admin/menus').then((r) => r.data),

  create: (body: CreateMenuItemBody) =>
    api.post<MenuItemDto>('/admin/menus', body).then((r) => r.data),

  update: (id: number, body: UpdateMenuItemBody) =>
    api.put<MenuItemDto>(`/admin/menus/${id}`, body).then((r) => r.data),

  remove: (id: number) => api.delete(`/admin/menus/${id}`),
}
