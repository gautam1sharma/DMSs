import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Table, Tag, Typography } from 'antd'
import { dashboardService } from '../../api/dashboardService'
import { auditLogService } from '../../api/auditLogService'
import { formatRupee } from '../../utils/formatCurrency'
import { OrderStatusBadge } from '../../components/StatusBadge'
import type { ColumnsType } from 'antd/es/table'
import type { AuditLog, Order } from '../../types/models'
import { useResizableColumns } from '../../utils/resizableTable'
import dayjs from 'dayjs'

const { Title } = Typography

export default function AdminDashboard() {
  const { data, isLoading } = useQuery({
    queryKey: ['admin-dashboard'],
    queryFn: () => dashboardService.adminSummary(),
  })

  const { data: auditPreview, isLoading: auditLoading } = useQuery({
    queryKey: ['admin-audit-preview'],
    queryFn: () => auditLogService.list({ page: 0, size: 10, sort: 'createdAt,desc' }),
  })

  const recentOrderBase: ColumnsType<Order> = [
    { title: 'Order #', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Customer', dataIndex: 'customerName', key: 'customerName' },
    { title: 'Dealer', dataIndex: 'dealerCompanyName', key: 'dealerCompanyName' },
    {
      title: 'Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (v: number) => formatRupee(v),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s) => <OrderStatusBadge status={s} />,
    },
    {
      title: 'Date',
      dataIndex: 'orderDate',
      key: 'orderDate',
      render: (d: string) => dayjs(d).format('YYYY-MM-DD HH:mm'),
    },
  ]

  const { columns: recentOrderColumns, components: recentOrderComponents } = useResizableColumns(
    recentOrderBase,
    {
      orderNumber: 130,
      customerName: 140,
      dealerCompanyName: 140,
      totalAmount: 120,
      status: 110,
      orderDate: 160,
    },
  )

  const auditColumnsBase: ColumnsType<AuditLog> = [
    {
      title: 'Time',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (d: string) => dayjs(d).format('MM-DD HH:mm'),
    },
    { title: 'Action', dataIndex: 'action', key: 'action', ellipsis: true },
    {
      title: 'Actor',
      dataIndex: 'actorUsername',
      key: 'actorUsername',
      width: 110,
      render: (u?: string) => u || '—',
    },
    {
      title: '',
      dataIndex: 'success',
      key: 'success',
      width: 56,
      render: (ok: boolean) => (ok ? <Tag color="success">OK</Tag> : <Tag color="error">!</Tag>),
    },
  ]

  const { columns: auditColumns, components: auditComponents } = useResizableColumns(
    auditColumnsBase,
    {
      createdAt: 150,
      action: 200,
      actorUsername: 110,
      success: 56,
    },
  )

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}>
        Admin overview
      </Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={isLoading}>
            <Statistic title="Dealers" value={data?.dealerCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={isLoading}>
            <Statistic title="Customers" value={data?.customerCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={isLoading}>
            <Statistic title="Orders" value={data?.orderCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={isLoading}>
            <Statistic
              title="Revenue (total)"
              value={data?.revenueTotal ?? 0}
              formatter={(val) => formatRupee(Number(val))}
            />
          </Card>
        </Col>
      </Row>
      <Card title="Recent orders" style={{ marginTop: 24 }} loading={isLoading}>
        <Table<Order>
          rowKey="id"
          size="small"
          tableLayout="fixed"
          components={recentOrderComponents}
          columns={recentOrderColumns}
          dataSource={data?.recentOrders ?? []}
          pagination={false}
        />
      </Card>
      <Card
        title="Recent activity"
        style={{ marginTop: 24 }}
        loading={auditLoading}
        extra={<Link to="/admin/audit-logs">View all</Link>}
      >
        <Table<AuditLog>
          rowKey="id"
          size="small"
          tableLayout="fixed"
          components={auditComponents}
          columns={auditColumns}
          dataSource={auditPreview?.content ?? []}
          pagination={false}
        />
      </Card>
    </div>
  )
}
