<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_EXECUTION } from '@/constants/data-agent-graph-spec'

interface SqlResultSet {
  column?: string[]
  data?: Array<Record<string, unknown>>
  errorMsg?: string | null
}

interface SqlExecutionResult {
  resultSet?: SqlResultSet
  display?: Record<string, unknown>
}

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'SQL_EXECUTE_NODE')

const rawExecutionResult = computed(() => typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.SQL_EXECUTION_RESULT])

const parsedExecutionResult = computed<SqlExecutionResult | null>(() => {
  const value = rawExecutionResult.value
  if (!value) return null
  if (typeof value === 'object') return value as SqlExecutionResult
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value) as SqlExecutionResult
      return parsed && typeof parsed === 'object' ? parsed : null
    } catch {
      return null
    }
  }
  return null
})

const resultSet = computed<SqlResultSet | null>(() => parsedExecutionResult.value?.resultSet ?? null)
const columns = computed<string[]>(() => {
  const cols = resultSet.value?.column
  return Array.isArray(cols) ? cols : []
})
const rows = computed<Array<Record<string, unknown>>>(() => {
  const data = resultSet.value?.data
  return Array.isArray(data) ? data : []
})
const errorMessage = computed(() => {
  const msg = resultSet.value?.errorMsg
  return typeof msg === 'string' && msg.trim() ? msg : ''
})

const rawText = computed(() => {
  const value = rawExecutionResult.value
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
})
</script>

<template>
  <section class="sql-execution-card" :class="{ 'sql-execution-card--success': status === 'success' }">
    <div class="sql-execution-card__header">
      <div>
        <div class="sql-execution-card__eyebrow">SQL Execute</div>
        <h3 class="sql-execution-card__title">{{ displayName }}</h3>
      </div>
      <div class="sql-execution-card__status">{{ status === 'success' ? '已完成' : '执行中' }}</div>
    </div>

    <div v-if="errorMessage" class="sql-execution-card__error">
      {{ errorMessage }}
    </div>

    <div v-if="columns.length && rows.length" class="sql-execution-card__section">
      <div class="sql-execution-card__label">结果表</div>
      <div class="sql-execution-card__table-wrap">
        <table class="sql-execution-card__table">
          <thead>
            <tr>
              <th v-for="column in columns" :key="column">{{ column }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in rows" :key="`row-${index}`">
              <td v-for="column in columns" :key="`${index}-${column}`">
                {{ row[column] ?? '-' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else-if="rawText" class="sql-execution-card__section">
      <div class="sql-execution-card__label">执行结果</div>
      <pre class="sql-execution-card__text">{{ rawText }}</pre>
    </div>

    <div v-else-if="content" class="sql-execution-card__section">
      <div class="sql-execution-card__label">流式输出</div>
      <pre class="sql-execution-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.sql-execution-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.14), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.sql-execution-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.sql-execution-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.sql-execution-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #1d4ed8;
}

.sql-execution-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.sql-execution-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.sql-execution-card__error {
  margin-top: 12px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 13px;
}

.sql-execution-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.sql-execution-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.sql-execution-card__table-wrap {
  margin-top: 8px;
  overflow: auto;
}

.sql-execution-card__table {
  width: 100%;
  border-collapse: collapse;
  min-width: 420px;
}

.sql-execution-card__table th,
.sql-execution-card__table td {
  border: 1px solid #e2e8f0;
  padding: 8px;
  text-align: left;
  font-size: 12px;
  color: #334155;
  vertical-align: top;
}

.sql-execution-card__table th {
  background: #f8fafc;
  font-weight: 700;
}

.sql-execution-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
