import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, DatePicker, Form, Input, Select, Space, Table, Tag, Typography } from 'antd'
import { auditLogService } from '../../api/auditLogService'
import { serverPaginationProps } from '../../utils/tablePagination'
import {
  columnSortOrder,
  serverTableOnChange,
  springSortParam,
  TABLE_SORT_ASC_DESC,
  useServerTableSortRefs,
} from '../../utils/serverTableSort'
import { useResizableColumns } from '../../utils/resizableTable'
import { AUDIT_ACTION_OPTIONS, type AuditLog } from '../../types/models'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import dayjs from 'dayjs'

const { Title } = Typography
const { RangePicker } = DatePicker

const DEFAULT_SORT_FIELD = 'createdAt'

export default function ManageAuditLogs() {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('descend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [form] = Form.useForm<{ action?: string; actorUsername?: string; range?: [dayjs.Dayjs, dayjs.Dayjs] }>()

  const [filters, setFilters] = useState<{
    action?: string
    actorUsername?: string
    from?: string
    to?: string
  }>({})

  const sortParam = springSortParam(sortField, sortOrder ?? 'descend')

  const { data, isLoading } = useQuery({
    queryKey: ['admin-audit-logs', page, size, filters, sortField, sortOrder],
    queryFn: () =>
      auditLogService.list({
        page,
        size,
        sort: sortParam,
        ...filters,
      }),
  })

  const applyFilters = async () => {
    const v = await form.validateFields()
    const range = v.range
    setPage(0)
    setFilters({
      action: v.action,
      actorUsername: v.actorUsername,
      from: range?.[0]?.startOf('day').toISOString(),
      to: range?.[1]?.endOf('day').toISOString(),
    })
  }

  const clearFilters = () => {
    form.resetFields()
    setPage(0)
    setFilters({})
  }

  const baseColumns: ColumnsType<AuditLog> = [
    {
      title: 'Time',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 170,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'createdAt'),
      render: (d: string) => dayjs(d).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: 'Method',
      dataIndex: 'httpMethod',
      key: 'httpMethod',
      width: 88,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'httpMethod'),
      render: (m?: string) =>
        m ? (
          <Tag
            color={
              m === 'GET'
                ? 'blue'
                : m === 'POST'
                  ? 'green'
                  : m === 'PUT'
                    ? 'orange'
                    : m === 'PATCH'
                      ? 'purple'
                      : m === 'DELETE'
                        ? 'red'
                        : 'default'
            }
          >
            {m}
          </Tag>
        ) : (
          '—'
        ),
    },
    {
      title: 'Path',
      dataIndex: 'requestPath',
      key: 'requestPath',
      ellipsis: true,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'requestPath'),
      render: (p?: string) =>
        p ? (
          <span style={{ fontFamily: 'monospace', fontSize: 12 }} title={p}>
            {p}
          </span>
        ) : (
          '—'
        ),
    },
    {
      title: 'HTTP',
      dataIndex: 'httpStatus',
      key: 'httpStatus',
      width: 76,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'httpStatus'),
      render: (code?: number) =>
        code != null ? (
          <Tag
            color={
              code >= 500
                ? 'red'
                : code >= 400
                  ? 'volcano'
                  : code >= 300
                    ? 'geekblue'
                    : 'success'
            }
          >
            {code}
          </Tag>
        ) : (
          '—'
        ),
    },
    {
      title: 'Event',
      dataIndex: 'action',
      key: 'action',
      width: 200,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'action'),
      render: (a: string) => <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{a}</span>,
    },
    {
      title: 'Actor',
      dataIndex: 'actorUsername',
      key: 'actorUsername',
      width: 140,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'actorUsername'),
      render: (u?: string) => u || '—',
    },
    {
      title: 'OK',
      dataIndex: 'success',
      key: 'success',
      width: 72,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'success'),
      render: (ok: boolean) =>
        ok ? <Tag color="success">Yes</Tag> : <Tag color="error">No</Tag>,
    },
    {
      title: 'Target',
      key: 'targetType',
      width: 140,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'targetType'),
      render: (_, r) =>
        r.targetType || r.targetId != null
          ? `${r.targetType ?? ''}${r.targetId != null ? ` #${r.targetId}` : ''}`.trim()
          : '—',
    },
    {
      title: 'IP',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'ipAddress'),
      render: (ip?: string) => ip || '—',
    },
    {
      title: 'Detail',
      dataIndex: 'detail',
      key: 'detail',
      ellipsis: true,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'detail'),
      render: (d?: string) => d || '—',
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    createdAt: 170,
    httpMethod: 88,
    requestPath: 220,
    httpStatus: 76,
    action: 180,
    actorUsername: 120,
    success: 72,
    targetType: 120,
    ipAddress: 120,
    detail: 200,
  })

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}>
        Audit log
      </Title>
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={form} layout="inline" style={{ rowGap: 12 }}>
          <Form.Item name="action" label="Action">
            <Select
              allowClear
              placeholder="Any"
              style={{ minWidth: 220 }}
              options={AUDIT_ACTION_OPTIONS.map((a) => ({ value: a, label: a }))}
            />
          </Form.Item>
          <Form.Item name="actorUsername" label="Username">
            <Input allowClear placeholder="Contains…" style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="range" label="Date range">
            <RangePicker />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={() => void applyFilters()}>
                Apply
              </Button>
              <Button onClick={clearFilters}>Clear</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
      <Table<AuditLog>
        rowKey="id"
        tableLayout="fixed"
        components={components}
        sortDirections={TABLE_SORT_ASC_DESC}
        size="small"
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        scroll={{ x: 1500 }}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<AuditLog>({
          size,
          setPage,
          setSize,
          defaultSortField: DEFAULT_SORT_FIELD,
          defaultSortOrder: 'descend',
          sortFieldRef,
          sortOrderRef,
          setSortField,
          setSortOrder,
        })}
      />
    </div>
  )
}
