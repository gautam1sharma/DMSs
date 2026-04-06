import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  App,
  Button,
  DatePicker,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tooltip,
  Typography,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, UnlockOutlined } from '@ant-design/icons'
import { userService } from '../../api/userService'
import { ActiveBadge } from '../../components/StatusBadge'
import { CustomerLocationFields } from '../../components/CustomerLocationFields'
import { serverPaginationProps } from '../../utils/tablePagination'
import {
  columnSortOrder,
  serverTableOnChange,
  springSortParam,
  TABLE_SORT_ASC_DESC,
  useServerTableSortRefs,
} from '../../utils/serverTableSort'
import { useResizableColumns } from '../../utils/resizableTable'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import type { User } from '../../types/models'
import dayjs from 'dayjs'

const { Title, Text } = Typography

const ROLE_OPTIONS = ['ADMIN', 'DEALER', 'CUSTOMER']
const DEFAULT_SORT_FIELD = 'username'

function userHasAdminRole(u: User) {
  const roles = u.roles ?? []
  return roles.some((r) => r?.toUpperCase() === 'ADMIN')
}

/** Block delete for seeded admin username even if roles are missing in the list response. */
function userDeleteNotAllowed(u: User) {
  return u.username?.toLowerCase() === 'admin' || userHasAdminRole(u)
}

