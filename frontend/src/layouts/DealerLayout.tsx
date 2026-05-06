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
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  ReloadOutlined,
  DashboardOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined,
  IdcardOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { useAuth } from '../auth/AuthContext'
import { useUserAvatarUrl } from '../hooks/useUserAvatarUrl'
import {
  invalidateDealerPortalQueries,
  isDealerPortalQuery,
} from '../utils/portalQueryRefresh'

const { Header, Sider, Content } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

const DEALER_MENU_ITEMS: MenuProps['items'] = [
  { key: '/dealer', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/dealer/customers', icon: <UserOutlined />, label: 'My customers' },
  { key: '/dealer/products', icon: <ShoppingOutlined />, label: 'Products' },
  { key: '/dealer/orders', icon: <ShoppingCartOutlined />, label: 'Orders' },
  { key: '/dealer/profile', icon: <IdcardOutlined />, label: 'Profile' },
]

export default function DealerLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()
  const avatarSrc = useUserAvatarUrl()
  const queryClient = useQueryClient()
  const dealerFetching = useIsFetching({ predicate: isDealerPortalQuery })
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
          }}
        >
          {collapsed ? 'S' : 'Serene'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={DEALER_MENU_ITEMS}
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
            <Tooltip title="Settings — profile photo">
              <Button
                type="default"
                icon={<SettingOutlined />}
                onClick={() => navigate('/dealer/settings')}
              >
                Settings
              </Button>
            </Tooltip>
            <Tooltip title="Reload data on this screen">
              <Button
                type="default"
                icon={<ReloadOutlined />}
                loading={dealerFetching > 0}
                onClick={() => void invalidateDealerPortalQueries(queryClient)}
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
              <Avatar src={avatarSrc} style={{ backgroundColor: '#0d9488' }} icon={<UserOutlined />} />
              <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
                <Text strong>{user?.username}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  Dealer
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
