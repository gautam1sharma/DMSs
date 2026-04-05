import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button, Card, Form, Input, Typography, Space } from 'antd'
import { ShopOutlined, UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import { useAuth } from './AuthContext'
import { CustomerLocationFields } from '../components/CustomerLocationFields'

const { Title, Text } = Typography

export default function RegisterPage() {
  const { registerDealer } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()

  const onFinish = async (values: Record<string, string | undefined>) => {
    setLoading(true)
    try {
      await registerDealer({
        username: values.username!,
        email: values.email!,
        password: values.password!,
        companyName: values.companyName!,
        phone: values.phone,
        address: values.address,
        countryCode: values.countryCode,
        stateCode: values.stateCode,
        city: values.city,
      })
      navigate('/dealer', { replace: true })
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
        style={{ width: 480, maxWidth: '100%', borderRadius: 12, boxShadow: '0 12px 40px rgba(0,0,0,0.2)' }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 0 }}>
              Serene
            </Title>
            <Text type="secondary">Register as an authorized dealer</Text>
          </div>
          <Form form={form} layout="vertical" onFinish={onFinish} requiredMark={false}>
            <Form.Item
              name="companyName"
              label="Dealer name"
              rules={[{ required: true, message: 'Required' }]}
            >
              <Input prefix={<ShopOutlined />} placeholder="Outlet or business name" size="large" />
            </Form.Item>
            <Form.Item
              name="username"
              label="Username"
              rules={[{ required: true, min: 3, message: 'Min 3 characters' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Username" size="large" autoComplete="username" />
            </Form.Item>
            <Form.Item
              name="email"
              label="Email"
              rules={[{ required: true, type: 'email', message: 'Valid email required' }]}
            >
              <Input prefix={<MailOutlined />} placeholder="Email" size="large" autoComplete="email" />
            </Form.Item>
            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, min: 6, message: 'Min 6 characters' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Password"
                size="large"
                autoComplete="new-password"
              />
            </Form.Item>
            <Form.Item name="phone" label="Phone">
              <Input placeholder="Phone" size="large" />
            </Form.Item>
            <CustomerLocationFields form={form} />
            <Form.Item name="address" label="Street address">
              <Input placeholder="Street, building, unit" size="large" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                Create account
              </Button>
            </Form.Item>
          </Form>
          <div style={{ textAlign: 'center' }}>
            <Link to="/login">Back to sign in</Link>
          </div>
        </Space>
      </Card>
    </div>
  )
}
