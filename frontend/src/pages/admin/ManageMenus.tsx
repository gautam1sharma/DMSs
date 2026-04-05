import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag, Typography } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { menuService, type CreateMenuItemBody, type UpdateMenuItemBody } from '../../api/menuService'
import { ActiveBadge } from '../../components/StatusBadge'
import type { ColumnsType } from 'antd/es/table'
import type { MenuItemDto } from '../../types/models'
import { useResizableColumns } from '../../utils/resizableTable'

const { Title } = Typography

const ROLE_OPTIONS = ['ADMIN', 'DEALER', 'CUSTOMER']
const ICON_OPTIONS = [
  'DashboardOutlined',
  'TeamOutlined',
  'UserOutlined',
  'ShoppingOutlined',
  'ShoppingCartOutlined',
  'FileSearchOutlined',
  'IdcardOutlined',
  'MenuOutlined',
]

export default function ManageMenus() {
  const qc = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<MenuItemDto | null>(null)
  const [form] = Form.useForm()

  const { data, isLoading } = useQuery({
    queryKey: ['admin-menus'],
    queryFn: menuService.listAdmin,
  })

  const createMut = useMutation({
    mutationFn: (body: CreateMenuItemBody) => menuService.create(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-menus'] })
      qc.invalidateQueries({ queryKey: ['me-menus'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: UpdateMenuItemBody }) => menuService.update(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-menus'] })
      qc.invalidateQueries({ queryKey: ['me-menus'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: menuService.remove,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-menus'] })
      qc.invalidateQueries({ queryKey: ['me-menus'] })
    },
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({
      enabled: true,
      sortOrder: 100,
      roleNames: ['ADMIN'],
    })
    setModalOpen(true)
  }

  const openEdit = (record: MenuItemDto) => {
    setEditing(record)
    form.setFieldsValue({
      label: record.label,
      path: record.path,
      icon: record.icon,
      sortOrder: record.sortOrder,
      parentId: record.parentId ?? undefined,
      enabled: record.enabled,
      roleNames: record.roleNames,
    })
    setModalOpen(true)
  }

  const submit = async () => {
    const v = await form.validateFields()
    if (editing) {
      const body: UpdateMenuItemBody = {
        label: v.label,
        path: v.path,
        icon: v.icon,
        sortOrder: v.sortOrder,
        parentId: v.parentId ?? null,
        enabled: v.enabled,
        roleNames: v.roleNames,
      }
      await updateMut.mutateAsync({ id: editing.id, body })
    } else {
      const body: CreateMenuItemBody = {
        label: v.label,
        path: v.path,
        icon: v.icon,
        sortOrder: v.sortOrder,
        parentId: v.parentId,
        enabled: v.enabled ?? true,
        roleNames: v.roleNames,
      }
      await createMut.mutateAsync(body)
    }
  }

  const parentOptions =
    data
      ?.filter((m) => !editing || m.id !== editing.id)
      .map((m) => ({ value: m.id, label: `${m.label} (${m.path})` })) ?? []

  const baseColumns: ColumnsType<MenuItemDto> = [
    { title: 'Sort', dataIndex: 'sortOrder', width: 72, sorter: (a, b) => a.sortOrder - b.sortOrder, defaultSortOrder: 'ascend' },
    { title: 'Label', dataIndex: 'label', ellipsis: true },
    { title: 'Path', dataIndex: 'path', ellipsis: true },
    { title: 'Icon', dataIndex: 'icon', width: 160, ellipsis: true },
    {
      title: 'Roles',
      dataIndex: 'roleNames',
      render: (roles: string[]) =>
        roles?.length ? (
          <Space size={[0, 4]} wrap>
            {roles.map((r) => (
              <Tag key={r}>{r}</Tag>
            ))}
          </Space>
        ) : (
          '—'
        ),
    },
    {
      title: 'Enabled',
      dataIndex: 'enabled',
      width: 100,
      render: (e: boolean) => <ActiveBadge active={e} />,
    },
    {
      title: 'Actions',
      key: 'a',
      width: 160,
      render: (_, r) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(r)}>
            Edit
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() =>
              Modal.confirm({
                title: 'Delete menu item?',
                onOk: () => deleteMut.mutateAsync(r.id),
              })
            }
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    sortOrder: 72,
    label: 160,
    path: 200,
    icon: 140,
    roleNames: 200,
    enabled: 100,
    a: 200,
  })

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          Menu items
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Add menu item
        </Button>
      </Space>
      <Table<MenuItemDto>
        rowKey="id"
        tableLayout="fixed"
        components={components}
        loading={isLoading}
        columns={columns}
        dataSource={data ?? []}
        pagination={false}
      />
      <Modal
        title={editing ? 'Edit menu item' : 'Add menu item'}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false)
          setEditing(null)
          form.resetFields()
        }}
        onOk={() => void submit()}
        confirmLoading={createMut.isPending || updateMut.isPending}
        destroyOnClose
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="label" label="Label" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="path" label="Path (SPA route)" rules={[{ required: true }]}>
            <Input placeholder="/admin/..." />
          </Form.Item>
          <Form.Item name="icon" label="Icon key">
            <Select allowClear options={ICON_OPTIONS.map((i) => ({ value: i, label: i }))} placeholder="Ant Design icon name" />
          </Form.Item>
          <Form.Item name="sortOrder" label="Sort order" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="parentId" label="Parent (optional)">
            <Select allowClear options={parentOptions} placeholder="None" />
          </Form.Item>
          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="roleNames" label="Roles" rules={[{ required: true, message: 'Select at least one role' }]}>
            <Select mode="multiple" options={ROLE_OPTIONS.map((r) => ({ value: r, label: r }))} placeholder="Who sees this link" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
