<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '@/components/app/PageSection.vue'
import { getNotifications, markRead, markAllRead } from '@/api/notification'
import type { NotificationItem } from '@/types/notification'
import { useRouter } from 'vue-router'
import { RouteName } from '@/router/route-names'

const router = useRouter()
const loading = ref(false)
const notifications = ref<NotificationItem[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function loadData() {
  loading.value = true
  try {
    const res = await getNotifications(page.value, pageSize.value)
    notifications.value = res.list
    total.value = res.total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "通知加载失败")
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(item: NotificationItem) {
  if (item.isRead === 1) return
  await markRead(item.id)
  item.isRead = 1
}

async function handleMarkAllRead() {
  await markAllRead()
  notifications.value.forEach(n => n.isRead = 1)
}

async function handleClickItem(item: NotificationItem) {
  await handleMarkRead(item)
  if (item.businessType === 'task' && item.businessId) {
    router.push({ name: RouteName.TaskBoard })
  } else if (item.businessType === 'customer' && item.businessId) {
    router.push({ name: RouteName.CustomerDetail, params: { id: item.businessId } })
  } else if (item.businessType === 'topic' && item.businessId) {
    router.push({ name: RouteName.TopicDetail, params: { id: item.businessId } })
  }
}

function typeTagType(type: string) {
  switch (type) {
    case 'TASK_ASSIGNED':
    case 'TASK_DUE_SOON':
      return 'warning'
    case 'TASK_STATUS_CHANGED':
      return 'info'
    case 'FOLLOW_UP_REMINDER':
      return 'danger'
    case 'TOPIC_REPLIED':
    case 'TOPIC_MARKED_SOLUTION':
      return 'success'
    default:
      return 'info'
  }
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    TASK_ASSIGNED: '任务指派',
    TASK_DUE_SOON: '任务到期',
    TASK_STATUS_CHANGED: '任务变更',
    FOLLOW_UP_REMINDER: '跟进提醒',
    CUSTOMER_ASSIGNED: '客户转交',
    TOPIC_REPLIED: '新回答',
    TOPIC_MARKED_SOLUTION: '最佳方案',
    SYSTEM_ANNOUNCEMENT: '系统公告'
  }
  return map[type] || type
}

onMounted(loadData)
</script>

<template>
  <PageSection title="消息中心" description="全部通知消息">
    <template #extra>
      <el-button type="primary" link @click="handleMarkAllRead">
        全部已读
      </el-button>
    </template>

    <el-table v-loading="loading" :data="notifications" style="width: 100%">
      <el-table-column width="100">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" min-width="140">
        <template #default="{ row }">
          <span :class="{ 'unread-title': row.isRead === 0 }">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column width="100" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.isRead === 0"
            link
            type="primary"
            size="small"
            @click="handleMarkRead(row)"
          >
            标为已读
          </el-button>
          <el-button
            link
            type="primary"
            size="small"
            @click="handleClickItem(row)"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="loadData"
      @size-change="loadData"
    />
  </PageSection>
</template>

<style scoped>
.unread-title {
  font-weight: 600;
  color: var(--color-text-primary);
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
