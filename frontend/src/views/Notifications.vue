<template>
  <div class="notifications-page">
    <div class="page-header">
      <h1>消息通知</h1>
      <el-button v-if="notifications.length > 0" text type="primary" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <div v-loading="loading">
      <div v-if="notifications.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无通知" />
      </div>

      <div v-else class="notification-list">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleClick(item)"
        >
          <div class="notif-left">
            <div class="notif-dot" v-if="!item.isRead" />
            <div class="notif-icon">
              <el-icon v-if="item.type === 'REGISTRATION_SUCCESS'" color="#059669"><CircleCheck /></el-icon>
              <el-icon v-else-if="item.type === 'REGISTRATION_CANCELLED'" color="#71717A"><CircleClose /></el-icon>
              <el-icon v-else-if="item.type === 'EVENT_CANCELLED'" color="#DC2626"><WarningFilled /></el-icon>
              <el-icon v-else-if="item.type === 'EVENT_UPDATED'" color="#B45309"><InfoFilled /></el-icon>
              <el-icon v-else color="#71717A"><Bell /></el-icon>
            </div>
          </div>
          <div class="notif-body">
            <div class="notif-title">
              {{ item.title }}
              <span v-if="!item.isRead" class="unread-badge">未读</span>
            </div>
            <div class="notif-content">{{ item.content }}</div>
            <div class="notif-time">{{ formatDateTime(item.createdAt) }}</div>
          </div>
          <div class="notif-right">
            <el-button
              v-if="!item.isRead"
              size="small"
              text
              type="primary"
              @click.stop="handleMarkRead(item)"
            >
              标为已读
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, WarningFilled, InfoFilled, Bell } from '@element-plus/icons-vue'
import { getNotifications, markAsRead, markAllAsRead } from '../api/notification'
import { formatDateTime } from '../utils/eventUtils'
import type { NotificationItem } from '../types'

const router = useRouter()
const notifications = ref<NotificationItem[]>([])
const loading = ref(false)

async function loadNotifications() {
  loading.value = true
  try {
    const res = await getNotifications()
    notifications.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(item: NotificationItem) {
  try {
    await markAsRead(item.id)
    item.isRead = true
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('已全部标为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleClick(item: NotificationItem) {
  if (!item.isRead) {
    handleMarkRead(item)
  }
  if (item.relatedEventId) {
    router.push(`/events/${item.relatedEventId}`)
  }
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: var(--c-text-primary, #18181B);
}

.empty-state {
  padding: 60px 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--c-border, #E4E4E7);
  border: 1px solid var(--c-border, #E4E4E7);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: var(--c-bg-card, #FFFFFF);
  cursor: pointer;
  transition: background 0.15s;
}

.notification-item:hover {
  background: var(--c-bg-hover, #FAFAFA);
}

.notification-item.unread {
  background: #FFF7ED;
}

.notif-left {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 2px;
}

.notif-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-primary, #B45309);
  flex-shrink: 0;
}

.notif-icon {
  font-size: 20px;
  display: flex;
  align-items: center;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--c-text-primary, #18181B);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-badge {
  font-size: 11px;
  font-weight: 500;
  color: var(--c-primary, #B45309);
  background: #FFF7ED;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid #FED7AA;
}

.notif-content {
  font-size: 13px;
  color: var(--c-text-secondary, #71717A);
  line-height: 1.5;
  margin-bottom: 4px;
}

.notif-time {
  font-size: 12px;
  color: var(--c-text-tertiary, #A1A1AA);
}

.notif-right {
  flex-shrink: 0;
  padding-top: 2px;
}
</style>
