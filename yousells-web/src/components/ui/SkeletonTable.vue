<script setup lang="ts">
defineProps<{
  rows?: number;
  cols?: number;
}>();
</script>

<template>
  <div class="skeleton-table">
    <!-- Header -->
    <div class="skeleton-table__row skeleton-table__header">
      <div
        v-for="c in (cols || 5)"
        :key="`h-${c}`"
        class="skeleton-table__cell"
      >
        <div class="skeleton skeleton--short" />
      </div>
    </div>
    <!-- Body rows -->
    <div
      v-for="r in (rows || 6)"
      :key="`r-${r}`"
      class="skeleton-table__row"
    >
      <div
        v-for="c in (cols || 5)"
        :key="`r-${r}-c-${c}`"
        class="skeleton-table__cell"
      >
        <div class="skeleton" :style="{ width: `${60 + (r + c) * 10}%` }" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-table {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.skeleton-table__row {
  display: flex;
  align-items: center;
  padding: var(--space-md) var(--space-lg);
  border-bottom: 1px solid var(--color-border);
}

.skeleton-table__row:last-child {
  border-bottom: none;
}

.skeleton-table__header {
  background: var(--color-bg-hover);
  padding: var(--space-sm) var(--space-lg);
}

.skeleton-table__cell {
  flex: 1;
  padding-right: var(--space-md);
}

.skeleton {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(
    90deg,
    var(--color-bg-hover) 25%,
    var(--color-bg-surface) 50%,
    var(--color-bg-hover) 75%
  );
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s infinite;
}

.skeleton--short {
  width: 40%;
}

@keyframes skeleton-shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
