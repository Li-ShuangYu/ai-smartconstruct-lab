<template>
  <div class="execute-wrapper">
    <n-spin :show="loading">
      <header class="top-bar">
        <span class="task-name">{{ info.taskName }}</span>
        <span class="step-hint" v-if="info.status === 1">{{ seqIndex + 1 }} / {{ orderedSeq.length }}</span>
      </header>

      <main class="center-area">
        <!-- 未开始 -->
        <template v-if="info.status === 0">
          <div class="start-card">
            <div class="icon-big">🚀</div>
            <h1>{{ info.taskName }}</h1>
            <p>实训任务已下发 — {{ orderedSeq.length }} 个节点待完成</p>
            <n-button type="primary" size="large" :loading="acting" @click="handleStart">开始实训</n-button>
          </div>
        </template>

        <!-- 进行中 → 动态组件 -->
        <template v-else-if="info.status === 1 && curNode">
          <component
            :is="currentComponent"
            :config="curNode"
            :task-id="taskId"
            @next="handleNext"
          />
        </template>

        <!-- 已完成 -->
        <template v-else-if="info.status === 2">
          <div class="complete-card">
            <div class="check-icon">✅</div>
            <h1>实训完成</h1>
            <p>你已完成本实训的所有节点，系统已自动提交</p>
            <n-button type="primary" @click="$router.push('/student/workbench')">返回工作台</n-button>
          </div>
        </template>
      </main>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, useMessage } from 'naive-ui'
import { getParticipation, startTraining, nextTraining } from '@/services/modules/student-dashboard.service'
import type { ParticipationInfo } from '@/services/modules/student-dashboard.service'
import { buildOrderedSequence, normalizeType, checkIsEndNode } from '@/utils/training-flow'
import GroupingNode from './flow/GroupingNode.vue'
import UploadNode from './flow/UploadNode.vue'
import ResourceNode from './flow/ResourceNode.vue'
import HomeworkNode from './flow/HomeworkNode.vue'
import GenericNode from './flow/GenericNode.vue'

const route = useRoute(); const router = useRouter(); const message = useMessage()
const loading = ref(false); const acting = ref(false)
const info = ref<ParticipationInfo>({ participationId: 0, taskId: 0, status: 0, taskName: '' })
const orderedSeq = ref<any[]>([]); const seqIndex = ref(0)
const taskId = Number(route.query.taskId) || 0

const curNode = computed(() => orderedSeq.value[seqIndex.value] || null)

/** 根据 node_type 映射到真实组件 */
const currentComponent = computed(() => {
  if (!curNode.value) return null
  const nt = normalizeType(curNode.value.node_type || curNode.value.type || '')
  if (nt.includes('GROUPING')) return markRaw(GroupingNode)
  if (nt.includes('UPLOAD') || nt.includes('PLAN') || nt.includes('REQ') || nt.includes('SOLUTION')) return markRaw(UploadNode)
  if (nt.includes('HOMEWORK') || nt.includes('EXAM')) return markRaw(HomeworkNode)
  if (nt.includes('RESOURCE') || nt.includes('THEORY') || nt.includes('READ')) return markRaw(ResourceNode)
  return markRaw(GenericNode)
})

async function loadInfo() {
  loading.value = true
  try {
    const r = await getParticipation(taskId)
    if (r.code === 200) {
      info.value = r.data
      const json = r.data.templateJson
      if (json) {
        const payload = typeof json === 'string' ? JSON.parse(json) : json
        orderedSeq.value = buildOrderedSequence(payload.nodes || [], payload.edges || [])
        seqIndex.value = Math.max(0, r.data.currentNodeIndex ?? 0)
      }
    } else message.error(r.message || '加载失败')
  } catch { message.error('加载实训数据失败') } finally { loading.value = false }
}

async function handleStart() {
  if (!info.value.participationId) { message.error('参与记录加载失败，请刷新重试'); return }
  acting.value = true
  try {
    const r = await startTraining(info.value.participationId)
    if (r.code === 200) { info.value.status = 1; seqIndex.value = 0 }
    else message.error(r.message || '启动失败')
  } catch { message.error('操作失败') } finally { acting.value = false }
}

async function handleNext() {
  if (!info.value.participationId) { message.error('参与记录加载失败'); return }
  const isEnd = curNode.value ? checkIsEndNode(curNode.value) : false
  if (isEnd) {
    acting.value = true
    try {
      const r = await nextTraining(info.value.participationId, seqIndex.value, true)
      if (r.code === 200 && r.data.completed) { info.value.status = 2; message.success('实训完成！') }
      else message.error(r.message || '提交失败')
    } catch { message.error('操作失败') } finally { acting.value = false }
    return
  }
  const nextIdx = seqIndex.value + 1
  if (nextIdx >= orderedSeq.value.length) return
  acting.value = true
  try {
    const r = await nextTraining(info.value.participationId, nextIdx, false)
    if (r.code === 200) seqIndex.value = nextIdx
  } catch { message.error('操作失败') } finally { acting.value = false }
}

onMounted(() => { if (taskId) loadInfo() })
onUnmounted(() => {})
</script>

<style scoped>
.execute-wrapper { display: flex; flex-direction: column; height: calc(100vh - 64px); background: #F8FAFC; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #FFFFFF; border-bottom: 1px solid #E2E8F0; }
.task-name { font-size: 18px; font-weight: 700; color: #0F172A; }
.step-hint { font-size: 13px; color: #64748B; font-weight: 600; }
.center-area { flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px; overflow-y: auto; }
.start-card, .complete-card { text-align: center; }
.start-card h1, .complete-card h1 { font-size: 28px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.start-card p, .complete-card p { color: #64748B; margin: 0 0 24px 0; font-size: 15px; }
.icon-big { font-size: 64px; margin-bottom: 16px; }
.check-icon { font-size: 64px; margin-bottom: 16px; }
</style>
