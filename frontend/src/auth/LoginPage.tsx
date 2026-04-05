import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Button, Card, Form, Input, Typography, Space, Modal, Select, App } from 'antd'
import { UserOutlined, LockOutlined, ThunderboltOutlined, ShopOutlined } from '@ant-design/icons'
import { useAuth } from './AuthContext'
import type { LoginResponse } from '../types/models'

const { Title, Text } = Typography

/** Matches DataSeeder admin user. */
const DEMO_ADMIN = { username: 'admin', password: 'admin123' }
/** Matches IndianDemoBulkSeed: bharat3_dlr_01 … bharat3_dlr_25 / Dealer123! */
const DEMO_DEALER_PASSWORD = 'Dealer123!'
const DEMO_DEALER_USERNAMES = Array.from({ length: 25 }, (_, i) =>
  `bharat3_dlr_${String(i + 1).padStart(2, '0')}`,
)

const showDemoLogin =
  import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO_LOGIN === 'true'

export default function LoginPage() {
  const { message } = App.useApp()
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(false)
  const [dealerModalOpen, setDealerModalOpen] = useState(false)
  const [dealerPick, setDealerPick] = useState<string | null>(null)
  const [dealerSubmitting, setDealerSubmitting] = useState(false)

  const from = (location.state as { from?: { pathname: string } } | undefined)?.from?.pathname

  const navigateAfterLogin = (res: LoginResponse) => {
    if (from && from !== '/login') {
      navigate(from, { replace: true })
      return
    }
    if (res.roles?.includes('ADMIN')) navigate('/admin', { replace: true })
    else if (res.roles?.includes('DEALER')) navigate('/dealer', { replace: true })
    else navigate('/', { replace: true })
  }

  const runLogin = async (username: string, password: string) => {
    const res = await login(username, password)
    navigateAfterLogin(res)
  }

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      await runLogin(values.username, values.password)
    } catch {
      message.error('Sign in failed. Check username and password.')
    } finally {
      setLoading(false)
    }
  }

  const onDemoAdmin = async () => {
    setLoading(true)
    try {
      await runLogin(DEMO_ADMIN.username, DEMO_ADMIN.password)
    } catch {
      message.error('Demo admin sign-in failed. Is the backend seeded?')
    } finally {
      setLoading(false)
    }
  }

  const onDealerModalOk = async () => {
    if (!dealerPick) {
      message.warning('Choose a dealer account')
      return
    }
    setDealerSubmitting(true)
    try {
      await runLogin(dealerPick, DEMO_DEALER_PASSWORD)
      setDealerModalOpen(false)
      setDealerPick(null)
    } catch {
      message.error('Demo dealer sign-in failed. Is Indian demo seed loaded?')
    } finally {
      setDealerSubmitting(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #0f766e 0%, #134e4a 50%, #0c4a6e 100%)',
        padding: 24,
      }}
    >
      <Card
        style={{ width: 420, maxWidth: '100%', borderRadius: 12, boxShadow: '0 12px 40px rgba(0,0,0,0.2)' }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 0 }}>
              Serene
            </Title>
            <Text type="secondary">Dealer Management — Sign in</Text>
          </div>
          <Form layout="vertical" onFinish={onFinish} requiredMark={false}>
            <Form.Item
              name="username"
              label="Username"
              rules={[{ required: true, message: 'Enter username' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Username" size="large" autoComplete="username" />
            </Form.Item>
            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, message: 'Enter password' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Password"
                size="large"
                autoComplete="current-password"
              />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                Sign in
              </Button>
            </Form.Item>
          </Form>
          {showDemoLogin && (
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>
                Quick demo
              </Text>
              <Button
                icon={<ThunderboltOutlined />}
                block
                size="large"
                disabled={loading || dealerSubmitting}
                onClick={onDemoAdmin}
              >
                Sign in as admin
              </Button>
              <Button
                icon={<ShopOutlined />}
                block
                size="large"
                disabled={loading || dealerSubmitting}
                onClick={() => setDealerModalOpen(true)}
              >
                Sign in as demo dealer…
              </Button>
            </Space>
          )}
          <Modal
            title="Choose demo dealer"
            open={dealerModalOpen}
            onCancel={() => {
              setDealerModalOpen(false)
              setDealerPick(null)
            }}
            onOk={onDealerModalOk}
            okText="Sign in"
            confirmLoading={dealerSubmitting}
            destroyOnClose
          >
            <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
              Indian demo seed accounts use password <code>Dealer123!</code>
            </Text>
            <Select
              showSearch
              placeholder="Select dealer username"
              style={{ width: '100%' }}
              value={dealerPick ?? undefined}
              onChange={setDealerPick}
              optionFilterProp="label"
              options={DEMO_DEALER_USERNAMES.map((u) => ({ value: u, label: u }))}
            />
          </Modal>
          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">Dealer? </Text>
            <Link to="/register">Create dealer account</Link>
          </div>
        </Space>
      </Card>
    </div>
  )
}
