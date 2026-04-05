import { useState } from 'react'
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
import { useQueryClient, useIsFetching } from '@tanstack/react-query'
import {
  DashboardOutlined,
  TeamOutlined,
  UserOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  FileSearchOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import { useAuth } from '../auth/AuthContext'
import {
  invalidateAdminPortalQueries,
  isAdminPortalQuery,
} from '../utils/portalQueryRefresh'

const { Header, Sider, Content } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

const menuItems: MenuProps['items'] = [
  { key: '/admin', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/admin/dealers', icon: <TeamOutlined />, label: 'Dealers' },
  { key: '/admin/customers', icon: <UserOutlined />, label: 'Customers' },
  { key: '/admin/products', icon: <ShoppingOutlined />, label: 'Products' },
  { key: '/admin/orders', icon: <ShoppingCartOutlined />, label: 'Orders' },
  { key: '/admin/users', icon: <UserOutlined />, label: 'Users' },
  { key: '/admin/audit-logs', icon: <FileSearchOutlined />, label: 'Audit log' },
]

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
