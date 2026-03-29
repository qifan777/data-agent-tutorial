<script setup lang="ts">
import { Client, ClientFactory } from '@a2a-js/sdk/client'
import { type AgentCard } from '@a2a-js/sdk'
import { computed, markRaw, onMounted, reactive, ref, shallowRef } from 'vue'
import ConfirmNodeCard from '@/components/confirm-node-card.vue'
import {
  TOY_GRAPH_ARTIFACT_OUTPUT,
  TOY_GRAPH_MESSAGE_METADATA,
  TOY_GRAPH_NODE,
} from '@/constants/toy-graph-spec'
import RouteNodeCard from '@/components/route-node-card.vue'
import StudyPlanNodeCard from '@/components/study-plan-node-card.vue'
import TravelPlanNodeCard from '@/components/travel-plan-node-card.vue'
import WrapUpNodeCard from '@/components/wrap-up-node-card.vue'
import { api } from '@/utils/api-instance.ts'

interface ToyStep {
  id: string
  name: string
  content: string
  data?: Record<string, any>
  status: 'pending' | 'success'
}

const NODE_COMPONENTS = {
  [TOY_GRAPH_NODE.ROUTE]: markRaw(RouteNodeCard),
  [TOY_GRAPH_NODE.CONFIRM]: markRaw(ConfirmNodeCard),
  [TOY_GRAPH_NODE.TRAVEL_PLAN]: markRaw(TravelPlanNodeCard),
  [TOY_GRAPH_NODE.STUDY_PLAN]: markRaw(StudyPlanNodeCard),
  [TOY_GRAPH_NODE.WRAP_UP]: markRaw(WrapUpNodeCard),
}

const DEFAULT_EXAMPLES = [
  '帮我做一个杭州周末旅行攻略，想吃本地美食',
  '我想两周内入门 Python，请给我一个学习计划',
]

const factory = new ClientFactory()
const client = shallowRef<Client | undefined>()
const steps = reactive<ToyStep[]>([])
const userInput = ref(DEFAULT_EXAMPLES[0])
const currentTaskId = ref<string>()
const currentContextId = ref<string>()
const awaitingConfirmation = ref(false)
const upsertStep = (
  name: string,
  chunk: string,
  status: ToyStep['status'],
  data: Record<string, any> = {},
) => {
  const existing = steps.find((step) => step.name === name)
  if (existing) {
    existing.content += chunk
    existing.status = status
    existing.data = { ...existing.data, ...data }
    return
  }
  steps.push({
    id: crypto.randomUUID(),
    name,
    data: data,
    content: chunk,
    status: status,
  })
}

const isRunning = ref(false)

const orderedSteps = computed(() => {
  return [...steps].sort((a, b) => {
    const order: string[] = [
      TOY_GRAPH_NODE.ROUTE,
      TOY_GRAPH_NODE.CONFIRM,
      TOY_GRAPH_NODE.TRAVEL_PLAN,
      TOY_GRAPH_NODE.STUDY_PLAN,
      TOY_GRAPH_NODE.WRAP_UP,
    ]
    return order.indexOf(a.name) - order.indexOf(b.name)
  })
})

const resetSteps = () => {
  steps.splice(0, steps.length)
  currentTaskId.value = undefined
  currentContextId.value = undefined
  awaitingConfirmation.value = false
}

const updateStepStatus = (name: string, status: ToyStep['status']) => {
  const step = steps.find((item) => item.name === name)
  if (step) {
    step.status = status
  }
}

const streamMessage = async (
  input: string,
  keepSteps = false,
  metadata?: Record<string, unknown>,
) => {
  if (!client.value || !input || isRunning.value) return
  if (!keepSteps) {
    resetSteps()
  }
  isRunning.value = true
  try {
    const stream = client.value.sendMessageStream({
      message: {
        messageId: crypto.randomUUID(),
        role: 'user',
        kind: 'message',
        taskId: currentTaskId.value,
        contextId: currentContextId.value,
        metadata,
        parts: [{ kind: 'text', text: input }],
      },
    })
    for await (const event of stream) {
      currentTaskId.value = 'taskId' in event ? event.taskId : currentTaskId.value
      currentContextId.value = 'contextId' in event ? event.contextId : currentContextId.value

      if (event.kind === 'artifact-update') {
        const artifact = event.artifact
        const artifactName = artifact.name
        const text = artifact.parts.find((p) => p.kind === 'text')?.text || ''
        const data = artifact.parts.find((p) => p.kind === 'data')?.data

        if (artifactName && artifactName in NODE_COMPONENTS) {
          const status: ToyStep['status'] =
            artifact.metadata?.outputType == TOY_GRAPH_ARTIFACT_OUTPUT.GRAPH_NODE_FINISHED ||
              artifact.metadata?.outputType == TOY_GRAPH_ARTIFACT_OUTPUT.HUMAN_CONFIRMED
              ? 'success'
              : 'pending'
          upsertStep(artifactName, text, status, data)
        }
      }
      if (event.kind === 'status-update' && event.status.state === 'input-required') {
        awaitingConfirmation.value = true
      }
      if (event.kind === 'status-update' && event.status.state === 'completed') {
        awaitingConfirmation.value = false
      }
    }
  } finally {
    isRunning.value = false
  }
}

const handleSend = async () => {
  const input = userInput.value?.trim() ?? ''
  await streamMessage(input, false)
}

