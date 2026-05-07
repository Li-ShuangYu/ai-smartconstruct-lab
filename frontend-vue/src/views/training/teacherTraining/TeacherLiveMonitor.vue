<template>
  <div class="live-monitor">
    <n-spin :show="loading">
      <header class="top-bar">
        <div class="bar-left"><h2>{{ taskName || '课中实训监控' }}</h2><n-tag v-if="onlineCount>0" type="success" size="small">{{ onlineCount }} 人在线</n-tag></div>
        <n-steps v-if="allNodes && allNodes.length" :current="currentStep" size="small" style="flex:1;margin:0 40px"><n-step v-for="(n,i) in allNodes" :key="n.node_id" :title="n.name" :status="i<currentStep?'finish':i===currentStep?'process':'wait'" /></n-steps>
        <div class="bar-right"><n-button type="primary" :loading="advancing" @click="handleNextStage">进入下一环节</n-button></div>
      </header>
      <main class="main-area">
        <template v-if="!state"><n-empty description="等待教师推进第一个环节..." /></template>
        <template v-else-if="state.nodeType && compMap[state.nodeType]">
          <component :is="compMap[state.nodeType]" :config="state.config" :live-data="liveDataPool" @intervene="handleIntervene" />
        </template>
        <template v-else><n-card class="fallback"><h3>{{ state.nodeName }}</h3><p>{{ formatConfig(state.config) }}</p></n-card></template>
      </main>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, markRaw } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NCard, NSpin, NEmpty, NTag, NSteps, NStep, useMessage } from 'naive-ui'
import { TrainingWSClient } from '@/utils/websocket'
import http from '@/services/api'
import StartMonitor from './StartMonitor.vue'
import GroupMonitor from './GroupMonitor.vue'
import ResourceMonitor from './ResourceMonitor.vue'
import AIStudyMonitor from './AIStudyMonitor.vue'
import IDEMonitor from './IDEMonitor.vue'
import TaskMonitor from './TaskMonitor.vue'
import RequirementMonitor from './RequirementMonitor.vue'
import PlanMonitor from './PlanMonitor.vue'
import HomeworkMonitor from './HomeworkMonitor.vue'
import ExamMonitor from './ExamMonitor.vue'
import MindMapPreviewMonitor from './MindMapPreviewMonitor.vue'
import MindMapEditorMonitor from './MindMapEditorMonitor.vue'
import KnowledgeEvalMonitor from './KnowledgeEvalMonitor.vue'
import PeerReviewMonitor from './PeerReviewMonitor.vue'
import GroupReviewMonitor from './GroupReviewMonitor.vue'
import TeacherEvalDashboard from './TeacherEvalDashboard.vue'
import SummaryReportMonitor from './SummaryReportMonitor.vue'

const route = useRoute(); const message = useMessage()
const loading = ref(false); const advancing = ref(false)
const taskId = Number(route.query.taskId) || 0
const taskName = ref(''); const onlineCount = ref(0)
const allNodes = ref<any[]>([]); const currentStep = ref(0)
const state = ref<{ nodeType?: string; nodeName?: string; config?: any; currentNodeId?: string } | null>(null)
const liveDataPool = ref<Record<string, any>>({})
let wsClient: TrainingWSClient | null = null

const compMap: Record<string, any> = {
  START: markRaw(StartMonitor), GROUPING: markRaw(GroupMonitor),
  RESOURCE_READ: markRaw(ResourceMonitor), THEORY_CLASS: markRaw(AIStudyMonitor),
  CODING_CLASS: markRaw(IDEMonitor), TASK_DISTRIBUTE: markRaw(TaskMonitor),
  REQ_UPLOAD: markRaw(RequirementMonitor), PLAN_UPLOAD: markRaw(PlanMonitor), SOLUTION_UPLOAD: markRaw(PlanMonitor),
  HOMEWORK: markRaw(HomeworkMonitor), EXAM: markRaw(ExamMonitor),
  MINDMAP_PREVIEW: markRaw(MindMapPreviewMonitor), MINDMAP_DRAW: markRaw(MindMapEditorMonitor),
  KNOWLEDGE_EVALUATION: markRaw(KnowledgeEvalMonitor),
  STUDENT_PEER_REVIEW: markRaw(PeerReviewMonitor), GROUP_PEER_REVIEW: markRaw(GroupReviewMonitor),
  TEACHER_EVAL: markRaw(TeacherEvalDashboard), END: markRaw(SummaryReportMonitor)
}

