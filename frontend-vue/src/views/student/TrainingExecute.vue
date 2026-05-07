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

        <!-- 进行中 → 动态渲染节点 -->
        <template v-else-if="info.status === 1 && curNode">
          <div class="dynamic-card">

            <!-- START -->
            <template v-if="isType('START')">
              <div class="welcome-card">
                <div class="icon-wrap success">▶️</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p>实训流程即将开始，请按步骤完成各项任务</p>
              </div>
            </template>

            <!-- GROUPING -->
            <template v-else-if="isType('GROUPING')">
              <div class="task-card">
                <div class="icon-wrap info">👥</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p>正在进行随机分组... 您被分配到了 <b>第 {{ randomGroup }} 组</b></p>
                <p class="cfg-hint">分组方式: {{ curNode.config?.group_method || curNode.config?.groupMethod || '随机' }}，共 {{ curNode.config?.group_count || curNode.config?.groupCount || 4 }} 组</p>
              </div>
            </template>

            <!-- 资源阅读 / 理论实训 -->
            <template v-else-if="isType('RESOURCE_READ') || isType('THEORY_CLASS') || isType('RESOURCE')">
              <div class="task-card">
                <div class="icon-wrap info">{{ isType('THEORY_CLASS') ? '👩‍🏫' : '📖' }}</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <div class="resource-card">
                  <div class="file-icon">📄</div>
                  <div class="file-info">
                    <span class="file-name">{{ curNode.config?.resource_name || curNode.config?.resourceName || curNode.config?.class_material_id || curNode.config?.topic || '教学资源' }}</span>
                  </div>
                </div>
                <n-checkbox v-model:checked="nodeCompleted" style="margin-top:12px">我已阅读完成</n-checkbox>
              </div>
            </template>

            <!-- 上传类 -->
            <template v-else-if="isType('PLAN_UPLOAD') || isType('REQ_UPLOAD') || isType('SOLUTION_UPLOAD') || isType('UPLOAD')">
              <div class="task-card">
                <div class="icon-wrap info">📤</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p class="cfg-hint">{{ curNode.config?.scenario || curNode.config?.upload_content || curNode.config?.scene_content || '请按要求上传文件' }}</p>
                <n-upload :multiple="false" :default-file-list="[]" action="#" style="margin-top:16px">
                  <n-button>选择文件</n-button>
                </n-upload>
                <p class="cfg-hint" style="margin-top:8px">支持格式: {{ curNode.config?.format || 'pdf, docx, zip' }}</p>
              </div>
            </template>

            <!-- 任务下发 -->
            <template v-else-if="isType('TASK') || isType('TASK_DISTRIBUTE') || isType('TASK_PUBLISH')">
              <div class="task-card">
                <div class="icon-wrap warning">📋</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p class="cfg-hint">{{ curNode.config?.task_content || curNode.config?.taskTitle || curNode.config?.taskDesc || '请完成下发任务' }}</p>
              </div>
            </template>

            <!-- 作业 / 考试 -->
            <template v-else-if="isType('HOMEWORK') || isType('EXAM')">
              <div class="task-card">
                <div class="icon-wrap warning">{{ isType('EXAM') ? '⏱️' : '📝' }}</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <div class="countdown-bar">
                  <span>剩余时间</span>
                  <span class="countdown-num">{{ formatTime(remainingSec) }}</span>
                </div>
                <div class="question-block" v-for="(q, qi) in mockQuestions" :key="qi">
                  <p class="q-title">题目 {{ qi + 1 }}：{{ q.title }}</p>
                  <n-radio-group :name="`q${qi}`">
                    <n-space vertical><n-radio v-for="opt in q.options" :key="opt" :value="opt">{{ opt }}</n-radio></n-space>
                  </n-radio-group>
                </div>
                <n-checkbox v-model:checked="nodeCompleted" style="margin-top:12px">确认提交答案</n-checkbox>
              </div>
            </template>

            <!-- 思维导图类 -->
            <template v-else-if="isType('MINDMAP') || isType('MINDMAP_PREVIEW') || isType('MINDMAP_DRAW') || isType('MINDMAP_PRACTICE') || isType('KNOWLEDGE')">
              <div class="task-card">
                <div class="icon-wrap info">🧠</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <div class="mindmap-placeholder">
                  <div class="mm-node center">主题</div>
                  <div class="mm-branches">
                    <div class="mm-node branch">分支 A</div>
                    <div class="mm-node branch">分支 B</div>
                    <div class="mm-node branch">分支 C</div>
                  </div>
                </div>
                <p class="cfg-hint">{{ curNode.config?.mindmap_scene || curNode.config?.scenario || '思维导图场景' }}</p>
              </div>
            </template>

            <!-- 编码实训 -->
            <template v-else-if="isType('CODING') || isType('CODING_CLASS')">
              <div class="task-card">
                <div class="icon-wrap info">💻</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <div class="code-placeholder">
                  <pre><code>// 编码环境: {{ curNode.config?.coding_env_id || curNode.config?.envType || 'K8s 容器' }}
              // 镜像: {{ curNode.config?.image || 'sm-suite' }}
              // 请在 IDE 中编写代码...</code></pre>
                </div>
              </div>
            </template>

            <!-- 互评类 -->
            <template v-else-if="isType('PEER_REVIEW') || isType('STUDENT_PEER_REVIEW') || isType('GROUP_PEER_REVIEW') || isType('INTER_GROUP_REVIEW') || isType('REVIEW')">
              <div class="task-card">
                <div class="icon-wrap info">{{ isType('GROUP') || isType('INTER') ? '🔄' : '🤝' }}</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p class="cfg-hint">评价维度: {{ curNode.config?.review_content || curNode.config?.dimension || '综合表现' }}</p>
                <n-rate :default-value="3" size="large" style="margin-top:12px" />
                <n-input type="textarea" placeholder="输入评语..." style="margin-top:12px" />
              </div>
            </template>

            <!-- 教师评价 -->
            <template v-else-if="isType('TEACHER_EVAL') || isType('EVALUATION') || isType('EVAL')">
              <div class="task-card">
                <div class="icon-wrap success">🏆</div>
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p class="cfg-hint">总评权重: {{ (curNode.config?.weight || 0.5) * 100 }}%</p>
                <n-rate :default-value="4" size="large" />
              </div>
            </template>

            <!-- END -->
            <template v-else-if="isType('END')">
              <div class="welcome-card">
                <div class="icon-wrap success">✅</div>
                <h2 class="node-title">实训结束</h2>
                <p>你已完成所有节点，点击下方按钮提交实训</p>
              </div>
            </template>

            <!-- 默认 -->
            <template v-else>
              <div class="task-card">
                <h2 class="node-title">{{ curNode.name }}</h2>
                <p class="cfg-hint">{{ formatConfig(curNode.config) }}</p>
              </div>
            </template>
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
        <div class="bar-info">
          <span>{{ curNode?.name }}</span>
        </div>
        <n-button type="primary" size="large" :loading="acting" @click="handleNext">
          {{ isEnd ? '提交并结束实训' : '下一步' }}
        </n-button>
      </footer>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, NUpload, NCheckbox, NRadioGroup, NRadio, NSpace, NInput, NRate, useMessage } from 'naive-ui'
