<script setup lang="ts">
defineProps<{
  content: string
  status: 'pending' | 'success'
}>()
</script>

<template>
  <section class="node-card node-card--wrap" :class="`node-card--${status}`">
    <div class="node-card__header">
      <div>
        <div class="node-card__eyebrow">Wrap Up Node</div>
        <h3 class="node-card__title">3. 收尾节点</h3>
        <p class="node-card__subtitle">无论前面走哪条分支，最后都会在这里整理成更好理解的行动清单。</p>
      </div>
      <div class="node-card__badge">{{ status === 'pending' ? '整理中' : '已收尾' }}</div>
    </div>
    <pre class="node-card__content">{{ content || '等待最终整理结果...' }}</pre>
  </section>
</template>

<style scoped>
.node-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.node-card--wrap {
  background:
    radial-gradient(circle at top left, rgba(244, 114, 182, 0.12), transparent 28%),
    linear-gradient(180deg, #ffffff, #fdf2f8);
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
  color: #db2777;
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
