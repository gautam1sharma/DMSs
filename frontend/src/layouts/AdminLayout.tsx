import { useMemo, useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Layout,
  Menu,
  theme,
  Dropdown,
  Avatar,
  Typography,
  Button,
  Grid,
  Space,
  Tooltip,
} from 'antd'
import type { MenuProps } from 'antd'
import { useQuery, useQueryClient, useIsFetching } from '@tanstack/react-query'
import {
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import { useAuth } from '../auth/AuthContext'
import {
  invalidateAdminPortalQueries,
  isAdminPortalQuery,
} from '../utils/portalQueryRefresh'
import { menuService } from '../api/menuService'
import { menuIconFromKey } from '../utils/menuIcons'
import type { MenuItemDto } from '../types/models'

const { Header, Sider, Content } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

const FALLBACK_ADMIN_MENU: MenuItemDto[] = [
  { id: -1, label: 'Dashboard', path: '/admin', icon: 'DashboardOutlined', sortOrder: 10, enabled: true, roleNames: ['ADMIN'] },
  { id: -2, label: 'Dealers', path: '/admin/dealers', icon: 'TeamOutlined', sortOrder: 20, enabled: true, roleNames: ['ADMIN'] },
  { id: -3, label: 'Customers', path: '/admin/customers', icon: 'UserOutlined', sortOrder: 30, enabled: true, roleNames: ['ADMIN'] },
  { id: -4, label: 'Products', path: '/admin/products', icon: 'ShoppingOutlined', sortOrder: 40, enabled: true, roleNames: ['ADMIN'] },
  { id: -5, label: 'Orders', path: '/admin/orders', icon: 'ShoppingCartOutlined', sortOrder: 50, enabled: true, roleNames: ['ADMIN'] },
  { id: -6, label: 'Users', path: '/admin/users', icon: 'UserOutlined', sortOrder: 60, enabled: true, roleNames: ['ADMIN'] },
  { id: -7, label: 'Audit log', path: '/admin/audit-logs', icon: 'FileSearchOutlined', sortOrder: 70, enabled: true, roleNames: ['ADMIN'] },
  { id: -8, label: 'Menus', path: '/admin/menus', icon: 'MenuOutlined', sortOrder: 75, enabled: true, roleNames: ['ADMIN'] },
]

function toAntMenuItems(rows: MenuItemDto[]): MenuProps['items'] {
  return rows
    .filter((m) => m.enabled)
    .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
    .map((m) => ({
      key: m.path,
      icon: menuIconFromKey(m.icon),
      label: m.label,
    }))
}

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()
  const queryClient = useQueryClient()
  const adminFetching = useIsFetching({ predicate: isAdminPortalQuery })
  const screens = useBreakpoint()
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken()

  const { data: apiMenus } = useQuery({
    queryKey: ['me-menus'],
    queryFn: menuService.listMyMenus,
    staleTime: 60_000,
  })

  const menuItems = useMemo(() => {
    const src = apiMenus && apiMenus.length > 0 ? apiMenus : FALLBACK_ADMIN_MENU
    return toAntMenuItems(src)
  }, [apiMenus])

  const onMenuClick: MenuProps['onClick'] = ({ key }) => navigate(key)

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        breakpoint="lg"
        collapsedWidth={screens.md ? 80 : 0}
        onBreakpoint={(broken) => {
          if (broken) setCollapsed(true)
        }}
        style={{ background: '#0f172a' }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            padding: collapsed ? 0 : '0 20px',
            color: '#fff',
            fontWeight: 700,
            letterSpacing: 0.5,
          }}
        >
          {collapsed ? 'S' : 'Serene'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={onMenuClick}
          style={{ background: '#0f172a', borderInlineEnd: 'none' }}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 16px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <div style={{ flex: 1 }} />
          <Space size="middle" align="center">
            <Tooltip title="Reload data on this screen">
              <Button
                type="default"
                icon={<ReloadOutlined />}
                loading={adminFetching > 0}
                onClick={() => void invalidateAdminPortalQueries(queryClient)}
              >
                Refresh
              </Button>
            </Tooltip>
            <Dropdown
            menu={{
              items: [
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: 'Sign out',
                  onClick: async () => {
                    await logout()
                    navigate('/login')
                  },
                },
              ],
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <Avatar style={{ backgroundColor: '#0d9488' }} icon={<UserOutlined />} />
              <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
                <Text strong>{user?.username}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  Administrator
                </Text>
              </div>
            </div>
          </Dropdown>
          </Space>
        </Header>
        <Content
          style={{
            margin: 16,
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
