<script setup lang="ts">
import type { DashboardCustomerReminder } from "@/types/dashboard";
import { stageLabel, intentLabel } from "@/constants/stage";
import { relativeDate } from "@/utils/format";

defineProps<{
  customers: DashboardCustomerReminder[];
  loading: boolean;
}>();
</script>

<template>
  <div class="list-card">
    <h3>重点客户</h3>
    <p v-if="loading" class="list-card__placeholder">加载中...</p>
    <p v-else-if="customers.length === 0" class="list-card__placeholder">暂无重点客户</p>
    <ul v-else>
      <li v-for="customer in customers" :key="customer.customerId">
        {{ customer.nickname }}｜{{ intentLabel(customer.intentLevel) }}｜{{ stageLabel(customer.currentStage) }}｜{{ relativeDate(customer.nextFollowAt) }}
      </li>
    </ul>
  </div>
</template>
