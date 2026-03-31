<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_EXECUTION } from '@/constants/data-agent-graph-spec'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'FEASIBILITY_ASSESSMENT_NODE')

const feasibilityText = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.FEASIBILITY_RESULT]
  return typeof value === 'string' ? value : ''
})
</script>

<template>
  <section class="feasibility-card" :class="{ 'feasibility-card--success': status === 'success' }">
    <div class="feasibility-card__header">
      <div>
        <div class="feasibility-card__eyebrow">Feasibility</div>
        <h3 class="feasibility-card__title">{{ displayName }}</h3>
      </div>
      <div class="feasibility-card__status">{{ status === 'success' ? '已完成' : '评估中' }}</div>
    </div>

    <div v-if="feasibilityText" class="feasibility-card__section">
      <div class="feasibility-card__label">可行性评估结果</div>
      <pre class="feasibility-card__text">{{ feasibilityText }}</pre>
    </div>

    <div v-else-if="content" class="feasibility-card__section">
      <div class="feasibility-card__label">流式输出</div>
      <pre class="feasibility-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.feasibility-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(245, 158, 11, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.feasibility-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.feasibility-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.feasibility-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #b45309;
}

.feasibility-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.feasibility-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.feasibility-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.feasibility-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.feasibility-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
