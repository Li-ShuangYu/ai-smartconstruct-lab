<template>
  <div class="engine-wrapper">
    <n-spin :show="loading">
      <header class="top-bar" v-if="state"><span class="task-step">{{ state.nodeName || state.nodeType }}</span><span class="progress" v-if="progress">节点 {{ progress }}</span></header>
      <main class="center">
        <template v-if="!state"><n-empty description="实训数据加载中..." /></template>
        <template v-else-if="state.nodeType && compMap[state.nodeType]">
          <component :is="compMap[state.nodeType]" :config="state.config" :context="ctx" @step-complete="handleStepComplete" @update-data="onDataUpdate" />
        </template>
        <template v-else><n-card class="fallback"><h3>{{ state.nodeName }}</h3><p>{{ formatConfig(state.config) }}</p><n-button type="primary" @click="handleStepComplete()">下一步</n-button></n-card></template>
      </main>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NSpin, NEmpty, useMessage } from 'naive-ui'
import { getCurrentState, proceedNextNode } from '@/services/modules/training.service'
import type { TrainingState } from '@/services/modules/training.service'
import StartPortal from './StartPortal.vue'
import GroupBuilder from './GroupBuilder.vue'
import ResourceViewer from './ResourceViewer.vue'
import AIStudyCard from './AIStudyCard.vue'
import SimulatedIDE from './SimulatedIDE.vue'
import TaskBoard from './TaskBoard.vue'
import RequirementCloud from './RequirementCloud.vue'
import PlanUpload from './PlanUpload.vue'
import HomeworkEngine from './HomeworkEngine.vue'
import ExamEngine from './ExamEngine.vue'
import MindMapPreview from './MindMapPreview.vue'
import MindMapEditor from './MindMapEditor.vue'
import KnowledgeHeatmap from './KnowledgeHeatmap.vue'
import PeerReview from './PeerReview.vue'
import GroupReview from './GroupReview.vue'
import TeacherDashboard from './TeacherDashboard.vue'
import SummaryReport from './SummaryReport.vue'

const route = useRoute(); const router = useRouter(); const message = useMessage()
const loading = ref(false); const state = ref<TrainingState | null>(null)
const participationId = Number(route.query.participationId) || Number(route.params.participationId) || 0
const ctx = computed(() => ({ isGrouped: false, isLeader: false, groupId: '', participationId, nodeId: state.value?.currentNodeId || '' }))

const compMap: Record<string, any> = {
  START: markRaw(StartPortal), GROUPING: markRaw(GroupBuilder),
  RESOURCE_READ: markRaw(ResourceViewer), THEORY_CLASS: markRaw(AIStudyCard),
  CODING_CLASS: markRaw(SimulatedIDE), TASK_DISTRIBUTE: markRaw(TaskBoard),
  REQ_UPLOAD: markRaw(RequirementCloud), PLAN_UPLOAD: markRaw(PlanUpload), SOLUTION_UPLOAD: markRaw(PlanUpload),
  HOMEWORK: markRaw(HomeworkEngine), EXAM: markRaw(ExamEngine),
  MINDMAP_PREVIEW: markRaw(MindMapPreview), MINDMAP_DRAW: markRaw(MindMapEditor),
  KNOWLEDGE_EVALUATION: markRaw(KnowledgeHeatmap),
  STUDENT_PEER_REVIEW: markRaw(PeerReview), GROUP_PEER_REVIEW: markRaw(GroupReview),
  TEACHER_EVAL: markRaw(TeacherDashboard), END: markRaw(SummaryReport)
}

const progress = computed(() => {
  if (!state.value?.allNodes) return ''
  const idx = state.value.allNodes.findIndex((n: any) => n.node_id === state.value!.currentNodeId)
  return idx >= 0 ? `${idx + 1} / ${state.value.allNodes.length}` : ''
})

function formatConfig(cfg: Record<string, any> | null): string {
  if (!cfg) return '无配置'
  return Object.entries(cfg).filter(([,v]) => v != null).map(([k,v]) => `${k}: ${typeof v === 'object' ? JSON.stringify(v) : v}`).join('；')
}

function normalizeType(t: string) { return (t || '').toUpperCase().replace(/[ _-]/g, '_') }

async function applyState(data: TrainingState) {
  state.value = data
  if (data.nodeType) {
    const nt = normalizeType(data.nodeType)
    if (!compMap[nt]) { for (const k of Object.keys(compMap)) { if (nt.includes(k)) { state.value.nodeType = k; break } } }
    else { state.value.nodeType = nt }
  }
}

async function loadState() {
  if (!participationId) return
  loading.value = true
  try { const r = await getCurrentState(participationId); if (r.code === 200 && r.data) applyState(r.data); else message.error(r.message || '加载失败') }
  catch { message.error('加载实训状态失败') } finally { loading.value = false }
}

async function handleStepComplete() {
  if (!state.value?.currentNodeId || !participationId) return
  if (normalizeType(state.value.nodeType).includes('END')) { message.success('实训完成！'); router.push('/student/workbench'); return }
  loading.value = true
  try { const r = await proceedNextNode(participationId, state.value.currentNodeId); if (r.code === 200 && r.data) applyState(r.data); else message.error(r.message || '推进失败') }
  catch { message.error('操作失败') } finally { loading.value = false }
}

function onDataUpdate(_data: any) {}
onMounted(() => loadState())
</script>
<style scoped>
.engine-wrapper { display: flex; flex-direction: column; height: calc(100vh - 64px); background: #F8FAFC; }
.top-bar { display: flex; justify-content: space-between; padding: 14px 24px; background: #fff; border-bottom: 1px solid #E2E8F0; }
.task-step { font-weight: 700; font-size: 17px; color: #0F172A; }
.progress { font-size: 13px; color: #64748B; }
.center { flex: 1; overflow-y: auto; display: flex; align-items: center; justify-content: center; }
.fallback { text-align: center; padding: 40px; } .fallback h3 { margin: 0 0 12px 0; font-size: 20px; } .fallback p { color: #64748B; margin-bottom: 20px; }
</style>
