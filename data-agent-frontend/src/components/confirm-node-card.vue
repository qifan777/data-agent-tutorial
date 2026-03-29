<script setup lang="ts">
const props = defineProps<{
  data?: Record<string, any>
  status: 'pending' | 'success'
  disabled?: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <section class="confirm-card" :class="`confirm-card--${status}`">
    <div class="confirm-card__header">
      <div>
        <div class="confirm-card__eyebrow">Human In The Loop</div>
        <h3 class="confirm-card__title">2. 人工确认节点</h3>
        <p class="confirm-card__subtitle">系统已经完成路由判断，等待人工确认后再继续往下执行。</p>
      </div>
      <div class="confirm-card__badge">
        {{ status === 'pending' ? '等待确认' : '已确认' }}
      </div>
    </div>

    <div class="confirm-card__body">
      <div class="confirm-card__label">建议分支</div>
      <div class="confirm-card__value">{{ props.data?.sceneLabel || props.data?.scene || '待确认' }}</div>
      <div class="confirm-card__hint">
        {{
          props.data?.scene === 'travel'
            ? '确认后会进入旅行分支，继续生成旅行攻略并整理成清单。'
            : '确认后会进入学习分支，继续生成学习计划并整理成清单。'
        }}
      </div>
    </div>

    <div v-if="status === 'pending'" class="confirm-card__actions">
      <el-button type="primary" :disabled="disabled" @click="emit('confirm')">确认继续</el-button>
      <el-button :disabled="disabled" @click="emit('cancel')">取消本次执行</el-button>
    </div>
  </section>
</template>

<style scoped>
.confirm-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(168, 85, 247, 0.12), transparent 30%),
    linear-gradient(180deg, #ffffff, #faf5ff);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.confirm-card--pending {
  border-color: rgba(168, 85, 247, 0.28);
}

.confirm-card--success {
  border-color: rgba(34, 197, 94, 0.22);
}

.confirm-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.confirm-card__eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #9333ea;
}

.confirm-card__title {
  margin: 8px 0 6px;
  font-size: 20px;
  color: #0f172a;
}

.confirm-card__subtitle {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.confirm-card__badge {
  flex-shrink: 0;
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(15, 23, 42, 0.06);
  font-size: 12px;
  color: #334155;
}

.confirm-card__body {
  margin-top: 16px;
  border-radius: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(147, 51, 234, 0.14);
}

.confirm-card__label {
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #64748b;
}

.confirm-card__value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.confirm-card__hint {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
}

.confirm-card__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
}
</style>
