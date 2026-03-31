<script setup lang="ts">
import { Client, ClientFactory } from '@a2a-js/sdk/client'
import EvidenceRecallNodeCard from '@/components/evidence-recall-node-card.vue'
import FeasibilityAssessmentNodeCard from '@/components/feasibility-assessment-node-card.vue'
import HumanFeedbackNodeCard from '@/components/human-feedback-node-card.vue'
import PlanExecutionNodeCard from '@/components/plan-execution-node-card.vue'
import PlannerNodeCard from '@/components/planner-node-card.vue'
import SchemeRecallNodeCard from '@/components/scheme-recall-node-card.vue'
import SqlExecutionNodeCard from '@/components/sql-execution-node-card.vue'
import SqlGenerationNodeCard from '@/components/sql-generation-node-card.vue'
import TableRelationNodeCard from '@/components/table-relation-node-card.vue'
import { type AgentCard } from '@a2a-js/sdk'
import { type Component, computed, markRaw, onMounted, reactive, ref, shallowRef } from 'vue'
import {
  DATA_AGENT_ARTIFACT_OUTPUT,
  DATA_AGENT_GRAPH_NODE,
  DATA_AGENT_MESSAGE_METADATA,
  DATA_AGENT_NODE_ORDER,
} from '@/constants/data-agent-graph-spec'
import { api } from '@/utils/api-instance.ts'

interface GraphStep {
  id: string
  name: string
  content: string
  data?: Record<string, unknown>
  status: 'pending' | 'success'
}

const NODE_COMPONENTS: Record<string, Component> = {
  [DATA_AGENT_GRAPH_NODE.EVIDENCE_RECALL]: markRaw(EvidenceRecallNodeCard),
  [DATA_AGENT_GRAPH_NODE.SCHEMA_RECALL]: markRaw(SchemeRecallNodeCard),
  [DATA_AGENT_GRAPH_NODE.TABLE_RELATION]: markRaw(TableRelationNodeCard),
  [DATA_AGENT_GRAPH_NODE.FEASIBILITY_ASSESSMENT]: markRaw(FeasibilityAssessmentNodeCard),
  [DATA_AGENT_GRAPH_NODE.PLANNER]: markRaw(PlannerNodeCard),
  [DATA_AGENT_GRAPH_NODE.HUMAN_FEEDBACK]: markRaw(HumanFeedbackNodeCard),
  [DATA_AGENT_GRAPH_NODE.PLAN_EXECUTION]: markRaw(PlanExecutionNodeCard),
  [DATA_AGENT_GRAPH_NODE.SQL_GENERATION]: markRaw(SqlGenerationNodeCard),
  [DATA_AGENT_GRAPH_NODE.SQL_EXECUTION]: markRaw(SqlExecutionNodeCard),
}

const DEFAULT_EXAMPLES = [
  'What is the highest percentage of K–12 students eligible for free meals among schools in Alameda County?',
  'How many schools that are exclusively virtual have an average SAT Math score greater than 400?',
]
const DATABASE_OPTIONS = [
  'california_schools',
  'toxicology',
  'european_football_2',
  'student_club',
  'debit_card_specializing',
  'card_games',
  'formula_1',
  'thrombosis_prediction',
  'codebase_community',
  'financial',
] as const

const factory = new ClientFactory()
const client = shallowRef<Client | undefined>()
const steps = reactive<GraphStep[]>([])
const userInput = ref(DEFAULT_EXAMPLES[0])
const selectedDatabase = ref<(typeof DATABASE_OPTIONS)[number]>('california_schools')
const currentTaskId = ref<string>()
const currentContextId = ref<string>()
const awaitingConfirmation = ref(false)
const confirmationApproved = ref(true)
const confirmationFeedback = ref('用户确认继续执行')
const upsertStep = (
  name: string,
  chunk: string,
  status: GraphStep['status'],
  data: Record<string, unknown> = {},
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
  return DATA_AGENT_NODE_ORDER
    .map((name) => steps.find((step) => step.name === name))
    .filter((step): step is GraphStep => Boolean(step))
})

const resetSteps = () => {
  steps.splice(0, steps.length)
  currentTaskId.value = undefined
  currentContextId.value = undefined
  awaitingConfirmation.value = false
  confirmationApproved.value = true
  confirmationFeedback.value = '用户确认继续执行'
}

