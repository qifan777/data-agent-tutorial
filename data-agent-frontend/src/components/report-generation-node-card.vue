<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_EXECUTION } from '@/constants/data-agent-graph-spec'
import MarkdownMessage from '@/components/markdown-message.vue'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'REPORT_GENERATOR_NODE')

const reportText = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.REPORT_RESULT]
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
})

const reportObject = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.REPORT_RESULT]
  if (value != null && typeof value === 'object') return JSON.stringify(value, null, 2)
  return ''
})
</script>

<template>
  <section
    class="report-generation-card"
    :class="{ 'report-generation-card--success': status === 'success' }"
  >
    <div class="report-generation-card__header">
      <div>
        <div class="report-generation-card__eyebrow">Report Generate</div>
        <h3 class="report-generation-card__title">{{ displayName }}</h3>
      </div>
      <div class="report-generation-card__status">{{ status === 'success' ? '已完成' : '生成中' }}</div>
    </div>
    <div v-if="content" class="report-generation-card__section">
      <div class="report-generation-card__label">流式输出</div>
      <MarkdownMessage :id="`${displayName}-stream`" :message="content" />
    </div>
  </section>
</template>

<style scoped>
.report-generation-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.report-generation-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.report-generation-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.report-generation-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #0369a1;
}

.report-generation-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.report-generation-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.report-generation-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.report-generation-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.report-generation-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
