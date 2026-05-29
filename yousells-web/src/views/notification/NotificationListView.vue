<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageSection from '@/components/app/PageSection.vue'
import { formatDate } from '@/utils/format'
import { getNotifications, markRead, markAllRead, deleteNotification, getTrashNotifications, restoreNotification, permanentDeleteNotification, permanentDeleteAllNotifications } from '@/api/notification'
import type { NotificationItem } from '@/types/notification'
import { fetchChurnRisks } from '@/api/dashboard'
import type { ChurnRiskItem } from '@/types/churn'
import { useRouter } from 'vue-router'
import { RouteName } from '@/router/route-names'

const router = useRouter()
const activeTab = ref<'inbox' | 'trash'>('inbox')
const loading = ref(false)
const notifications = ref<NotificationItem[]>([])
const page = ref(1)
const pageSize = ref(10)
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

const trashTotal = ref(0)
const trashLoading = ref(false)

async function loadTrash() {
  trashLoading.value = true
  try {
    const res = await getTrashNotifications(page.value, pageSize.value)
    notifications.value = res.list
    trashTotal.value = res.total
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "垃圾站加载失败")
  } finally {
    trashLoading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  activeTab.value === 'inbox' ? loadData() : loadTrash()
}

function onPageSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  activeTab.value === 'inbox' ? loadData() : loadTrash()
}

async function handleTabChange(tab: string) {
  activeTab.value = tab as 'inbox' | 'trash'
  page.value = 1
  if (activeTab.value === 'inbox') {
    await loadData()
  } else {
    await loadTrash()
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
  if (item.isDeleted === 1) return
  await handleMarkRead(item)
  if (item.businessType === 'task' && item.businessId) {
    router.push({ name: RouteName.TaskBoard })
  } else if (item.businessType === 'customer' && item.businessId) {
    router.push({ name: RouteName.CustomerDetail, params: { id: item.businessId } })
  } else if (item.businessType === 'topic' && item.businessId) {
    router.push({ name: RouteName.TopicDetail, params: { id: item.businessId } })
  }
}

async function handleDelete(item: NotificationItem) {
  notifications.value = notifications.value.filter(n => n.id !== item.id)
  total.value = Math.max(0, total.value - 1)
  try {
    await deleteNotification(item.id)
  } catch {
    notifications.value.unshift(item)
    total.value++
    ElMessage.error('删除失败')
  }
}

async function handleRestore(item: NotificationItem) {
  notifications.value = notifications.value.filter(n => n.id !== item.id)
  trashTotal.value = Math.max(0, trashTotal.value - 1)
  try {
    await restoreNotification(item.id)
    ElMessage.success('已恢复到收件箱')
  } catch {
    notifications.value.unshift(item)
    trashTotal.value++
    ElMessage.error('恢复失败')
  }
}

async function handleEmptyTrash() {
  if (notifications.value.length === 0) return
  try {
    await ElMessageBox.confirm('将彻底删除垃圾站中所有消息，不可恢复。确定清空？', '清空垃圾站', { type: 'warning', confirmButtonText: '清空' })
  } catch {
    return
  }
  try {
    await permanentDeleteAllNotifications()
    notifications.value = []
    trashTotal.value = 0
    ElMessage.success('垃圾站已清空')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '清空失败')
  }
}

