import { useEffect, useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Avatar, Button, Card, Descriptions, Space, Typography, theme } from 'antd'
import { InboxOutlined, UserOutlined } from '@ant-design/icons'
import { useAuth } from '../auth/AuthContext'
import { MY_AVATAR_BLOB_QUERY_KEY, profileService } from '../api/profileService'
import { useUserAvatarUrl } from '../hooks/useUserAvatarUrl'

const { Title, Paragraph } = Typography

const ACCEPT_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const MAX_BYTES = 2 * 1024 * 1024

/** Settings: profile photo via choose-file, drag-and-drop, or click avatar (uploads immediately). */
export default function SettingsPage() {
  const { message } = App.useApp()
  const { token } = theme.useToken()
  const { user, mergeUser } = useAuth()
  const qc = useQueryClient()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const avatarSrc = useUserAvatarUrl()
  const [dropActive, setDropActive] = useState(false)

  const { data: me, isLoading } = useQuery({
    queryKey: ['settings-profile'],
    queryFn: profileService.getMe,
  })

  useEffect(() => {
    if (!me) return
    mergeUser({
      userId: me.userId,
      username: me.username,
      email: me.email,
      roles: me.roles,
      hasAvatar: me.hasAvatar,
    })
  }, [me, mergeUser])

  const uploadMut = useMutation({
    mutationFn: profileService.uploadAvatar,
    onSuccess: (res) => {
      mergeUser({ hasAvatar: res.hasAvatar, email: res.email })
      void qc.invalidateQueries({ queryKey: [MY_AVATAR_BLOB_QUERY_KEY] })
      void qc.invalidateQueries({ queryKey: ['settings-profile'] })
      message.success('Profile photo updated')
    },
  })

  const deleteMut = useMutation({
    mutationFn: profileService.deleteAvatar,
    onSuccess: () => {
      mergeUser({ hasAvatar: false })
      // Drop cached blob only — invalidating refires GET /me/avatar and 404 triggers a global error toast.
      qc.removeQueries({ queryKey: [MY_AVATAR_BLOB_QUERY_KEY] })
      void qc.invalidateQueries({ queryKey: ['settings-profile'] })
      message.success('Profile photo removed')
    },
  })

  const display = me ?? user
  const hasPhoto = !!(me?.hasAvatar ?? user?.hasAvatar)

  const tryUploadFile = (file: File | undefined) => {
    if (!file) return
    if (!ACCEPT_IMAGE_TYPES.includes(file.type)) {
      message.warning('Use a JPEG, PNG, WebP, or GIF image.')
      return
    }
    if (file.size > MAX_BYTES) {
      message.warning('Image must be 2 MB or smaller.')
      return
    }
    uploadMut.mutate(file)
  }

  const onFilePicked = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    tryUploadFile(file)
  }

  const onDragOver = (e: DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    if (!uploadMut.isPending) {
      e.dataTransfer.dropEffect = 'copy'
      setDropActive(true)
    }
  }

  const onDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()
    if (e.currentTarget.contains(e.relatedTarget as Node | null)) {
      return
    }
    setDropActive(false)
  }

  const onDrop = (e: DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDropActive(false)
    if (uploadMut.isPending) return
    const file = e.dataTransfer.files?.[0]
    tryUploadFile(file)
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 640 }}>
      <div>
        <Title level={3} style={{ marginBottom: 4 }}>
          Settings
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          Profile photo — drag and drop an image onto the zone below, use{' '}
          <strong>Choose photo</strong>, or click your picture. Files upload immediately (JPEG, PNG, WebP, or GIF; up to 2
          MB).
        </Paragraph>
      </div>

      <Card loading={isLoading && !display}>
        <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
          <Descriptions.Item label="Username">{display?.username ?? '—'}</Descriptions.Item>
          <Descriptions.Item label="Email">{display?.email ?? '—'}</Descriptions.Item>
        </Descriptions>

        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          style={{ display: 'none' }}
          onChange={onFilePicked}
        />

        <Space align="start" size={24} wrap>
          <div style={{ textAlign: 'center' }}>
            <Avatar
              src={hasPhoto ? avatarSrc : undefined}
              icon={<UserOutlined />}
              size={96}
              style={{
                cursor: uploadMut.isPending ? 'wait' : 'pointer',
                backgroundColor: '#0d9488',
              }}
              onClick={() => {
                if (!uploadMut.isPending) fileInputRef.current?.click()
              }}
            />
            <Paragraph type="secondary" style={{ marginTop: 8, marginBottom: 0, maxWidth: 120, fontSize: 12 }}>
              Click photo to change
            </Paragraph>
          </div>

          <Space direction="vertical" align="start" style={{ minWidth: 200 }}>
            <Button type="primary" loading={uploadMut.isPending} onClick={() => fileInputRef.current?.click()}>
              Choose photo
            </Button>
            <Button
              danger
              disabled={!hasPhoto || deleteMut.isPending}
              loading={deleteMut.isPending}
              onClick={() => deleteMut.mutate()}
            >
              Remove photo
            </Button>
          </Space>
        </Space>

        <div
          role="presentation"
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
          style={{
            marginTop: 24,
            padding: '28px 20px',
            border: `2px dashed ${dropActive ? token.colorPrimary : token.colorBorder}`,
            borderRadius: token.borderRadiusLG,
            background: dropActive ? token.colorPrimaryBg : token.colorFillAlter,
            textAlign: 'center',
            transition: 'border-color 0.2s, background 0.2s',
            opacity: uploadMut.isPending ? 0.65 : 1,
            pointerEvents: uploadMut.isPending ? 'none' : 'auto',
          }}
        >
          <InboxOutlined style={{ fontSize: 36, color: token.colorPrimary, display: 'block', marginBottom: 12 }} />
          <Paragraph style={{ marginBottom: 4, fontWeight: 500 }}>Drag and drop your photo here</Paragraph>
          <Paragraph type="secondary" style={{ marginBottom: 0, fontSize: 13 }}>
            Release to upload, or use Choose photo above
          </Paragraph>
        </div>
      </Card>
    </Space>
  )
}
