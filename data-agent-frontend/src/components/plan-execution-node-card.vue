<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_PLANNING } from '@/constants/data-agent-graph-spec'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'PLAN_EXECUTE_NODE')

const currentStep = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.CURRENT_STEP]
  return typeof value === 'number' ? value : null
})

const nextNode = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.NEXT_NODE]
  return typeof value === 'string' ? value : ''
})

const executionOutput = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.EXECUTION_OUTPUT]
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
})
</script>

<template>
  <section class="plan-execution-card" :class="{ 'plan-execution-card--success': status === 'success' }">
    <div class="plan-execution-card__header">
      <div>
        <div class="plan-execution-card__eyebrow">Plan Execute</div>
        <h3 class="plan-execution-card__title">{{ displayName }}</h3>
      </div>
      <div class="plan-execution-card__status">{{ status === 'success' ? '已完成' : '执行中' }}</div>
    </div>

    <div class="plan-execution-card__meta">
      <span v-if="currentStep !== null" class="plan-execution-card__tag">当前步骤: {{ currentStep }}</span>
      <span v-if="nextNode" class="plan-execution-card__tag">下一节点: {{ nextNode }}</span>
    </div>

    <div v-if="executionOutput" class="plan-execution-card__section">
      <div class="plan-execution-card__label">执行输出</div>
      <pre class="plan-execution-card__text">{{ executionOutput }}</pre>
    </div>

    <div v-else-if="content" class="plan-execution-card__section">
      <div class="plan-execution-card__label">流式输出</div>
      <pre class="plan-execution-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.plan-execution-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.plan-execution-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.plan-execution-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.plan-execution-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #047857;
}

.plan-execution-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.plan-execution-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.plan-execution-card__meta {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.plan-execution-card__tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: #065f46;
}

.plan-execution-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.plan-execution-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.plan-execution-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
