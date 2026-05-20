<script setup lang="ts">
defineProps<{
  stats: { label: string; value: number }[];
  loading: boolean;
}>();
</script>

<template>
  <div class="stats-grid">
    <div v-for="item in stats" :key="item.label" class="stat-card">
      <div class="stat-card__label">{{ item.label }}</div>
      <div class="stat-card__value">{{ loading ? "-" : item.value }}</div>
    </div>
  </div>
</template>