export default function ManageUsers() {
  const { message } = App.useApp()
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [form] = Form.useForm()
  const watchedRoles = Form.useWatch('roleNames', form) as string[] | undefined

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')
  const showDealerSection = (watchedRoles ?? []).includes('DEALER')
  const showCustomerSection = (watchedRoles ?? []).includes('CUSTOMER')
  const dealerLocationRequired = showDealerSection && (!editing || editing.dealerId == null)
  const customerProfileRequired =
    showCustomerSection && (!editing || editing.customerId == null)
  const locationFieldsRequired = dealerLocationRequired || customerProfileRequired
  const showProfileContact = showDealerSection || showCustomerSection

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users', page, size, sortField, sortOrder],
    queryFn: () => userService.list(page, size, sortParam),
  })

  const createMut = useMutation({
    mutationFn: userService.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-users'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Parameters<typeof userService.update>[1] }) =>
      userService.update(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-users'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: userService.remove,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] }),
  })

  const unlockMut = useMutation({
    mutationFn: userService.unlock,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] }),
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record: User) => {
    setEditing(record)
    form.setFieldsValue({
      email: record.email,
      enabled: record.enabled,
      roleNames: record.roles,
      accountExpiry: record.accountExpiry ? dayjs(record.accountExpiry) : undefined,
      companyName: record.dealerCompanyName,
      customerFullName: record.customerFullName,
      phone: record.dealerPhone ?? record.customerPhone,
      address: record.dealerAddress ?? record.customerAddress,
      countryCode: record.dealerCountryCode ?? record.customerCountryCode,
      stateCode: record.dealerStateCode ?? record.customerStateCode,
      city: record.dealerCity ?? record.customerCity,
      dealerActive: record.dealerActive ?? true,
      customerActive: record.customerActive ?? true,
    })
    setModalOpen(true)
  }

  const submit = async () => {
    const v = await form.validateFields()
    const expiryIso = v.accountExpiry ? (v.accountExpiry as dayjs.Dayjs).toISOString() : undefined
    const needsNewDealerProfile =
      (v.roleNames as string[] | undefined)?.includes('DEALER') &&
      (!editing || editing.dealerId == null)
    const needsNewCustomerProfile =
      (v.roleNames as string[] | undefined)?.includes('CUSTOMER') &&
      (!editing || editing.customerId == null)
    if (needsNewDealerProfile) {
      if (!String(v.companyName ?? '').trim()) {
        message.error('Dealer outlet name is required when the DEALER role is selected')
        return
      }
      if (!v.countryCode || !v.stateCode || !v.city) {
        message.error('Country, state, and city are required when creating a dealer profile')
        return
      }
    }
    if (needsNewCustomerProfile) {
      if (!String(v.customerFullName ?? '').trim()) {
        message.error('Customer full name is required when the CUSTOMER role is selected')
        return
      }
      if (!v.countryCode || !v.stateCode || !v.city) {
        message.error('Country, state, and city are required when creating a customer profile')
        return
      }
    }
    if (editing) {
      const body: Parameters<typeof userService.update>[1] = {
        email: v.email,
        enabled: v.enabled,
        roleNames: v.roleNames,
        accountExpiry: expiryIso,
      }
      if (v.password && String(v.password).trim().length > 0) {
        body.password = v.password
      }
      if (needsNewDealerProfile) {
        body.companyName = v.companyName
        body.phone = v.phone
        body.address = v.address
        body.countryCode = v.countryCode
        body.stateCode = v.stateCode
        body.city = v.city
        body.dealerActive = v.dealerActive ?? true
      }
      if (needsNewCustomerProfile) {
        body.customerFullName = v.customerFullName
        body.phone = v.phone
        body.address = v.address
        body.countryCode = v.countryCode
        body.stateCode = v.stateCode
        body.city = v.city
        body.customerActive = v.customerActive ?? true
      }
      await updateMut.mutateAsync({
        id: editing.id,
        body,
      })
    } else {
      const payload: Parameters<typeof userService.create>[0] = {
        username: v.username,
        email: v.email,
        password: v.password,
        enabled: v.enabled ?? true,
        roleNames: v.roleNames,
        accountExpiry: expiryIso,
      }
      if (needsNewDealerProfile) {
        payload.companyName = v.companyName
        payload.phone = v.phone
        payload.address = v.address
        payload.countryCode = v.countryCode
        payload.stateCode = v.stateCode
        payload.city = v.city
        payload.dealerActive = v.dealerActive ?? true
      }
      if (needsNewCustomerProfile) {
        payload.customerFullName = v.customerFullName
        payload.phone = v.phone
        payload.address = v.address
        payload.countryCode = v.countryCode
        payload.stateCode = v.stateCode
        payload.city = v.city
        payload.customerActive = v.customerActive ?? true
      }
      await createMut.mutateAsync(payload)
    }
  }

  const baseColumns: ColumnsType<User> = [
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'username'),
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'email'),
    },
    {
      title: 'Roles',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: string[]) => roles?.join(', '),
    },
    {
      title: 'Enabled',
      dataIndex: 'enabled',
      key: 'enabled',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'enabled'),
      render: (e: boolean) => <ActiveBadge active={e} />,
    },
    {
      title: 'Failed',
      dataIndex: 'failedAttempts',
      key: 'failedAttempts',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'failedAttempts'),
    },
    {
      title: 'Last login',
      dataIndex: 'lastLoginAt',
      key: 'lastLoginAt',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'lastLoginAt'),
      render: (v: string | undefined) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '—'),
    },
    {
      title: 'Actions',
      key: 'a',
      render: (_, r) => (
        <Space wrap>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(r)}>
            Edit
          </Button>
          <Tooltip title="Clears lockout and sets last login to now (365-day inactivity rule).">
            <Button type="link" icon={<UnlockOutlined />} onClick={() => unlockMut.mutateAsync(r.id)}>
              Unlock
            </Button>
          </Tooltip>
          {userDeleteNotAllowed(r) ? (
            <Tooltip title="Administrator accounts cannot be deleted">
              <span>
                <Button type="link" danger icon={<DeleteOutlined />} disabled>
                  Delete
                </Button>
              </span>
            </Tooltip>
          ) : (
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              onClick={() =>
                Modal.confirm({
                  title: 'Delete user?',
                  onOk: () => deleteMut.mutateAsync(r.id),
                })
              }
            >
              Delete
            </Button>
          )}
        </Space>
      ),
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    username: 140,
    email: 220,
    roles: 160,
    enabled: 100,
    failedAttempts: 88,
    lastLoginAt: 160,
    a: 280,
  })

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          Users
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Add user
        </Button>
      </Space>
      <Table<User>
        rowKey="id"
        tableLayout="fixed"
        components={components}
        sortDirections={TABLE_SORT_ASC_DESC}
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<User>({
          size,
          setPage,
          setSize,
          defaultSortField: DEFAULT_SORT_FIELD,
          sortFieldRef,
          sortOrderRef,
          setSortField,
          setSortOrder,
          nonSortableColumnKeys: new Set(['roles']),
        })}
      />
      <Modal
        title={editing ? 'Edit user' : 'Create user'}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false)
          setEditing(null)
          form.resetFields()
        }}
        onOk={submit}
        confirmLoading={createMut.isPending || updateMut.isPending}
        destroyOnClose
        width={560}
      >
        <Form form={form} layout="vertical">
          {!editing && (
            <>
              <Form.Item name="username" label="Username" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Form.Item name="password" label="Password" rules={[{ required: true, min: 6 }]}>
                <Input.Password />
              </Form.Item>
            </>
          )}
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          {editing && (
            <Form.Item name="password" label="New password" extra="Leave blank to keep current">
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item
            name="roleNames"
            label="Roles"
            extra="Customer cannot be combined with Dealer or Admin (separate logins)."
            rules={[
              { required: true },
              {
                validator: (_: unknown, value: string[] | undefined) => {
                  const v = value ?? []
                  if (v.includes('CUSTOMER') && (v.includes('DEALER') || v.includes('ADMIN'))) {
                    return Promise.reject(
                      new Error('Customer role cannot be combined with Dealer or Administrator'),
                    )
                  }
                  return Promise.resolve()
                },
              },
            ]}
          >
            <Select mode="multiple" options={ROLE_OPTIONS.map((r) => ({ label: r, value: r }))} />
          </Form.Item>
          {showDealerSection && (
            <>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                Dealer portal profile — required to sign in at /dealer (outlet name and service area).
              </Text>
              <Form.Item
                name="companyName"
                label="Dealer outlet name"
                rules={
                  dealerLocationRequired ? [{ required: true, message: 'Required for dealer users' }] : []
                }
              >
                <Input placeholder="Outlet or business name" />
              </Form.Item>
              <Form.Item name="dealerActive" label="Dealer active" valuePropName="checked" initialValue>
                <Switch />
              </Form.Item>
            </>
          )}
          {showCustomerSection && (
            <>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                Customer portal profile — full name and location assign an active dealer by city when possible.
              </Text>
              <Form.Item
                name="customerFullName"
                label="Customer full name"
                rules={
                  customerProfileRequired
                    ? [{ required: true, message: 'Required for customer portal users' }]
                    : []
                }
              >
                <Input placeholder="Display name for orders" />
              </Form.Item>
              <Form.Item name="customerActive" label="Customer active" valuePropName="checked" initialValue>
                <Switch />
              </Form.Item>
            </>
          )}
          {showProfileContact && (
            <>
              <Form.Item name="phone" label="Phone">
                <Input />
              </Form.Item>
              <CustomerLocationFields form={form} locationOptional={!locationFieldsRequired} />
              <Form.Item name="address" label="Street address">
                <Input placeholder="Street, building, unit" />
              </Form.Item>
            </>
          )}
          <Form.Item name="enabled" label="Enabled" valuePropName="checked" initialValue>
            <Switch />
          </Form.Item>
          <Form.Item name="accountExpiry" label="Account expiry">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
