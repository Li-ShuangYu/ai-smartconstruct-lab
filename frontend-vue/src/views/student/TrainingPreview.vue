<template>
  <div class="preview-wrapper">
    <n-spin :show="loading">
      <header class="preview-header">
        <h1>{{ info.taskName }}</h1>
        <p>实训流程预览 — 共 {{ orderedNodes.length }} 个节点</p>
      </header>

      <main class="timeline-area">
        <n-timeline v-if="orderedNodes.length > 0">
          <n-timeline-item v-for="(node, idx) in orderedNodes" :key="node.node_id"
            :type="idx === 0 ? 'success' : idx === orderedNodes.length - 1 ? 'warning' : 'info'"
            :title="`${idx + 1}. ${node.name || node.node_id}`"
            :time="nodeLabel(node.node_type)"
          >
            <p class="node-config">{{ formatConfig(node.config) }}</p>
          </n-timeline-item>
        </n-timeline>
        <n-empty v-else description="无法解析实训流程" />
      </main>

      <footer class="preview-footer">
        <n-button @click="$router.back()">返回</n-button>
        <n-button type="primary" size="large" :loading="starting" @click="handleStart">
          开始实训
        </n-button>
      </footer>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, NTimeline, NTimelineItem, NEmpty, useMessage } from 'naive-ui'
import { getParticipation, startTraining } from '@/services/modules/student-dashboard.service'
import type { ParticipationInfo } from '@/services/modules/student-dashboard.service'
import { buildOrderedSequence } from '@/utils/training-flow'

const route = useRoute(); const router = useRouter(); const message = useMessage()
const loading = ref(false); const starting = ref(false)
const info = ref<ParticipationInfo>({ participationId: 0, taskId: 0, status: 0, taskName: '' })
const orderedNodes = ref<any[]>([])

const taskId = Number(route.query.taskId) || 0

const nodeTypeLabels: Record<string, string> = {
  start: '流程起点', end: '流程终点',
  grouping: '学生分组', resource_read: '资源阅读', theory_class: '理论实训', coding_class: '编码实训',
  task_distribute: '任务下发', req_upload: '需求上传', plan_upload: '方案上传', solution_upload: '方案上传',
  homework: '课后作业', exam: '考核考试',
  mindmap_preview: '思维导图预习', mindmap_draw: '思维导图绘制', knowledge_eval: '知识评价',
  student_peer_review: '学生互评', inter_group_review: '组间互评', teacher_eval: '教师总评'
}
function nodeLabel(t: string) { return nodeTypeLabels[t] || t }

function formatConfig(config: Record<string, unknown>): string {
  if (!config) return '无配置'
  const parts: string[] = []
  for (const [k, v] of Object.entries(config)) {
    if (v !== undefined && v !== '' && v !== null) {
      const key = { group_count: '分组数', time_limit_mins: '时限', task_content: '任务内容', mindmap_scene: '场景', review_content: '互评内容', class_material_id: '课件ID', coding_env_id: '环境ID' }[k] || k
      parts.push(`${key}: ${typeof v === 'object' ? JSON.stringify(v) : v}`)
    }
  }
  return parts.join('；') || '无配置'
}

onMounted(async () => {
  loading.value = true
  try {
    const r = await getParticipation(taskId)
    if (r.code === 200) {
      info.value = r.data
      const json = r.data.templateJson
      if (json) {
        const payload = typeof json === 'string' ? JSON.parse(json) : json
        orderedNodes.value = buildOrderedSequence(payload.nodes || [], payload.edges || [])
      }
    } else message.error(r.message || '加载失败')
  } catch { message.error('加载实训数据失败') } finally { loading.value = false }
})

async function handleStart() {
  starting.value = true
  try {
    const r = await startTraining(info.value.participationId)
    if (r.code === 200) {
      router.replace({ path: '/student/training-execute', query: { taskId } })
    } else message.error(r.message || '启动失败')
  } catch { message.error('操作失败') } finally { starting.value = false }
}
</script>

<style scoped>
.preview-wrapper { max-width: 800px; margin: 0 auto; padding: 32px 24px; min-height: calc(100vh - 64px); display: flex; flex-direction: column; }
.preview-header { text-align: center; margin-bottom: 32px; }
.preview-header h1 { font-size: 26px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.preview-header p { color: #64748B; font-size: 14px; margin: 0; }
.timeline-area { flex: 1; padding: 16px 0; }
.node-config { font-size: 13px; color: #64748B; margin: 0; line-height: 1.5; }
.preview-footer { display: flex; justify-content: space-between; align-items: center; padding: 20px 0; border-top: 1px solid #E2E8F0; margin-top: 32px; }
</style>