import { getParticipation, startTraining, nextTraining } from '@/services/modules/student-dashboard.service'
import type { ParticipationInfo } from '@/services/modules/student-dashboard.service'
import { buildOrderedSequence, normalizeType, checkIsEndNode } from '@/utils/training-flow'

const route = useRoute(); const router = useRouter(); const message = useMessage()
const loading = ref(false); const acting = ref(false); const nodeCompleted = ref(false)
const info = ref<ParticipationInfo>({ participationId: 0, taskId: 0, status: 0, taskName: '' })
const orderedSeq = ref<any[]>([]); const seqIndex = ref(0)
const taskId = Number(route.query.taskId) || 0
const randomGroup = ref(Math.floor(Math.random() * 4) + 1)
const remainingSec = ref(600)
let countdownTimer: any

const curNode = computed(() => orderedSeq.value[seqIndex.value] || null)
const isEnd = computed(() => curNode.value ? checkIsEndNode(curNode.value) : false)

// 模拟倒计时 (作业/考试节点)
function startCountdown() { stopCountdown(); remainingSec.value = (curNode.value?.config?.time_limit_mins || 10) * 60; countdownTimer = setInterval(() => { if (remainingSec.value > 0) remainingSec.value-- }, 1000) }
function stopCountdown() { if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null } }

// 模拟选择题
const mockQuestions = computed(() => {
  if (!curNode.value) return []
  const mode = curNode.value.config?.source_mode || 'ai'
  const count = mode === 'ai' ? (curNode.value.config?.ai_question_count || 2) : (curNode.value.config?.bank_count || 2)
  return Array.from({ length: Math.min(count, 2) }, (_, i) => ({
    title: i === 0 ? '下列哪个属于对称加密算法？' : '分组密码的工作模式不包括？',
    options: i === 0 ? ['A. RSA', 'B. SM4', 'C. ECC', 'D. DH'] : ['A. ECB', 'B. CBC', 'C. CTR', 'D. HTTP']
  }))
})

