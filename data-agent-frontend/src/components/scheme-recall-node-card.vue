<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_RECALL } from '@/constants/data-agent-graph-spec'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

interface RecallDoc {
  id?: string
  text?: string
  score?: number
  media?: string | null
  metadata?: Record<string, unknown>
}

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})

const tableDocs = computed<RecallDoc[]>(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_RECALL.TABLE_SCHEMA]
  return Array.isArray(value) ? (value as RecallDoc[]) : []
})

const columnDocs = computed<RecallDoc[]>(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_RECALL.COLUMN_SCHEMA]
  return Array.isArray(value) ? (value as RecallDoc[]) : []
})

const sampleTableDocs = computed(() => tableDocs.value.slice(0, 5))
const sampleColumnDocs = computed(() => columnDocs.value.slice(0, 8))
const displayName = computed(() => props.name || 'SCHEME_RECALL_NODE')

const formatNumber = (value: unknown, digits = 4) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '-'
  return value.toFixed(digits)
}
</script>

<template>
  <section class="scheme-card" :class="{ 'scheme-card--success': status === 'success' }">
    <div class="scheme-card__header">
      <div>
        <div class="scheme-card__eyebrow">Schema Recall</div>
        <h3 class="scheme-card__title">{{ displayName }}</h3>
      </div>
      <div class="scheme-card__status">{{ status === 'success' ? '已完成' : '召回中' }}</div>
    </div>

    <div class="scheme-card__summary">
      <div class="scheme-card__chip">表召回: {{ tableDocs.length }}</div>
      <div class="scheme-card__chip">列召回: {{ columnDocs.length }}</div>
    </div>

    <div v-if="sampleTableDocs.length" class="scheme-card__section">
      <div class="scheme-card__label">Top Tables</div>
      <div class="scheme-card__items">
        <article v-for="(doc, index) in sampleTableDocs" :key="`table-${index}`" class="scheme-item">
          <div class="scheme-item__title">{{ doc.text || '-' }}</div>
          <div class="scheme-item__meta">
            <span class="scheme-item__tag">score: {{ formatNumber(doc.score) }}</span>
            <span class="scheme-item__tag"
              >distance: {{ formatNumber(doc.metadata?.distance) }}</span
            >
          </div>
        </article>
      </div>
    </div>

    <div v-if="sampleColumnDocs.length" class="scheme-card__section">
      <div class="scheme-card__label">Top Columns</div>
      <div class="scheme-card__items">
        <article v-for="(doc, index) in sampleColumnDocs" :key="`column-${index}`" class="scheme-item">
          <div class="scheme-item__title">{{ doc.text || '-' }}</div>
          <div class="scheme-item__meta">
            <span class="scheme-item__tag">score: {{ formatNumber(doc.score) }}</span>
            <span class="scheme-item__tag"
              >distance: {{ formatNumber(doc.metadata?.distance) }}</span
            >
          </div>
        </article>
      </div>
    </div>

    <div v-if="content" class="scheme-card__content">{{ content }}</div>
  </section>
</template>

<style scoped>
.scheme-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.scheme-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.scheme-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.scheme-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #1d4ed8;
}

.scheme-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.scheme-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.scheme-card__summary {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.scheme-card__chip {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e3a8a;
}

.scheme-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.scheme-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.scheme-card__items {
  margin-top: 8px;
  display: grid;
  gap: 8px;
}

.scheme-item {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  background: #f8fafc;
}

.scheme-item__title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.scheme-item__meta {
  margin-top: 6px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.scheme-item__tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 3px 8px;
  border: 1px solid #cbd5e1;
  color: #334155;
  background: #fff;
}

.scheme-card__content {
  margin-top: 12px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}
</style>