const markAllPendingStepAsSuccess = () => {
  for (const step of steps) {
    if (step.status === 'pending') {
      step.status = 'success'
    }
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
        metadata: {
          [DATA_AGENT_MESSAGE_METADATA.DATABASE_ID]: selectedDatabase.value,
          ...(metadata ?? {}),
        },
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

        if (artifactName) {
          const outputType = String(artifact.metadata?.outputType ?? '')
          const status: GraphStep['status'] =
            outputType === DATA_AGENT_ARTIFACT_OUTPUT.GRAPH_NODE_FINISHED ? 'success' : 'pending'
          upsertStep(artifactName, text, status, data)
        }
      }
      if (event.kind === 'status-update' && event.status.state === 'completed') {
        awaitingConfirmation.value = false
        markAllPendingStepAsSuccess()
      }
      if (event.kind === 'status-update' && event.status.state === 'input-required') {
        awaitingConfirmation.value = true
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

const submitConfirmation = async () => {
  awaitingConfirmation.value = false
  const approved = confirmationApproved.value
  const feedback = (confirmationFeedback.value || '').trim()
  await streamMessage(feedback || (approved ? '确认继续' : '取消本次执行'), true, {
    [DATA_AGENT_MESSAGE_METADATA.CONFIRMATION_APPROVED]: approved,
    [DATA_AGENT_MESSAGE_METADATA.CONFIRMATION_FEEDBACK]:
      feedback || (approved ? '用户确认继续执行' : '用户取消本次执行'),
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
        <h1 class="hero-panel__title">一个输入，观察 Data Agent 的节点执行轨迹</h1>
        <p class="hero-panel__desc">
          页面会把后端返回的节点结果按时间顺序实时展示，不再绑定固定 demo 分支节点，
          可以直接适配新的 EVIDENCE_RECALL 节点。
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

        <div class="hero-panel__db">
          <span class="hero-panel__db-label">数据库</span>
          <el-select
            v-model="selectedDatabase"
            class="hero-panel__db-select"
            placeholder="请选择数据库"
          >
            <el-option
              v-for="database in DATABASE_OPTIONS"
              :key="database"
              :label="database"
              :value="database"
            />
          </el-select>
        </div>

        <el-input
          v-model="userInput"
          :rows="4"
          type="textarea"
          resize="none"
          placeholder="输入一个业务分析问题"
          @keydown.enter.exact.prevent="handleSend"
        />

        <div class="hero-panel__actions">
          <el-button type="primary" size="large" :loading="isRunning" @click="handleSend">
            {{ isRunning ? '运行中...' : '运行 Graph' }}
          </el-button>
          <span class="hero-panel__hint">建议先用示例问题体验 Evidence Recall 节点。</span>
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
        <template v-for="step in orderedSteps" :key="step.id">
          <component
            v-if="NODE_COMPONENTS[step.name]"
            :is="NODE_COMPONENTS[step.name]"
            :name="step.name"
            :content="step.content"
            :data="step.data"
            :status="step.status"
          />
        </template>
      </div>

      <div v-if="awaitingConfirmation" class="human-input-panel">
        <h3>需要人工输入</h3>
        <p>请填写确认结果和反馈，提交后会继续执行后续节点。</p>
        <div class="human-input-panel__field">
          <div class="human-input-panel__label">确认结果</div>
          <el-radio-group v-model="confirmationApproved">
            <el-radio :value="true">确认继续</el-radio>
            <el-radio :value="false">取消执行</el-radio>
          </el-radio-group>
        </div>
        <div class="human-input-panel__field">
          <div class="human-input-panel__label">反馈内容</div>
          <el-input
            v-model="confirmationFeedback"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="请输入反馈，将映射到 confirmationFeedback"
          />
        </div>
        <el-button type="primary" :loading="isRunning" @click="submitConfirmation">
          提交人工输入
        </el-button>
      </div>

      <div v-else-if="!orderedSteps.length" class="empty-state">
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

.hero-panel__db {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero-panel__db-label {
  font-size: 13px;
  color: #334155;
  white-space: nowrap;
}

.hero-panel__db-select {
  flex: 1;
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

.human-input-panel {
  margin-top: 18px;
  border: 1px solid rgba(168, 85, 247, 0.24);
  border-radius: 16px;
  padding: 16px;
  background:
    radial-gradient(circle at top left, rgba(168, 85, 247, 0.1), transparent 32%),
    linear-gradient(180deg, #ffffff, #faf5ff);
}

.human-input-panel h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.human-input-panel p {
  margin: 8px 0 0;
  color: #475569;
}

.human-input-panel__field {
  margin: 14px 0;
}

.human-input-panel__label {
  margin-bottom: 8px;
  font-size: 13px;
  color: #334155;
}

.node-card {
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 18px;
  background: #fff;
  padding: 16px;
}

.node-card--success {
  border-color: rgba(34, 197, 94, 0.45);
}

.node-card__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.node-card__title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.node-card__status {
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #f1f5f9;
  color: #475569;
}

.node-card__content {
  margin-top: 12px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #334155;
}

.node-card__data {
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
