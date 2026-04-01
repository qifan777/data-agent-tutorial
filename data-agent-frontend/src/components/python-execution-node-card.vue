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
const displayName = computed(() => props.name || 'PYTHON_EXECUTE_NODE')

const executionResult = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.PYTHON_EXECUTION_RESULT]
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
})
</script>

<template>
  <section
    class="python-execution-card"
    :class="{ 'python-execution-card--success': status === 'success' }"
  >
    <div class="python-execution-card__header">
      <div>
        <div class="python-execution-card__eyebrow">Python Execute</div>
        <h3 class="python-execution-card__title">{{ displayName }}</h3>
      </div>
      <div class="python-execution-card__status">{{ status === 'success' ? '已完成' : '执行中' }}</div>
    </div>

    <div v-if="executionResult" class="python-execution-card__section">
      <div class="python-execution-card__label">执行结果</div>
      <pre class="python-execution-card__text">{{ executionResult }}</pre>
    </div>

    <div v-else-if="content" class="python-execution-card__section">
      <div class="python-execution-card__label">流式输出</div>
      <pre class="python-execution-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.python-execution-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(234, 88, 12, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.python-execution-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.python-execution-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.python-execution-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #c2410c;
}

.python-execution-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.python-execution-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.python-execution-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.python-execution-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.python-execution-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
