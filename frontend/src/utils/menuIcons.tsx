import {
  AppstoreOutlined,
  DashboardOutlined,
  FileSearchOutlined,
  IdcardOutlined,
  MenuOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons'
import type { ReactNode } from 'react'

const ICONS: Record<string, ReactNode> = {
  DashboardOutlined: <DashboardOutlined />,
  TeamOutlined: <TeamOutlined />,
  UserOutlined: <UserOutlined />,
  ShoppingOutlined: <ShoppingOutlined />,
  ShoppingCartOutlined: <ShoppingCartOutlined />,
  FileSearchOutlined: <FileSearchOutlined />,
  IdcardOutlined: <IdcardOutlined />,
  MenuOutlined: <MenuOutlined />,
}

export function menuIconFromKey(icon?: string | null): ReactNode {
  if (!icon) return <AppstoreOutlined />
  return ICONS[icon] ?? <AppstoreOutlined />
}
