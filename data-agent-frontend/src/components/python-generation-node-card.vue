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
const displayName = computed(() => props.name || 'PYTHON_GENERATE_NODE')

const pythonCode = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_EXECUTION.PYTHON_GENERATION_RESULT]
  if (typeof value === 'string') return value
  if (value != null && typeof value === 'object') return JSON.stringify(value, null, 2)
  return ''
})
</script>

<template>
  <section
    class="python-generation-card"
    :class="{ 'python-generation-card--success': status === 'success' }"
  >
    <div class="python-generation-card__header">
      <div>
        <div class="python-generation-card__eyebrow">Python Generate</div>
        <h3 class="python-generation-card__title">{{ displayName }}</h3>
      </div>
      <div class="python-generation-card__status">{{ status === 'success' ? '已完成' : '生成中' }}</div>
    </div>

    <div v-if="pythonCode" class="python-generation-card__section">
      <div class="python-generation-card__label">Python 代码</div>
      <pre class="python-generation-card__code">{{ pythonCode }}</pre>
    </div>

    <div v-else-if="content" class="python-generation-card__section">
      <div class="python-generation-card__label">流式输出</div>
      <pre class="python-generation-card__code">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.python-generation-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(16, 185, 129, 0.13), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.python-generation-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.python-generation-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.python-generation-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #047857;
}

.python-generation-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.python-generation-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.python-generation-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.python-generation-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.python-generation-card__code {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}
</style>
