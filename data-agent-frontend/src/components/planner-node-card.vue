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
const displayName = computed(() => props.name || 'PLANNER_NODE')
interface PlanStep {
  step?: number
  tool_to_use?: string
  tool_parameters?: Record<string, unknown>
}

interface StructuredPlan {
  thought_process?: string
  execution_plan?: PlanStep[]
}

const rawPlan = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.PLAN]
  if (typeof value === 'string') return value
  if (value && typeof value === 'object') return JSON.stringify(value)
  return ''
})

const planNextNode = computed(() => {
  const value = typedData.value[DATA_AGENT_STATE_KEY_PLANNING.NEXT_NODE]
  return typeof value === 'string' ? value : ''
})

const formattedPlan = computed(() => {
  const text = rawPlan.value.trim()
  if (!text) return ''
  try {
    return JSON.stringify(JSON.parse(text), null, 2)
  } catch {
    return text
  }
})

const parsedPlan = computed<StructuredPlan | null>(() => {
  if (!rawPlan.value.trim()) return null
  try {
    const parsed = JSON.parse(rawPlan.value) as StructuredPlan
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch {
    return null
  }
})

const thoughtProcess = computed(() => parsedPlan.value?.thought_process || '')
const executionPlan = computed<PlanStep[]>(() => {
  const plan = parsedPlan.value?.execution_plan
  return Array.isArray(plan) ? plan : []
})
</script>

<template>
  <section class="planner-card" :class="{ 'planner-card--success': status === 'success' }">
    <div class="planner-card__header">
      <div>
        <div class="planner-card__eyebrow">Planner</div>
        <h3 class="planner-card__title">{{ displayName }}</h3>
      </div>
      <div class="planner-card__status">{{ status === 'success' ? '已完成' : '规划中' }}</div>
    </div>

    <div v-if="planNextNode" class="planner-card__meta">
      <span class="planner-card__tag">下一节点: {{ planNextNode }}</span>
    </div>

    <div v-if="thoughtProcess" class="planner-card__section">
      <div class="planner-card__label">思考过程</div>
      <div class="planner-card__text">{{ thoughtProcess }}</div>
    </div>

    <div v-if="executionPlan.length" class="planner-card__section">
      <div class="planner-card__label">执行步骤</div>
      <div class="planner-card__steps">
        <article v-for="(item, index) in executionPlan" :key="`step-${index}`" class="planner-step">
          <div class="planner-step__title">Step {{ item.step ?? index + 1 }}</div>
          <div class="planner-step__tool">{{ item.tool_to_use || '-' }}</div>
          <pre v-if="item.tool_parameters" class="planner-step__params">{{
            JSON.stringify(item.tool_parameters, null, 2)
          }}</pre>
        </article>
      </div>
    </div>

    <div v-else-if="formattedPlan" class="planner-card__section">
      <div class="planner-card__label">执行计划</div>
      <pre class="planner-card__text">{{ formattedPlan }}</pre>
    </div>

    <div v-else-if="content" class="planner-card__section">
      <div class="planner-card__label">流式输出</div>
      <pre class="planner-card__text">{{ content }}</pre>
    </div>
  </section>
</template>

<style scoped>
.planner-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(139, 92, 246, 0.12), transparent 34%),
    linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
}

.planner-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.planner-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.planner-card__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #6d28d9;
}

.planner-card__title {
  margin: 6px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.planner-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 12px;
  background: #e2e8f0;
  color: #334155;
}

.planner-card__meta {
  margin-top: 12px;
}

.planner-card__tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #f5f3ff;
  border: 1px solid #ddd6fe;
  color: #5b21b6;
}

.planner-card__section {
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #ffffff;
}

.planner-card__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #475569;
}

.planner-card__text {
  margin-top: 8px;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
  font-size: 13px;
}

.planner-card__steps {
  margin-top: 8px;
  display: grid;
  gap: 8px;
}

.planner-step {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  background: #f8fafc;
}

.planner-step__title {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.planner-step__tool {
  margin-top: 4px;
  font-size: 13px;
  color: #5b21b6;
}

.planner-step__params {
  margin-top: 6px;
  white-space: pre-wrap;
  font-size: 12px;
  line-height: 1.6;
  color: #334155;
}
</style>
