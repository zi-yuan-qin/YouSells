<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageSection from '@/components/app/PageSection.vue'
import { getLeaderboard } from '@/api/leaderboard'
import type { LeaderboardItem } from '@/types/leaderboard'

const loading = ref(false)
const data = ref<LeaderboardItem[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getLeaderboard()
    data.value = res.data.data
  } finally {
    loading.value = false
  }
}

function levelTagType(level: string) {
  switch (level) {
    case 'T3': return 'danger'
    case 'T2': return 'warning'
    case 'T1': return 'success'
    default: return 'info'
  }
}

onMounted(loadData)
</script>

<template>
  <PageSection title="战绩排行" description="本周团队战绩排名">
    <el-table v-loading="loading" :data="data" style="width: 100%">
      <el-table-column type="index" label="排名" width="70" align="center">
        <template #default="{ $index }">
          <el-tag v-if="$index === 0" type="danger" effect="dark">1</el-tag>
          <el-tag v-else-if="$index === 1" type="warning" effect="dark">2</el-tag>
          <el-tag v-else-if="$index === 2" type="success" effect="dark">3</el-tag>
          <span v-else class="rank-number">{{ $index + 1 }}</span>
        </template>
      </el-table-column>

      <el-table-column label="成员" min-width="140">
        <template #default="{ row }">
          <div class="user-cell">
            <span class="user-name">{{ row.realName }}</span>
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.level }}</el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="newCustomerCount" label="新增客户" width="110" align="center" />
      <el-table-column prop="followUpCount" label="跟进次数" width="110" align="center" />
      <el-table-column prop="convertedCount" label="课程达成" width="110" align="center">
        <template #default="{ row }">
          <span :class="{ 'highlight-converted': row.convertedCount > 0 }">
            {{ row.convertedCount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="taskCompletedCount" label="完成任务" width="110" align="center" />
      <el-table-column prop="conversionRate" label="转化率" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.conversionRate >= 50" type="success" size="small">{{ row.conversionRate }}%</el-tag>
          <el-tag v-else-if="row.conversionRate >= 20" type="warning" size="small">{{ row.conversionRate }}%</el-tag>
          <span v-else-if="row.conversionRate > 0" class="rate-low">{{ row.conversionRate }}%</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
    </el-table>
  </PageSection>
</template>

<style scoped>
.rank-number {
  display: inline-block;
  width: 24px;
  text-align: center;
  font-weight: 500;
  color: var(--color-text-secondary);
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-name {
  font-weight: 500;
}
.highlight-converted {
  color: var(--color-success);
  font-weight: 600;
}
.rate-low {
  color: var(--color-text-muted);
}
</style>
