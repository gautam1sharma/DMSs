import api from './axiosInstance'
import type { AuditLog, SpringPage } from '../types/models'

export interface AuditLogListParams {
  page?: number
  size?: number
  sort?: string
  action?: string
  actorUsername?: string
  from?: string
  to?: string
}

export const auditLogService = {
  list: (p: AuditLogListParams) =>
    api
      .get<SpringPage<AuditLog>>('/admin/audit-logs', {
        params: {
          page: p.page ?? 0,
          size: p.size ?? 20,
          sort: p.sort ?? 'createdAt,desc',
          ...(p.action ? { action: p.action } : {}),
          ...(p.actorUsername?.trim() ? { actorUsername: p.actorUsername.trim() } : {}),
          ...(p.from ? { from: p.from } : {}),
          ...(p.to ? { to: p.to } : {}),
        },
      })
      .then((r) => r.data),
}
