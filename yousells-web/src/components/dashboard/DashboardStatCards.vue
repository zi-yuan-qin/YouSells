<script setup lang="ts">
import {
  UserFilled,
  Bell,
  Warning,
  Plus,
  Star,
  CircleCheck
} from "@element-plus/icons-vue";
import StatCard from "@/components/ui/StatCard.vue";

export interface StatItem {
  label: string;
  value: number;
  icon: string;
}

defineProps<{
  stats: StatItem[];
  loading: boolean;
}>();

const emit = defineEmits<{
  click: [label: string];
}>();

const iconMap: Record<string, any> = {
  Users: UserFilled,
  Bell: Bell,
  Warning: Warning,
  Plus: Plus,
  Star: Star,
  Success: CircleCheck
};

const colorMap: Record<string, "primary" | "success" | "warning" | "danger" | "info"> = {
  Users: "primary",
  Bell: "warning",
  Warning: "danger",
  Plus: "success",
  Star: "info",
  Success: "success"
};
</script>

<template>
  <div class="stats-grid">
    <StatCard
      v-for="item in stats"
      :key="item.label"
      :title="item.label"
      :value="loading ? '—' : item.value"
      :icon="iconMap[item.icon]"
      :color="colorMap[item.icon]"
      class="stats-grid__card"
      @click="emit('click', item.label)"
    />
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-md);
}

.stats-grid__card {
  cursor: pointer;
}

@media (max-width: 700px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
