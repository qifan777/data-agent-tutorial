<script setup lang="ts">
defineProps<{
  content: string
  status: 'pending' | 'success'
}>()
</script>

<template>
  <section class="node-card node-card--travel" :class="`node-card--${status}`">
    <div class="node-card__header">
      <div>
        <div class="node-card__eyebrow">Travel Branch</div>
        <h3 class="node-card__title">2A. 旅行分支</h3>
        <p class="node-card__subtitle">用户被识别为旅行场景后，这个节点会持续流式生成旅行建议。</p>
      </div>
      <div class="node-card__badge">{{ status === 'pending' ? '攻略生成中' : '攻略完成' }}</div>
    </div>
    <pre class="node-card__content">{{ content || '等待旅行攻略输出...' }}</pre>
  </section>
</template>

<style scoped>
.node-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.node-card--travel {
  background:
    radial-gradient(circle at top right, rgba(251, 191, 36, 0.18), transparent 32%),
    linear-gradient(180deg, #ffffff, #fffbeb);
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
  color: #d97706;
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

.node-card__content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'SF Mono', 'Roboto Mono', monospace;
  font-size: 14px;
  line-height: 1.7;
  color: #1e293b;
}
</style>
