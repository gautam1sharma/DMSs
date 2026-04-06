export interface MenuItemDto {
  id: number
  label: string
  path: string
  icon?: string
  sortOrder: number
  parentId?: number | null
  enabled: boolean
  roleNames: string[]
}

export interface LoginResponse {
  token: string
  type: string
  userId: number
  username: string
  email: string
  roles: string[]
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface User {
  id: number
  username: string
  email: string
  enabled: boolean
  failedAttempts: number
  lockTime?: string
  accountExpiry?: string
  lastLoginAt?: string
  roles: string[]
  createdAt?: string
  updatedAt?: string
  /** Set when user has a dealer portal profile (Manage Users + DEALER role). */
  dealerId?: number
  dealerCompanyName?: string
  dealerPhone?: string
  dealerAddress?: string
  dealerCountryCode?: string
  dealerStateCode?: string
  dealerCity?: string
  dealerActive?: boolean
  /** Linked customers row when user has a customer portal profile. */
  customerId?: number
  customerFullName?: string
  customerPhone?: string
  customerAddress?: string
  customerCountryCode?: string
  customerStateCode?: string
  customerCity?: string
  customerActive?: boolean
}

export interface Dealer {
  id: number
  userId: number
  username: string
  email: string
  companyName: string
  phone?: string
  address?: string
  countryCode?: string
  stateCode?: string
  city?: string
  active: boolean
  createdAt?: string
}

export interface Customer {
  id: number
  dealerId?: number
  dealerCompanyName?: string
  userId?: number
  fullName: string
  phone?: string
  address?: string
  countryCode?: string
  stateCode?: string
  city?: string
  active: boolean
  createdAt?: string
}

export interface Product {
  id: number
  name: string
  description?: string
  price: number
  stockQty: number
  category?: string
  active: boolean
  createdAt?: string
  updatedAt?: string
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'

export interface OrderItem {
  id: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
}

export interface Order {
  id: number
  orderNumber: string
  customerId: number
  customerName: string
  dealerId?: number
  dealerCompanyName?: string
  totalAmount: number
  status: OrderStatus
  orderDate: string
  items: OrderItem[]
}

export interface DashboardSummary {
  dealerCount: number
  customerCount: number
  orderCount: number
  revenueTotal: number
  recentOrders: Order[]
}

export const AUDIT_ACTION_OPTIONS = [
  'LOGIN_SUCCESS',
  'LOGIN_FAILED',
  'LOGOUT',
  'REGISTER_DEALER',
  'ORDER_CREATED',
  'ORDER_STATUS_CHANGED',
  'CUSTOMER_CREATED',
  'CUSTOMER_UPDATED',
  'CUSTOMER_DELETED',
  'PRODUCT_CREATED',
  'PRODUCT_UPDATED',
  'PRODUCT_DELETED',
  'DEALER_CREATED',
  'DEALER_UPDATED',
  'DEALER_DELETED',
  'DEALER_PROFILE_UPDATED',
  'USER_CREATED',
  'USER_UPDATED',
  'USER_DELETED',
  'USER_UNLOCKED',
  'ADMIN_API_REQUEST',
  'DEALER_API_REQUEST',
] as const

export type AuditActionName = (typeof AUDIT_ACTION_OPTIONS)[number]

export interface AuditLog {
  id: number
  createdAt: string
  action: string
  actorUsername?: string
  actorUserId?: number
  targetType?: string
  targetId?: number
  detail?: string
  success: boolean
  ipAddress?: string
  /** Present for ADMIN_API_REQUEST rows */
  httpMethod?: string
  requestPath?: string
  httpStatus?: number
}

export interface ApiErrorBody {
  timestamp?: string
  status: number
  message: string
  path?: string
  traceId?: string
  fieldErrors?: Record<string, string>
}