const handleConfirm = async () => {
  updateStepStatus(TOY_GRAPH_NODE.CONFIRM, 'success')
  awaitingConfirmation.value = false
  await streamMessage('确认继续', true, {
    [TOY_GRAPH_MESSAGE_METADATA.CONFIRMATION_APPROVED]: true,
    [TOY_GRAPH_MESSAGE_METADATA.CONFIRMATION_FEEDBACK]: '用户确认继续执行',
  })
}

const handleCancel = async () => {
  updateStepStatus(TOY_GRAPH_NODE.CONFIRM, 'success')
  awaitingConfirmation.value = false
  await streamMessage('取消', true, {
    [TOY_GRAPH_MESSAGE_METADATA.CONFIRMATION_APPROVED]: false,
    [TOY_GRAPH_MESSAGE_METADATA.CONFIRMATION_FEEDBACK]: '用户取消本次执行',
  })
}

onMounted(async () => {
  client.value = await factory.createFromAgentCard(
    (await api.a2acontroller.agentJson()) as AgentCard,
  )
})
</script>

<template>
  <main class="page-shell">
    <section class="hero-panel">
      <div class="hero-panel__copy">
        <div class="hero-panel__eyebrow">Graph Tutorial Demo</div>
        <h1 class="hero-panel__title">一个输入，看看它如何在分支节点里流动</h1>
        <p class="hero-panel__desc">
          这个页面会把 graph 的每个节点都展示成一张卡片。你可以很直观地看到：
          先路由，再进入不同分支，最后统一收尾。
        </p>
      </div>

      <div class="hero-panel__composer">
        <div class="hero-panel__examples">
          <button
            v-for="example in DEFAULT_EXAMPLES"
            :key="example"
            class="example-chip"
            type="button"
            @click="userInput = example"
          >
            {{ example }}
          </button>
        </div>

        <el-input
          v-model="userInput"
          :rows="4"
          type="textarea"
          resize="none"
          placeholder="输入一个旅行需求，或者一个学习目标，然后回车或点击运行"
          @keydown.enter.exact.prevent="handleSend"
        />

        <div class="hero-panel__actions">
          <el-button type="primary" size="large" :loading="isRunning" @click="handleSend">
            {{ isRunning ? '运行中...' : '运行 Graph' }}
          </el-button>
          <span class="hero-panel__hint">推荐先试试“杭州周末旅行攻略”或“Python 学习计划”。</span>
        </div>
      </div>
    </section>

    <section class="timeline-panel">
      <div class="timeline-panel__header">
        <div>
          <h2>节点时间线</h2>
          <p>流式内容会实时追加到对应节点卡片里。</p>
        </div>
        <div class="timeline-panel__status">
          {{ isRunning ? '正在执行' : orderedSteps.length ? '执行完成' : '等待开始' }}
        </div>
      </div>

      <div v-if="orderedSteps.length" class="timeline-panel__list">
        <component
          v-for="step in orderedSteps"
          :key="step.id"
          :is="NODE_COMPONENTS[step.name as keyof typeof NODE_COMPONENTS]"
          :content="step.content"
          :data="step.data"
          :status="step.status"
          :disabled="isRunning"
          @confirm="handleConfirm"
          @cancel="handleCancel"
        />
      </div>

      <div v-else class="empty-state">
        <div class="empty-state__title">还没有节点输出</div>
        <div class="empty-state__desc">
          点击上面的示例，或者自己输入一句需求，就能看到 graph 的执行过程。
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.page-shell {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(253, 224, 71, 0.24), transparent 24%),
    radial-gradient(circle at top right, rgba(56, 189, 248, 0.16), transparent 20%),
    linear-gradient(180deg, #fffdf8 0%, #f8fafc 100%);
}

.hero-panel,
.timeline-panel {
  max-width: 1080px;
  margin: 0 auto;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(14px);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.08);
}

.hero-panel {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 28px;
  padding: 28px;
}

.hero-panel__eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #ea580c;
}

.hero-panel__title {
  margin: 12px 0;
  font-size: clamp(32px, 5vw, 54px);
  line-height: 1.05;
  color: #0f172a;
}

.hero-panel__desc {
  max-width: 520px;
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #475569;
}

.hero-panel__composer {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 247, 237, 0.9), rgba(255, 255, 255, 0.96));
  border: 1px solid rgba(249, 115, 22, 0.12);
}

.hero-panel__examples {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.example-chip {
  border: none;
  border-radius: 999px;
  padding: 10px 14px;
  background: #fff;
  color: #9a3412;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.12);
}

.example-chip:hover {
  transform: translateY(-1px);
}

.hero-panel__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-panel__hint {
  font-size: 13px;
  color: #64748b;
}

.timeline-panel {
  margin-top: 24px;
  padding: 24px;
}

.timeline-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.timeline-panel__header h2 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.timeline-panel__header p {
  margin: 8px 0 0;
  color: #64748b;
}

.timeline-panel__status {
  border-radius: 999px;
  padding: 10px 14px;
  background: #e2e8f0;
  color: #334155;
  font-size: 13px;
}

.timeline-panel__list {
  display: grid;
  gap: 16px;
}

.empty-state {
  padding: 36px 20px;
  border: 1px dashed rgba(100, 116, 139, 0.35);
  border-radius: 20px;
  text-align: center;
  background: rgba(248, 250, 252, 0.86);
}

.empty-state__title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.empty-state__desc {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }
}
</style>
