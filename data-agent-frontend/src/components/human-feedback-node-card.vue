<script setup lang="ts">
import { computed } from 'vue'
import { DATA_AGENT_STATE_KEY_HUMAN_REVIEW } from '@/constants/data-agent-graph-spec'

const props = defineProps<{
  name?: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}>()

const typedData = computed<Record<string, unknown>>(() => props.data ?? {})
const displayName = computed(() => props.name || 'HUMAN_FEEDBACK_NODE')

const approved = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_HUMAN_REVIEW.CONFIRMATION_APPROVED]
  return typeof value === 'boolean' ? value : null
})

const feedback = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_HUMAN_REVIEW.CONFIRMATION_FEEDBACK]
  return typeof value === 'string' ? value : ''
})

const nextNode = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_HUMAN_REVIEW.NEXT_NODE]
  return typeof value === 'string' ? value : ''
})
</script>

<template>
  <section class="human-card" :class="{ 'human-card--success': status === 'success' }">
    <div class="human-card__header">
      <div>
        <div class="human-card__eyebrow">Human Review</div>
        <h3 class="human-card__title">{{ displayName }}</h3>
      </div>
      <div class="human-card__status">{{ status === 'success' ? '已完成' : '等待确认' }}</div>
    </div>

    <div v-if="nextNode" class="human-card__meta">
      <span class="human-card__tag">下一节点: {{ nextNode }}</span>
    </div>

    <div v-if="approved !== null" class="human-card__section">
      <div class="human-card__label">确认结果</div>
      <div class="human-card__text">{{ approved ? '确认继续执行' : '取消执行' }}</div>
    </div>

    <div v-if="feedback" class="human-card__section">
      <div class="human-card__label">反馈内容</div>
      <pre class="human-card__text">{{ feedback }}</pre>
    </div>

    <div v-else-if="content" class="human-card__section">
      <div class="human-card__label">流式输出</div>
      <pre class="human-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.human-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(217, 70, 239, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.human-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.human-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.human-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #a21caf;
}

.human-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.human-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.human-card__meta {
  margin-top: 12px;
}

.human-card__tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #fdf4ff;
  border: 1px solid #f5d0fe;
  color: #86198f;
}

.human-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.human-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.human-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}
</style>
