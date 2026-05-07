<template>
  <div class="execute-wrapper">
    <n-spin :show="loading">
      <!-- 顶部任务栏 -->
      <header class="top-bar">
        <span class="task-name">{{ info.taskName }}</span>
        <span class="step-hint" v-if="info.status === 1">节点 {{ nodeIndex + 1 }} / {{ totalNodes }}</span>
      </header>

      <!-- 中央内容 -->
      <main class="center-area">
        <!-- 未开始 -->
        <template v-if="info.status === 0">
          <div class="start-card">
            <h1>{{ info.taskName }}</h1>
            <p>实训任务已下发，点击下方按钮开始</p>
            <n-button type="primary" size="large" :loading="acting" @click="handleStart">开始实训</n-button>
          </div>
        </template>

        <!-- 进行中 -->
        <template v-else-if="info.status === 1">
          <div class="node-card">
            <div class="node-icon-large">{{ currentNode?.name?.charAt(0) || '📋' }}</div>
            <h1 class="node-name">{{ currentNode?.name || '加载中...' }}</h1>
            <p class="node-desc" v-if="currentNode?.config">
              {{ formatConfig(currentNode.config) }}
            </p>
          </div>
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

      <!-- 底部操作栏 -->
      <footer v-if="info.status === 1" class="bottom-bar">
        <n-button type="primary" size="large" :loading="acting" @click="handleNext">
          {{ isLastNode ? '结束并提交实训' : '下一步' }}
        </n-button>
      </footer>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, useMessage } from 'naive-ui'
import { getParticipation, startTraining, nextTraining } from '@/services/modules/student-dashboard.service'
import type { ParticipationInfo } from '@/services/modules/student-dashboard.service'

const route = useRoute(); const router = useRouter(); const message = useMessage()
const loading = ref(false); const acting = ref(false)
const info = ref<ParticipationInfo>({ participationId: 0, taskId: 0, status: 0, taskName: '' })
const nodeIndex = ref(0); const nodes = ref<any[]>([]); const totalNodes = ref(0)

const taskId = Number(route.query.taskId) || Number(route.params.taskId) || 0

const currentNode = computed(() => nodes.value[nodeIndex.value] || null)
const isLastNode = computed(() => nodeIndex.value >= totalNodes.value - 1)

function parseTemplateJson(json: any) {
  if (!json) { nodes.value = []; totalNodes.value = 0; return }
  const data = typeof json === 'string' ? JSON.parse(json) : json
  nodes.value = data?.nodes || []
  totalNodes.value = nodes.value.length
}

function formatConfig(config: Record<string, unknown>): string {
  if (!config) return ''
  const parts: string[] = []
  for (const [k, v] of Object.entries(config)) {
    if (v !== undefined && v !== '' && v !== null) parts.push(`${k}: ${v}`)
  }
  return parts.join(' | ') || '无额外配置'
}

async function loadInfo() {
  loading.value = true
  try {
    const r = await getParticipation(taskId)
    if (r.code === 200) {
      info.value = r.data
      parseTemplateJson(r.data.templateJson)
      nodeIndex.value = r.data.currentNodeIndex ?? 0
    } else { message.error(r.message || '加载失败') }
  } catch { message.error('加载实训数据失败') } finally { loading.value = false }
}

async function handleStart() {
  acting.value = true
  try {
    const r = await startTraining(info.value.participationId)
    if (r.code === 200) {
      info.value.status = 1; nodeIndex.value = r.data.currentNodeIndex
    } else message.error(r.message || '启动失败')
  } catch { message.error('操作失败') } finally { acting.value = false }
}

async function handleNext() {
  acting.value = true
  try {
    const r = await nextTraining(info.value.participationId)
    if (r.code === 200) {
      if (r.data.completed) {
        info.value.status = 2; message.success('实训完成！')
      } else {
        nodeIndex.value = r.data.currentNodeIndex
      }
    } else message.error(r.message || '操作失败')
  } catch { message.error('操作失败') } finally { acting.value = false }
}

onMounted(() => { if (taskId) loadInfo() })
</script>

<style scoped>
.execute-wrapper { display: flex; flex-direction: column; height: calc(100vh - 64px); background: #F8FAFC; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #FFFFFF; border-bottom: 1px solid #E2E8F0; }
.task-name { font-size: 18px; font-weight: 700; color: #0F172A; }
.step-hint { font-size: 13px; color: #64748B; font-weight: 600; }
.center-area { flex: 1; display: flex; align-items: center; justify-content: center; padding: 40px; }
.start-card, .complete-card { text-align: center; }
.start-card h1, .complete-card h1 { font-size: 28px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.start-card p, .complete-card p { color: #64748B; margin: 0 0 24px 0; font-size: 15px; }
.check-icon { font-size: 64px; margin-bottom: 16px; }
.node-card { text-align: center; background: #FFFFFF; border-radius: 16px; padding: 48px; max-width: 600px; width: 100%; border: 1px solid #E2E8F0; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.node-icon-large { font-size: 48px; margin-bottom: 16px; }
.node-name { font-size: 32px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.node-desc { font-size: 14px; color: #64748B; line-height: 1.6; }
.bottom-bar { display: flex; justify-content: center; padding: 20px; background: #FFFFFF; border-top: 1px solid #E2E8F0; }
</style>