async function handlePermanentDelete(item: NotificationItem) {
  try {
    await ElMessageBox.confirm('彻底删除后无法恢复，确定删除？', '确认', { type: 'warning' })
  } catch {
    return
  }
  notifications.value = notifications.value.filter(n => n.id !== item.id)
  trashTotal.value = Math.max(0, trashTotal.value - 1)
  try {
    await permanentDeleteNotification(item.id)
    ElMessage.success('已彻底删除')
  } catch {
    notifications.value.unshift(item)
    trashTotal.value++
    ElMessage.error('删除失败')
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

const churnRisks = ref<ChurnRiskItem[]>([])
const churnLoading = ref(false)

const factorScoreMap: Record<string, number> = {
  "沉默超过14天": 40,
  "沉默超过7天": 20,
  "意向冷淡": 25,
  "意向下降(观望)": 25,
  "跟进内容含消极关键词": 20,
  "30天无跟进": 20,
  "下一步行动计划逾期7天+": 10
}

function getFactorScore(factor: string): number {
  return factorScoreMap[factor] ?? 0
}

function sumFactorScores(factors: string[]): number {
  return factors.reduce((sum, f) => sum + getFactorScore(f), 0)
}

async function loadChurnRisks() {
  churnLoading.value = true
  try {
    const res = await fetchChurnRisks()
    churnRisks.value = [...(res.highRisk ?? []), ...(res.mediumRisk ?? [])]
  } catch {
    // 非关键数据，静默失败
  } finally {
    churnLoading.value = false
  }
}

const churnPage = ref(1)
const churnPageSize = ref(3)

const pagedChurnRisks = computed(() => {
  const start = (churnPage.value - 1) * churnPageSize.value
  return churnRisks.value.slice(start, start + churnPageSize.value)
})

const scoringRules = [
  { rule: "严重沉默", condition: "距上次跟进超过 14 天", score: 40 },
  { rule: "中度沉默", condition: "距上次跟进超过 7 天", score: 20 },
  { rule: "意向堪忧", condition: "意向为「冷淡」或「观望」", score: 25 },
  { rule: "回应消极", condition: "最近跟进内容含负面关键词", score: 20 },
  { rule: "长期无跟进", condition: "30 天内无任何跟进记录", score: 20 },
  { rule: "行动逾期", condition: "下一步行动计划超过 7 天未执行", score: 10 }
];

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

onMounted(() => {
  loadData()
  loadChurnRisks()
})
</script>

<template>
  <PageSection title="消息中心" description="全部通知消息">
    <template #extra>
      <el-button v-if="activeTab === 'inbox'" type="primary" link @click="handleMarkAllRead">
        全部已读
      </el-button>
      <el-button v-else-if="activeTab === 'trash' && trashTotal > 0" type="danger" link @click="handleEmptyTrash">
        清空垃圾站
      </el-button>
    </template>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="收件箱" name="inbox" />
      <el-tab-pane label="垃圾站" name="trash" />
    </el-tabs>

    <el-table v-loading="loading || trashLoading" :data="notifications" style="width: 100%">
      <el-table-column width="100">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" min-width="140">
        <template #default="{ row }">
          <span :class="{ 'unread-title': row.isRead === 0 && row.isDeleted === 0 }">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="时间" width="140">
        <template #default="{ row }">
          {{ formatDate(activeTab === 'trash' && row.updatedAt ? row.updatedAt : row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column width="180" align="center">
        <template #default="{ row }">
          <template v-if="activeTab === 'inbox'">
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
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
          <template v-else>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleRestore(row)"
            >
              恢复
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handlePermanentDelete(row)"
            >
              彻底删除
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !trashLoading && notifications.length === 0" description="暂无数据" />

    <el-pagination
      v-if="(activeTab === 'inbox' ? total : trashTotal) > 0"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="activeTab === 'inbox' ? total : trashTotal"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="onPageChange"
      @size-change="onPageSizeChange"
    />

    <el-divider />

    <div class="churn-detail">
      <h4 class="scoring-title">风险客户加分明细</h4>
      <div v-if="churnRisks.length === 0 && !churnLoading" class="churn-empty">暂无风险客户</div>
      <div v-else v-for="risk in pagedChurnRisks" :key="risk.customerId" class="churn-customer">
        <div class="churn-customer__header">
          <span class="churn-customer__name">{{ risk.customerName }}</span>
          <el-tag :type="risk.riskLevel === 'high' ? 'danger' : 'warning'" size="small">
            {{ risk.riskLevel === 'high' ? '高风险' : '中风险' }}
          </el-tag>
          <span class="churn-customer__total">总分 <strong>{{ risk.riskScore }}</strong></span>
        </div>
        <div class="churn-customer__factors">
          <div v-for="factor in risk.riskFactors" :key="factor" class="churn-factor">
            <span class="churn-factor__name">{{ factor }}</span>
            <el-tag size="small" :type="getFactorScore(factor) >= 40 ? 'danger' : getFactorScore(factor) >= 20 ? 'warning' : 'info'">
              +{{ getFactorScore(factor) }}
            </el-tag>
          </div>
          <div class="churn-factor churn-factor--ai" v-if="risk.suggestion">
            <span class="churn-factor__name">AI 建议</span>
            <el-tooltip :content="risk.suggestion" placement="top" :show-after="300">
              <span class="churn-factor__suggestion">{{ risk.suggestion }}</span>
            </el-tooltip>
          </div>
        </div>
      </div>
      <el-pagination
        v-if="churnRisks.length > churnPageSize"
        v-model:current-page="churnPage"
        v-model:page-size="churnPageSize"
        :total="churnRisks.length"
        layout="total, prev, pager, next"
        size="small"
        class="pagination"
      />
    </div>

    <el-divider />

    <div class="scoring-rules">
      <h4 class="scoring-title">客户流失风险评分标准</h4>
      <el-table :data="scoringRules" size="small" :show-header="false" style="width: 100%">
        <el-table-column prop="rule" label="规则" width="280" />
        <el-table-column prop="condition" label="条件" min-width="240" />
        <el-table-column prop="score" label="分值" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.score >= 40 ? 'danger' : row.score >= 20 ? 'warning' : 'info'" size="small">
              +{{ row.score }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="scoring-threshold">
        <el-tag type="danger" size="small">≥ 60 高风险</el-tag>
        <el-tag type="warning" size="small" style="margin-left: 8px">30~55 中风险</el-tag>
        <el-tag type="info" size="small" style="margin-left: 8px">&lt; 30 不展示</el-tag>
      </div>
    </div>
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
.scoring-rules {
  margin-top: 8px;
}
.scoring-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 12px 0;
}
.scoring-threshold {
  margin-top: 12px;
}
.churn-detail {
  margin-top: 8px;
}
.churn-empty {
  text-align: center;
  padding: 16px;
  color: var(--color-text-muted);
  font-size: 13px;
}
.churn-customer {
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-bg-elevated);
}
.churn-customer__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.churn-customer__name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
}
.churn-customer__total {
  margin-left: auto;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.churn-customer__total strong {
  color: var(--color-primary);
  font-size: 16px;
}
.churn-customer__factors {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.churn-factor {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--color-bg-base);
  border-radius: 4px;
  font-size: 12px;
}
.churn-factor__name {
  color: var(--color-text-secondary);
}
.churn-factor--ai {
  width: 100%;
  background: var(--color-primary-soft);
}
.churn-factor__suggestion {
  color: var(--color-text-primary);
  font-size: 12px;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
</style>
