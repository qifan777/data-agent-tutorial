<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_RECALL } from '@/constants/data-agent-graph-spec'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

interface TableColumn {
  name?: string
  type?: string
  isPrimaryKey?: boolean
}

interface TableSchema {
  name?: string
  columns?: TableColumn[]
}

interface ForeignKeyColumn {
  name?: string
  dbTable?: {
    name?: string
  }
}

interface ForeignKeySchema {
  sourceColumn?: ForeignKeyColumn
  targetColumn?: ForeignKeyColumn
}

interface RelationPayload {
  databaseId?: string
  dbTables?: TableSchema[]
  dbForeignKeys?: ForeignKeySchema[]
}

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'TABLE_RELATION_NODE')

const relationPayload = computed<RelationPayload>(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_RECALL.TABLE_RELATION]
  if (value && typeof value === 'object') {
    return value as RelationPayload
  }
  return typedData.value as RelationPayload
})

const databaseId = computed(() => String(relationPayload.value.databaseId ?? typedData.value.databaseId ?? ''))
const tables = computed<TableSchema[]>(() =>
  Array.isArray(relationPayload.value.dbTables) ? relationPayload.value.dbTables : [],
)
const foreignKeys = computed<ForeignKeySchema[]>(() =>
  Array.isArray(relationPayload.value.dbForeignKeys) ? relationPayload.value.dbForeignKeys : [],
)

const previewTables = computed(() => tables.value.slice(0, 8))
const previewForeignKeys = computed(() => foreignKeys.value.slice(0, 10))

const tableColumnPreview = (table: TableSchema) => {
  const columns = Array.isArray(table.columns) ? table.columns : []
  const names = columns.map((col) => col.name).filter(Boolean).slice(0, 6)
  return names.join(', ')
}

const foreignKeyExpression = (fk: ForeignKeySchema) => {
  const sourceTable = fk.sourceColumn?.dbTable?.name || '?'
  const sourceColumn = fk.sourceColumn?.name || '?'
  const targetTable = fk.targetColumn?.dbTable?.name || '?'
  const targetColumn = fk.targetColumn?.name || '?'
  return `${sourceTable}.${sourceColumn} -> ${targetTable}.${targetColumn}`
}
</script>

<template>
  <section class="relation-card" :class="{ 'relation-card--success': status === 'success' }">
    <div class="relation-card__header">
      <div>
        <div class="relation-card__eyebrow">Table Relation</div>
        <h3 class="relation-card__title">{{ displayName }}</h3>
      </div>
      <div class="relation-card__status">{{ status === 'success' ? '已完成' : '分析中' }}</div>
    </div>

    <div class="relation-card__summary">
      <div v-if="databaseId" class="relation-card__chip">DB: {{ databaseId }}</div>
      <div class="relation-card__chip">表: {{ tables.length }}</div>
      <div class="relation-card__chip">外键: {{ foreignKeys.length }}</div>
    </div>

    <div v-if="previewTables.length" class="relation-card__section">
      <div class="relation-card__label">Selected Tables</div>
      <div class="relation-card__items">
        <article v-for="(table, index) in previewTables" :key="`table-${index}`" class="relation-item">
          <div class="relation-item__title">{{ table.name || '-' }}</div>
          <div class="relation-item__meta">列数: {{ table.columns?.length ?? 0 }}</div>
          <div v-if="tableColumnPreview(table)" class="relation-item__columns">
            {{ tableColumnPreview(table) }}
          </div>
        </article>
      </div>
    </div>

    <div v-if="previewForeignKeys.length" class="relation-card__section">
      <div class="relation-card__label">Foreign Keys</div>
      <ul class="relation-card__list">
        <li v-for="(fk, index) in previewForeignKeys" :key="`fk-${index}`">
          {{ foreignKeyExpression(fk) }}
        </li>
      </ul>
    </div>

    <div v-if="content" class="relation-card__content">{{ content }}</div>
  </section>
</template>

<style scoped>
.relation-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.14), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.relation-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.relation-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.relation-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #047857;
}

.relation-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.relation-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.relation-card__summary {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.relation-card__chip {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: #065f46;
}

.relation-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.relation-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.relation-card__items {
  margin-top: 8px;
  display: grid;
  gap: 8px;
}

.relation-item {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  background: #f8fafc;
}

.relation-item__title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.relation-item__meta {
  margin-top: 4px;
  font-size: 12px;
  color: #475569;
}

.relation-item__columns {
  margin-top: 6px;
  font-size: 13px;
  color: #334155;
  line-height: 1.7;
}

.relation-card__list {
  margin: 8px 0 0;
  padding-left: 18px;
  display: grid;
  gap: 6px;
  color: #334155;
  line-height: 1.7;
}

.relation-card__content {
  margin-top: 12px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}
</style>
