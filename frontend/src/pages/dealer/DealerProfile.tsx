import { useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Form, Input, Typography } from 'antd'
import { dealerService } from '../../api/dealerService'
import { CustomerLocationFields } from '../../components/CustomerLocationFields'

const { Title } = Typography

export default function DealerProfile() {
  const { message } = App.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()

  const { data, isLoading } = useQuery({
    queryKey: ['dealer-profile'],
    queryFn: () => dealerService.profile(),
  })

  useEffect(() => {
    if (!data) return
    form.setFieldsValue({
      email: data.email,
      companyName: data.companyName,
      phone: data.phone,
      address: data.address,
      countryCode: data.countryCode,
      stateCode: data.stateCode,
      city: data.city,
    })
  }, [data, form])

  const updateMut = useMutation({
    mutationFn: dealerService.updateProfile,
    onSuccess: (res) => {
      qc.setQueryData(['dealer-profile'], res)
      message.success('Profile updated')
    },
  })

  const onFinish = async (v: Record<string, string | undefined>) => {
    await updateMut.mutateAsync({
      email: v.email,
      companyName: v.companyName,
      phone: v.phone,
      address: v.address,
      countryCode: v.countryCode,
      stateCode: v.stateCode,
      city: v.city,
    })
  }

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}>
        Dealer profile
      </Title>
      <Card loading={isLoading} style={{ maxWidth: 560 }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="companyName" label="Dealer name" rules={[{ required: true }]}>
            <Input placeholder="Outlet or business name" />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input />
          </Form.Item>
          <CustomerLocationFields form={form} locationOptional />
          <Form.Item name="address" label="Street address">
            <Input placeholder="Street, building, unit" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={updateMut.isPending}>
              Save changes
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
