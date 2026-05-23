<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  title?: string;
  description?: string;
  actionText?: string;
  icon?: unknown;
  size?: "sm" | "md" | "lg";
}>();

const emit = defineEmits<{
  (e: "action"): void;
}>();

const sizeClass = computed(() => `empty-state--${props.size || "md"}`);
</script>

<template>
  <div class="empty-state" :class="sizeClass">
    <div class="empty-state__icon">
      <slot name="icon">
        <el-icon v-if="icon" :size="size === 'lg' ? 64 : size === 'sm' ? 32 : 48">
          <component :is="icon" />
        </el-icon>
        <svg v-else viewBox="0 0 48 48" fill="none" class="empty-state__svg">
          <rect x="8" y="14" width="32" height="24" rx="4" stroke="currentColor" stroke-width="2" opacity="0.3"/>
          <circle cx="18" cy="24" r="3" fill="currentColor" opacity="0.2"/>
          <circle cx="30" cy="24" r="3" fill="currentColor" opacity="0.2"/>
          <path d="M24 8v6" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
        </svg>
      </slot>
    </div>
    <h3 v-if="title" class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <el-button
      v-if="actionText"
      type="primary"
      size="small"
      @click="emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-2xl);
  color: var(--color-text-muted);
}

.empty-state--sm {
  padding: var(--space-lg);
}

.empty-state--lg {
  padding: var(--space-3xl);
}

.empty-state__icon {
  margin-bottom: var(--space-md);
  color: var(--color-text-muted);
  opacity: 0.6;
}

.empty-state__svg {
  width: 48px;
  height: 48px;
}

.empty-state--lg .empty-state__svg {
  width: 64px;
  height: 64px;
}

.empty-state--sm .empty-state__svg {
  width: 32px;
  height: 32px;
}

.empty-state__title {
  font: var(--font-h2);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-xs);
}

.empty-state__desc {
  font: var(--font-body);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-md);
  max-width: 320px;
}
</style>
