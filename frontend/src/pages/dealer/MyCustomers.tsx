import { useEffect, useRef, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Form, Input, Modal, Space, Switch, Table, Typography } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { customerService } from '../../api/customerService'
import { ActiveBadge } from '../../components/StatusBadge'
import { CustomerLocationFields } from '../../components/CustomerLocationFields'
import { formatCustomerLocation } from '../../utils/formatCustomerLocation'
import { serverPaginationProps } from '../../utils/tablePagination'
import { columnSortOrder, serverTableOnChange, springSortParam } from '../../utils/serverTableSort'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import type { Customer } from '../../types/models'

const { Title } = Typography

const DEFAULT_SORT_FIELD = 'fullName'

export default function MyCustomers() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [q, setQ] = useState('')
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const sortFieldRef = useRef(sortField)
  useEffect(() => {
    sortFieldRef.current = sortField
  }, [sortField])
  const sortOrderRef = useRef(sortOrder)
  useEffect(() => {
    sortOrderRef.current = sortOrder
  }, [sortOrder])
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Customer | null>(null)
  const [form] = Form.useForm()

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')

  const { data, isLoading } = useQuery({
    queryKey: ['dealer-customers', page, size, q, sortField, sortOrder],
    queryFn: () => customerService.dealerList(page, size, sortParam, q || undefined),
  })

  const createMut = useMutation({
    mutationFn: customerService.dealerCreate,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['dealer-customers'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Parameters<typeof customerService.dealerUpdate>[1] }) =>
      customerService.dealerUpdate(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['dealer-customers'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: customerService.dealerDelete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['dealer-customers'] }),
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record: Customer) => {
    setEditing(record)
    form.setFieldsValue({
      fullName: record.fullName,
      phone: record.phone,
      address: record.address,
      countryCode: record.countryCode,
      stateCode: record.stateCode,
      city: record.city,
      active: record.active,
    })
    setModalOpen(true)
  }

  const submit = async () => {
    const v = await form.validateFields()
    const loc = {
      countryCode: v.countryCode,
      stateCode: v.stateCode,
      city: v.city,
    }
    if (editing) {
      await updateMut.mutateAsync({
        id: editing.id,
        body: {
          fullName: v.fullName,
          phone: v.phone,
          address: v.address,
          ...loc,
          active: v.active,
        },
      })
    } else {
      await createMut.mutateAsync({
        fullName: v.fullName,
        phone: v.phone,
        address: v.address,
        ...loc,
        active: v.active ?? true,
      })
    }
  }

  const columns: ColumnsType<Customer> = [
    {
      title: 'Name',
      dataIndex: 'fullName',
      key: 'fullName',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'fullName'),
    },
    {
      title: 'Location',
      key: 'city',
      ellipsis: true,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'city'),
      render: (_, r) => formatCustomerLocation(r),
    },
    {
      title: 'Phone',
      dataIndex: 'phone',
      key: 'phone',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'phone'),
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'active'),
      render: (a: boolean) => <ActiveBadge active={a} />,
    },
    {
      title: 'Actions',
      key: 'a',
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
                title: 'Delete customer?',
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

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          My customers
        </Title>
        <Space>
          <Input.Search
            allowClear
            placeholder="Search"
            onSearch={(v) => {
              setQ(v)
              setPage(0)
            }}
            style={{ width: 220 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Add customer
          </Button>
        </Space>
      </Space>
      <Table<Customer>
        rowKey="id"
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<Customer>({
          size,
          setPage,
          setSize,
          defaultSortField: DEFAULT_SORT_FIELD,
          sortFieldRef,
          sortOrderRef,
          setSortField,
          setSortOrder,
        })}
      />
      <Modal
        title={editing ? 'Edit customer' : 'Add customer'}
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
          <Form.Item name="fullName" label="Full name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input />
          </Form.Item>
          <CustomerLocationFields form={form} locationOptional={!!editing} />
          <Form.Item name="address" label="Street address">
            <Input placeholder="Street, building, unit" />
          </Form.Item>
          <Form.Item name="active" label="Active" valuePropName="checked" initialValue>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