function normalizeType(t: string) { return (t || '').toUpperCase().replace(/[ _-]/g, '_') }

async function fetchInClassState() {
  if (!taskId) return
  try {
    const r = await http.get(`/api/student/training/in-class/${taskId}/current-state`)
    if (r.data.code === 200 && r.data.data) {
      const d = r.data.data
      taskName.value = d.taskName || ''
      state.value = { nodeType: d.nodeType, nodeName: d.nodeName, config: d.config, currentNodeId: d.currentNodeId }
      allNodes.value = d.allNodes || []
      const idx = (allNodes.value as any[]).findIndex((n: any) => n.node_id === d.currentNodeId)
      currentStep.value = Math.max(0, idx)
      if (state.value.nodeType) {
        const nt = normalizeType(state.value.nodeType)
        if (!compMap[nt]) { for (const k of Object.keys(compMap)) { if (nt.includes(k)) { state.value.nodeType = k; break } } }
        else { state.value.nodeType = nt }
      }
    }
  } catch { /* ignore */ }
}

// 推进下一环节
async function handleNextStage() {
  if (!taskId || !allNodes.value.length) return
  const curId = state.value?.currentNodeId
  const curIdx = allNodes.value.findIndex((n: any) => n.node_id === curId)
  const nextNode = curIdx >= 0 && curIdx < allNodes.value.length - 1 ? allNodes.value[curIdx + 1] : null
  if (!nextNode) { message.warning('已是最后一个环节'); return }
  advancing.value = true
  try {
    const r = await http.post(`/api/teacher/training/${taskId}/next-node`, { nextNodeId: nextNode.node_id })
    if (r.data.code === 200) {
      // 后端广播 STAGE_CHANGED 后，本教师端也会通过 WS 收到然后自动刷新
      await fetchInClassState()
    } else message.error(r.data.message || '推进失败')
  } catch { message.error('操作失败') } finally { advancing.value = false }
}

// 干预事件
function handleIntervene(actionType: string, payload: any) { message.info(`干预: ${actionType}`) }

function formatConfig(cfg: any) { return cfg ? JSON.stringify(cfg) : '' }

onMounted(async () => {
  await fetchInClassState()
  const token = localStorage.getItem('auth_token') || ''
  wsClient = new TrainingWSClient(`ws://localhost:8080/ws/training?token=${token}&taskId=${taskId}`)
  try {
    await wsClient.connect()
    wsClient.on('STAGE_CHANGED', () => { fetchInClassState() })
    wsClient.on('NODE_PROGRESS', (data: any) => {
      if (data && data.studentId) {
        liveDataPool.value = { ...liveDataPool.value, [`progress_${data.studentId}`]: data }
      }
    })
  } catch { message.warning('WebSocket 连接失败') }
})
onUnmounted(() => { if (wsClient) wsClient.close() })
</script>

<style scoped>
.live-monitor { display: flex; flex-direction: column; height: calc(100vh - 64px); background: #F8FAFC; }
.top-bar { display: flex; align-items: center; padding: 12px 24px; background: #fff; border-bottom: 1px solid #E2E8F0; gap: 16px; }
.bar-left { display: flex; align-items: center; gap: 12px; }
.bar-left h2 { font-size: 18px; font-weight: 800; color: #0F172A; margin: 0; }
.bar-right { display: flex; gap: 8px; }
.main-area { flex: 1; overflow-y: auto; }
.fallback { text-align: center; padding: 40px; }
</style>
