<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markRead, markAllRead } from '@/api/notification'
import type { NotificationItem } from '@/types/notification'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { RouteName } from '@/router/route-names'

const authStore = useAuthStore()
const router = useRouter()

const unreadCount = ref(0)
const notifications = ref<NotificationItem[]>([])
const popoverVisible = ref(false)
let ws: WebSocket | null = null
let pollTimer: number | null = null
let reconnectTimer: number | null = null
let isMounted = false

function connectWebSocket() {
  const token = authStore.accessToken
  if (!token) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/notifications?token=${token}`

  ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'notification') {
        unreadCount.value++
        notifications.value.unshift(data.data)
      }
    } catch {
      // ignore malformed messages
    }
  }

  ws.onclose = () => {
    if (!isMounted) return
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = window.setTimeout(connectWebSocket, 5000)
  }
}

async function loadUnreadCount() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch {
    // ignore
  }
}

async function loadNotifications() {
  try {
    const res = await getNotifications(1, 10)
    notifications.value = res.list
  } catch {
    // ignore
  }
}

async function handleMarkRead(item: NotificationItem, event: Event) {
  event.stopPropagation()
  await markRead(item.id)
  item.isRead = 1
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

async function handleMarkAllRead() {
  await markAllRead()
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
}

function handleClickItem(item: NotificationItem) {
  if (!item.isRead) {
    markRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  popoverVisible.value = false

  if (item.businessType === 'task' && item.businessId) {
    router.push({ name: RouteName.TaskBoard })
  } else if (item.businessType === 'customer' && item.businessId) {
    router.push({ name: RouteName.CustomerDetail, params: { id: item.businessId } })
  } else if (item.businessType === 'topic' && item.businessId) {
    router.push({ name: RouteName.TopicDetail, params: { id: item.businessId } })
  }
}

function startPolling() {
  pollTimer = window.setInterval(loadUnreadCount, 30000)
}

onMounted(() => {
  isMounted = true
  loadUnreadCount()
  connectWebSocket()
  startPolling()
})

onUnmounted(() => {
  isMounted = false
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) ws.close()
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="bottom"
    :width="360"
    trigger="click"
    @show="loadNotifications"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="20" class="bell-icon">
          <Bell />
        </el-icon>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="notification-header">
        <span class="title">消息通知</span>
        <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>

      <div v-if="notifications.length === 0" class="notification-empty">
        暂无消息
      </div>

      <div v-else class="notification-list">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleClickItem(item)"
        >
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-body">{{ item.content }}</div>
            <div class="notification-time">{{ item.createdAt }}</div>
          </div>
          <el-button
            v-if="item.isRead === 0"
            link
            type="primary"
            size="small"
            @click="handleMarkRead(item, $event)"
          >
            标为已读
          </el-button>
        </div>
      </div>

      <div class="notification-footer">
        <el-button link type="primary" size="small" @click="router.push({ name: RouteName.NotificationList })">
          查看全部
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.notification-badge {
  cursor: pointer;
  margin-right: 16px;
}
.bell-icon {
  color: var(--color-text-secondary);
  transition: color 0.2s;
}
.bell-icon:hover {
  color: var(--color-primary);
}
.notification-panel {
  max-height: 400px;
  overflow-y: auto;
}
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 8px;
}
.notification-header .title {
  font-weight: 600;
  font-size: 14px;
}
.notification-empty {
  text-align: center;
  padding: 24px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.notification-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.notification-item:hover {
  background: var(--color-bg-hover);
}
.notification-item.unread {
  background: var(--color-primary-soft);
}
.notification-item.unread:hover {
  background: var(--color-primary-glow);
}
.notification-content {
  flex: 1;
  min-width: 0;
}
.notification-title {
  font-weight: 500;
  font-size: 13px;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.notification-body {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.notification-time {
  font-size: 11px;
  color: var(--color-text-muted);
}
.notification-footer {
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
  margin-top: 8px;
}
</style>
