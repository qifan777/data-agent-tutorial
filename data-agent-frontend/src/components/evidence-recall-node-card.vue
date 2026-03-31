<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  name?: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'EVIDENCE_RECALL_NODE')

const databaseId = computed(() => String(typedData.value.databaseId ?? ''))
const input = computed(() => String(typedData.value.input ?? ''))
const rewriteQuery = computed(() => String(typedData.value.REWRITE_QUERY ?? ''))
const evidence = computed(() => String(typedData.value.EVIDENCE ?? ''))

</script>

<template>
  <section class="evidence-card" :class="{ 'evidence-card--success': status === 'success' }">
    <div class="evidence-card__header">
      <div>
        <div class="evidence-card__eyebrow">Recall Node</div>
        <h3 class="evidence-card__title">{{ displayName }}</h3>
      </div>
      <div class="evidence-card__status">{{ status === 'success' ? '已完成' : '召回中' }}</div>
    </div>

    <div v-if="databaseId" class="evidence-card__meta">
      <span v-if="databaseId" class="evidence-card__tag">数据库: {{ databaseId }}</span>
    </div>

    <div v-if="input" class="evidence-card__section">
      <div class="evidence-card__label">用户问题</div>
      <div class="evidence-card__text">{{ input }}</div>
    </div>

    <div v-if="rewriteQuery" class="evidence-card__section">
      <div class="evidence-card__label">改写查询</div>
      <div class="evidence-card__text">{{ rewriteQuery }}</div>
    </div>

    <div v-if="evidence" class="evidence-card__section">
      <div class="evidence-card__label">召回证据</div>
      <pre class="evidence-card__evidence">{{ evidence }}</pre>
    </div>
  </section>
</template>

<style scoped>
.evidence-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.evidence-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.evidence-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.evidence-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #0284c7;
}

.evidence-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.evidence-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.evidence-card__meta {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.evidence-card__tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #ecfeff;
  border: 1px solid #bae6fd;
  color: #0c4a6e;
}

.evidence-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.evidence-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.evidence-card__text {
  margin-top: 6px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.evidence-card__evidence {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}

.evidence-card__content {
  margin-top: 14px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #334155;
}

.evidence-card__data {
  margin-top: 12px;
  border-radius: 12px;
  padding: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.6;
  color: #334155;
}
</style>
