import { Tag } from 'antd'
import type { OrderStatus } from '../types/models'

const orderColors: Record<OrderStatus, string> = {
  PENDING: 'gold',
  CONFIRMED: 'blue',
  SHIPPED: 'cyan',
  DELIVERED: 'green',
  CANCELLED: 'red',
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <Tag color={orderColors[status] || 'default'}>{status}</Tag>
}

export function ActiveBadge({ active }: { active: boolean }) {
  return <Tag color={active ? 'green' : 'default'}>{active ? 'Active' : 'Inactive'}</Tag>
}
