<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  content: string
  data?: Record<string, any>
  status: 'pending' | 'success'
}>()

const sceneLabel = computed(() => {
  return props.data?.sceneLabel || props.data?.scene || '尚未命中分支'
})

const sceneKey = computed(() => {
  return props.data?.scene || ''
})

const routeSummary = computed(() => {
  if (!sceneKey.value) return '等待路由节点返回分支判断结果...'
  return sceneKey.value === 'travel'
    ? '已识别为旅行场景，接下来会进入旅行分支。'
    : '已识别为学习场景，接下来会进入学习分支。'
})
</script>

<template>
  <section class="node-card node-card--route" :class="`node-card--${status}`">
    <div class="node-card__header">
      <div>
        <div class="node-card__eyebrow">Route Node</div>
        <h3 class="node-card__title">1. 路由节点</h3>
        <p class="node-card__subtitle">先识别用户意图，决定后续应该进入旅行分支还是学习分支。</p>
      </div>
      <div class="node-card__badge">{{ status === 'pending' ? '判断中' : '已完成' }}</div>
    </div>
    <div class="route-result">
      <div class="route-result__label">命中分支</div>
      <div class="route-result__value">{{ sceneLabel }}</div>
      <div class="route-result__summary">{{ routeSummary }}</div>
    </div>
  </section>
</template>

<style scoped>
.node-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  padding: 18px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.node-card--route {
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.12), transparent 30%),
    linear-gradient(180deg, #ffffff, #eff6ff);
}

.node-card--pending {
  border-color: rgba(249, 115, 22, 0.24);
}

.node-card--success {
  border-color: rgba(34, 197, 94, 0.22);
}

.node-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.node-card__eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #2563eb;
}

.node-card__title {
  margin: 8px 0 6px;
  font-size: 20px;
  color: #0f172a;
}

.node-card__subtitle {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.node-card__badge {
  flex-shrink: 0;
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(15, 23, 42, 0.06);
  font-size: 12px;
  color: #334155;
}

.route-result {
  border-radius: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(37, 99, 235, 0.12);
}

.route-result__label {
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #64748b;
}

.route-result__value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.route-result__summary {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
}
</style>
