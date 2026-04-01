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
const displayName = computed(() => props.name || 'PYTHON_ANALYZE_NODE')

const analysisText = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.EXECUTION_OUTPUT]
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value !== 'object') return String(value)

  const entries = Object.entries(value as Record<string, unknown>)
    .filter(([key, item]) => key.includes('_analysis') && typeof item === 'string')
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([, item]) => item as string)

  if (entries.length > 0) {
    return entries.join('\n\n')
  }
  return JSON.stringify(value, null, 2)
})
</script>

<template>
  <section class="python-analysis-card" :class="{ 'python-analysis-card--success': status === 'success' }">
    <div class="python-analysis-card__header">
      <div>
        <div class="python-analysis-card__eyebrow">Python Analyze</div>
        <h3 class="python-analysis-card__title">{{ displayName }}</h3>
      </div>
      <div class="python-analysis-card__status">{{ status === 'success' ? '已完成' : '分析中' }}</div>
    </div>

    <div v-if="analysisText" class="python-analysis-card__section">
      <div class="python-analysis-card__label">分析结果</div>
      <pre class="python-analysis-card__text">{{ analysisText }}</pre>
    </div>

    <div v-else-if="content" class="python-analysis-card__section">
      <div class="python-analysis-card__label">流式输出</div>
      <pre class="python-analysis-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.python-analysis-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(168, 85, 247, 0.13), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.python-analysis-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.python-analysis-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.python-analysis-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #7e22ce;
}

.python-analysis-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.python-analysis-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.python-analysis-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.python-analysis-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.python-analysis-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
