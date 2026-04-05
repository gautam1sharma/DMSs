import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Button, Card, Form, Input, Typography, Space } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useAuth } from './AuthContext'

const { Title, Text } = Typography

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(false)

  const from = (location.state as { from?: { pathname: string } } | undefined)?.from?.pathname

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const res = await login(values.username, values.password)
      if (from && from !== '/login') {
        navigate(from, { replace: true })
        return
      }
      if (res.roles?.includes('ADMIN')) navigate('/admin', { replace: true })
      else if (res.roles?.includes('DEALER')) navigate('/dealer', { replace: true })
      else navigate('/', { replace: true })
    } finally {
      setLoading(false)
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
          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">Dealer? </Text>
            <Link to="/register">Create dealer account</Link>
          </div>
        </Space>
      </Card>
    </div>
  )
}
