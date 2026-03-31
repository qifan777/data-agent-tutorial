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
const displayName = computed(() => props.name || 'SQL_GENERATE_NODE')

const sqlText = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.SQL_GENERATION_RESULT]
  if (typeof value === 'string') return value
  if (value != null && typeof value === 'object') return JSON.stringify(value, null, 2)
  return ''
})
</script>

<template>
  <section class="sql-generation-card" :class="{ 'sql-generation-card--success': status === 'success' }">
    <div class="sql-generation-card__header">
      <div>
        <div class="sql-generation-card__eyebrow">SQL Generate</div>
        <h3 class="sql-generation-card__title">{{ displayName }}</h3>
      </div>
      <div class="sql-generation-card__status">{{ status === 'success' ? '已完成' : '生成中' }}</div>
    </div>

    <div v-if="sqlText" class="sql-generation-card__section">
      <div class="sql-generation-card__label">SQL 语句</div>
      <pre class="sql-generation-card__code">{{ sqlText }}</pre>
    </div>

    <div v-else-if="content" class="sql-generation-card__section">
      <div class="sql-generation-card__label">流式输出</div>
      <pre class="sql-generation-card__code">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.sql-generation-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(99, 102, 241, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.sql-generation-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.sql-generation-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.sql-generation-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #3730a3;
}

.sql-generation-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.sql-generation-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.sql-generation-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.sql-generation-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.sql-generation-card__code {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}
</style>