function formatTime(sec: number): string { const m = Math.floor(sec / 60); const s = sec % 60; return `${m}:${s.toString().padStart(2, '0')}` }
function formatConfig(cfg: Record<string, unknown>): string {
  if (!cfg) return ''
  return Object.entries(cfg).filter(([,v]) => v !== undefined && v !== '' && v !== null).map(([k,v]) => `${k}: ${typeof v === 'object' ? JSON.stringify(v) : v}`).join('；')
}

// 匹配 node_type（不区分大小写，支持下划线/连字符变体）
const curType = computed(() => normalizeType(curNode.value?.node_type || curNode.value?.type || ''))
function isType(keyword: string): boolean { return curType.value.includes(normalizeType(keyword)) }

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
        if (info.value.status === 1 && isEnd.value) info.value.status = 2
      }
    } else message.error(r.message || '加载失败')
  } catch { message.error('加载实训数据失败') } finally { loading.value = false }
}

async function handleStart() {
  acting.value = true
  try {
    const r = await startTraining(info.value.participationId)
    if (r.code === 200) { info.value.status = 1; seqIndex.value = 0; randomGroup.value = Math.floor(Math.random() * 4) + 1 }
    else message.error(r.message || '启动失败')
  } catch { message.error('操作失败') } finally { acting.value = false }
}

async function handleNext() {
  if (isEnd.value) {
    acting.value = true
    try {
      const r = await nextTraining(info.value.participationId, seqIndex.value, true)
      if (r.code === 200 && r.data.completed) { info.value.status = 2; message.success('实训完成！'); stopCountdown() }
      else message.error(r.message || '提交失败')
    } catch { message.error('操作失败') } finally { acting.value = false }
    return
  }
  const nextIdx = seqIndex.value + 1
  if (nextIdx >= orderedSeq.value.length) return
  acting.value = true
  try {
    const r = await nextTraining(info.value.participationId, nextIdx, false)
    if (r.code === 200) {
      seqIndex.value = nextIdx; nodeCompleted.value = false
      const nextNode = orderedSeq.value[nextIdx]
      if (nextNode && checkIsEndNode(nextNode)) stopCountdown()
      else if (nextNode && (normalizeType(nextNode.node_type).includes('HOMEWORK') || normalizeType(nextNode.node_type).includes('EXAM'))) startCountdown()
    }
  } catch { message.error('操作失败') } finally { acting.value = false }
}

onMounted(() => { if (taskId) loadInfo() })
onUnmounted(() => stopCountdown())
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

/* 动态渲染卡片 */
.dynamic-card { width: 100%; max-width: 640px; }
.welcome-card, .task-card { background: #FFFFFF; border-radius: 16px; padding: 40px; text-align: center; border: 1px solid #E2E8F0; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.icon-wrap { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; font-size: 24px; }
.icon-wrap.success { background: #DCFCE7; }
.icon-wrap.info { background: #EEF2FF; }
.icon-wrap.warning { background: #FEF3C7; }
.node-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.cfg-hint { color: #64748B; font-size: 14px; margin: 8px 0 0 0; line-height: 1.5; }

.resource-card { display: flex; align-items: center; gap: 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin-top: 12px; }
.file-icon { font-size: 28px; flex-shrink: 0; }
.file-info { text-align: left; }
.file-name { font-weight: 600; color: #1E293B; font-size: 14px; }

.countdown-bar { display: flex; justify-content: space-between; align-items: center; background: #FEF3C7; border-radius: 8px; padding: 10px 16px; margin: 12px 0; font-size: 14px; color: #92400E; }
.countdown-num { font-weight: 800; font-size: 20px; font-family: monospace; }

.question-block { background: #F8FAFC; border-radius: 10px; padding: 16px; margin-top: 12px; text-align: left; }
.q-title { font-weight: 700; color: #1E293B; font-size: 14px; margin: 0 0 10px 0; }

.mindmap-placeholder { display: flex; flex-direction: column; align-items: center; gap: 12px; margin: 16px 0; }
.mm-node { padding: 10px 20px; border-radius: 8px; font-size: 14px; font-weight: 600; }
.mm-node.center { background: #EEF2FF; color: #4F46E5; border: 2px solid #818CF8; }
.mm-node.branch { background: #F1F5F9; color: #475569; border: 1px solid #E2E8F0; }
.mm-branches { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; }

.code-placeholder { background: #1E293B; border-radius: 10px; padding: 20px; text-align: left; margin-top: 12px; }
.code-placeholder pre { margin: 0; color: #A5D6A7; font-family: 'Courier New', monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; }

.bottom-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; background: #FFFFFF; border-top: 1px solid #E2E8F0; }
.bar-info { font-size: 14px; color: #64748B; }
</style>
