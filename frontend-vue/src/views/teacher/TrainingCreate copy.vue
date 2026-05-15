<template>
  <div class="training-create-wrapper">
    <!-- 左侧节点库 -->
    <aside class="material-sidebar" :class="{ 'is-collapsed': isSidebarCollapsed }">
      <div class="sidebar-inner">
        <h1 class="panel-main-title">实训流程组件仓库</h1>
        <div class="category-container">
          <div v-for="cat in MATERIAL_CATEGORIES" :key="cat.type" class="cat-group">
            <div class="cat-header" @click="toggleCategory(cat.type)">
              <span class="cat-label">{{ cat.label }}</span>
              <i class="pure-chevron" :class="{ 'is-rotated': expandedCats.includes(cat.type) }"></i>
            </div>
            <div class="drawer-animator" :class="{ 'is-open': expandedCats.includes(cat.type) }">
              <div class="node-list">
                <div v-for="item in cat.items" :key="item.type" class="draggable-node"
                  draggable="true" @dragstart="onDragStart($event, item)">
                  <span class="node-icon">{{ item.icon }}</span>
                  <span class="node-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="sidebar-toggle" @click="isSidebarCollapsed = !isSidebarCollapsed">
        {{ isSidebarCollapsed ? '▶' : '◀' }}
      </div>
    </aside>

    <!-- 中央画布 -->
    <main class="canvas-container" @drop="onDrop" @dragover.prevent>
      <div class="canvas-top-toolbar">
        <button class="glass-btn" :disabled="!canUndo" @click="undo">↩ 撤回</button>
        <button class="glass-btn" :disabled="!canRedo" @click="redo">↪ 恢复</button>
        <div class="divider"></div>
        <button class="secondary-action-btn" @click="handlePublish">🚀 发布实训</button>
      </div>
      <VueFlow v-model:nodes="store.nodes" v-model:edges="store.edges" :node-types="nodeTypes"
        :default-edge-options="edgeOptions" :is-valid-connection="checkValidConnection"
        @connect="onConnect" @pane-ready="onPaneReady" @node-drag-stop="recordHistory"
        @node-click="onNodeClick" @pane-click="onPaneClick">
        <Background pattern-color="#CBD5E1" :gap="24" :size="1.5" />
        <Controls position="bottom-right" class="minimal-controls" />
      </VueFlow>
    </main>

    <!-- 右侧配置面板 -->
    <transition name="panel-slide">
      <aside v-if="selectedNode" class="config-panel">
        <div class="config-header">
          <h3>节点配置</h3>
          <button class="close-btn" @click="selectedNode = null">✕</button>
        </div>
        <div class="config-body">
          <div class="config-field"><label>节点名称</label>
            <input v-model="selectedNode.data.label" class="config-input" @input="syncNodeData" />
          </div>
          <!-- 学生分组 -->
          <template v-if="selectedNode.data.type === 'grouping'">
            <div class="config-field"><label>分组数量</label>
              <input type="number" v-model.number="selectedNode.data.groupCount" class="config-input" min="1" @input="syncNodeData" />
            </div>
          </template>
          <!-- 资源阅读 -->
          <template v-else-if="selectedNode.data.type === 'resource_read'">
            <div class="config-field"><label>关联资源</label>
              <select v-model="selectedNode.data.resourceName" class="config-input" @change="syncNodeData">
                <option value="">请选择</option>
                <option value="SM4算法详解.pdf">SM4算法详解</option>
                <option value="抗重放指导.docx">抗重放攻击实验指导</option>
              </select>
            </div>
          </template>
          <!-- 任务下发 -->
          <template v-else-if="selectedNode.data.type === 'task_distribute'">
            <div class="config-field"><label>任务标题</label>
              <input v-model="selectedNode.data.taskTitle" class="config-input" @input="syncNodeData" />
            </div>
            <div class="config-field"><label>任务描述</label>
              <textarea v-model="selectedNode.data.taskDesc" class="config-textarea" rows="3" @input="syncNodeData"></textarea>
            </div>
          </template>
          <!-- 需求上传 / 方案上传 / 互评 / 思维导图类 -->
          <template v-else-if="['req_upload', 'plan_upload', 'student_peer_review', 'inter_group_review', 'mindmap_preview', 'mindmap_draw', 'knowledge_eval'].includes(selectedNode.data.type)">
            <div class="config-field"><label>场景/要求描述</label>
              <textarea v-model="selectedNode.data.scenario" class="config-textarea" rows="3" @input="syncNodeData"></textarea>
            </div>
          </template>
          <!-- 课后作业 -->
          <template v-else-if="selectedNode.data.type === 'homework'">
            <div class="config-field"><label>时长限制(分钟)</label>
              <input type="number" v-model.number="selectedNode.data.time_limit_mins" class="config-input" min="1" @input="syncNodeData" />
            </div>
            <div class="config-field"><label>出题方式</label>
              <select v-model="selectedNode.data.source_mode" class="config-input" @change="syncNodeData">
                <option value="ai">AI生成</option>
                <option value="bank">题库抽取</option>
              </select>
            </div>
            <template v-if="selectedNode.data.source_mode === 'ai'">
              <div class="config-field"><label>题型</label><input v-model="selectedNode.data.ai_question_type" class="config-input" @input="syncNodeData" placeholder="如: 选择题" /></div>
              <div class="config-field"><label>题数</label><input type="number" v-model.number="selectedNode.data.ai_question_count" class="config-input" min="1" @input="syncNodeData" /></div>
              <div class="config-field"><label>难度</label>
                <select v-model="selectedNode.data.ai_difficulty" class="config-input" @change="syncNodeData">
                  <option value="easy">简单</option><option value="medium">中等</option><option value="hard">困难</option>
                </select>
              </div>
            </template>
            <template v-if="selectedNode.data.source_mode === 'bank'">
              <div class="config-field"><label>题库ID</label><input v-model="selectedNode.data.bank_id" class="config-input" @input="syncNodeData" /></div>
              <div class="config-field"><label>抽题数</label><input type="number" v-model.number="selectedNode.data.bank_count" class="config-input" min="1" @input="syncNodeData" /></div>
            </template>
          </template>
          <!-- 编码实训 -->
          <template v-else-if="selectedNode.data.type === 'coding_class'">
            <div class="config-field"><label>环境ID</label>
              <input v-model="selectedNode.data.coding_env_id" class="config-input" @input="syncNodeData" />
            </div>
          </template>
          <!-- 理论实训 -->
          <template v-else-if="selectedNode.data.type === 'theory_class'">
            <div class="config-field"><label>课件/资源ID</label>
              <input v-model="selectedNode.data.class_material_id" class="config-input" @input="syncNodeData" />
            </div>
          </template>
          <div v-else class="hint">此节点类型无需额外配置</div>
        </div>
      </aside>
    </transition>

    <!-- 发布弹窗 -->
    <n-modal v-model:show="showPublishModal" preset="card" title="发布实训模板" style="width: 460px" :mask-closable="false">
      <n-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-placement="left" label-width="80">
        <n-form-item label="模板名称" path="templateName">
          <n-input v-model:value="publishForm.templateName" placeholder="请输入实训模板名称" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer">
          <n-button @click="showPublishModal = false">取消</n-button>
          <n-button type="primary" :loading="publishing" @click="confirmPublish">确认发布</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 实训流程编排器组件
 * 
 * 教师端用于创建实训模板的可视化编排页面，基于 Vue Flow 实现：
 * - 左侧节点库：提供可拖拽的实训节点组件
 * - 中央画布：可视化编排区域，支持节点拖拽、连接、撤销/重做
 * - 右侧配置面板：选中节点后的详细配置
 * 
 * @component TrainingCreate.vue
 */
import { ref, markRaw, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { NModal, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'
import { useTrainingStore } from '@/stores/modules/training.store'
import { MATERIAL_CATEGORIES } from './data/node-templates'
import { createTemplate } from '@/services/modules/teacher-template.service'
import type { CanvasData, OrchestrationNode, OrchestrationEdge } from '@/services/types/template.types'
import StandardNode from './components/StandardNode.vue'

// === 依赖注入 ===
const store = useTrainingStore()
const router = useRouter()
const message = useMessage()
const { addEdges, toObject, screenToFlowCoordinate } = useVueFlow()

// === Vue Flow 配置 ===

/** 自定义节点类型映射 */
const nodeTypes: Record<string, any> = { standard: markRaw(StandardNode) }

/** 连接线默认配置 */
const edgeOptions = {
  type: 'default', animated: true,
  style: { strokeWidth: 2, stroke: '#818CF8' },
  markerEnd: { type: MarkerType.ArrowClosed, width: 20, height: 20, color: '#818CF8' }
}

/**
 * 验证连接线是否有效
 * 
 * @param connection 连接参数
 * @returns true表示连接有效，false表示无效
 */
const checkValidConnection = (connection: any) => {
  if (connection.source === connection.target) return false  // 不能自连接
  if (connection.sourceHandle === 'target' || connection.targetHandle === 'source') return false  // 只能从输出端连到输入端
  return true
}

// 侧边栏
const isSidebarCollapsed = ref(false)
const expandedCats = ref(MATERIAL_CATEGORIES.map(c => c.type))
const toggleCategory = (type: string) => {
  const i = expandedCats.value.indexOf(type)
  if (i > -1) expandedCats.value.splice(i, 1); else expandedCats.value.push(type)
}

// === 撤销/恢复功能 ===

/** 历史记录栈 - 存储每次操作前的完整状态 */
const historyStack = ref<string[]>([])

/** 重做栈 - 存储被撤销的状态，用于恢复 */
const redoStack = ref<string[]>([])

/** 是否可以撤销 */
const canUndo = computed(() => historyStack.value.length > 0)

/** 是否可以重做 */
const canRedo = computed(() => redoStack.value.length > 0)

/**
 * 记录当前状态到历史栈
 * 
 * 将当前画布状态（节点+连接线）序列化为 JSON 字符串存入历史栈，
 * 最多保留 30 条历史记录，超出时自动丢弃最早的记录。
 */
const recordHistory = () => {
  historyStack.value.push(JSON.stringify(toObject()))
  if (historyStack.value.length > 30) historyStack.value.shift()
  redoStack.value = []  // 新操作后清空重做栈
}

/**
 * 执行撤销操作
 * 
 * 将当前状态保存到重做栈，然后从历史栈弹出上一个状态并恢复。
 */
const undo = () => {
  if (!canUndo.value) return
  redoStack.value.push(JSON.stringify(toObject()))
  const s = JSON.parse(historyStack.value.pop()!)
  store.nodes = s.nodes; store.edges = s.edges
}

/**
 * 执行重做操作
 * 
 * 将当前状态保存到历史栈，然后从重做栈弹出最近撤销的状态并恢复。
 */
const redo = () => {
  if (!canRedo.value) return
  historyStack.value.push(JSON.stringify(toObject()))
  const s = JSON.parse(redoStack.value.pop()!)
  store.nodes = s.nodes; store.edges = s.edges
}

// === 拖拽功能 ===

/**
 * 节点拖拽开始事件处理
 * 
 * 将节点模板数据序列化后存入 dataTransfer，供 drop 事件使用。
 * 
 * @param event 拖拽事件
 * @param item 节点模板数据
 */
const onDragStart = (event: DragEvent, item: any) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/vueflow', JSON.stringify(item))
    event.dataTransfer.effectAllowed = 'move'
  }
}

/**
 * 节点放置事件处理
 * 
 * 将从节点库拖入的节点添加到画布，并记录历史状态。
 * 
 * @param event 放置事件
 */
const onDrop = (event: DragEvent) => {
  const data = event.dataTransfer?.getData('application/vueflow')
  if (!data) return
  recordHistory()
  const item = JSON.parse(data)
  const position = screenToFlowCoordinate({ x: event.clientX, y: event.clientY })
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'standard',
    position,
    data: { type: item.type, label: item.label, icon: item.icon, ...item.defaultData }
  }
  store.nodes.push(newNode)
}

/**
 * 连接线创建事件处理
 * 
 * 记录历史并添加新的连接线。
 * 
 * @param params 连接参数
 */
const onConnect = (params: any) => { recordHistory(); addEdges({ ...params, animated: true }) }

/**
 * 画布就绪事件处理
 * 
 * 将 Vue Flow 实例保存到全局状态，供其他组件使用。
 * 
 * @param instance Vue Flow 实例
 */
const onPaneReady = (instance: any) => { store.flowInstance = instance }

// 节点选中 → 配置面板
const selectedNode = ref<any>(null)
const onNodeClick = ({ node }: any) => { selectedNode.value = node }
const onPaneClick = () => { selectedNode.value = null }
const syncNodeData = () => { /* reactive binding updates automatically */ }

// 发布弹窗
const showPublishModal = ref(false)
const publishing = ref(false)
const publishFormRef = ref<FormInst | null>(null)
const publishForm = ref({ templateName: '' })
const publishRules: FormRules = { templateName: [{ required: true, message: '请输入模板名称' }] }

// === 画布数据转换与验证 ===

/**
 * 将前端图形节点转换为标准 JSON 拓扑结构
 * 
 * 将 Vue Flow 的节点和连接线数据转换为后端可识别的标准格式，包括：
 * - orchestration_id: 编排唯一标识
 * - nodes: 节点数组，包含 node_id, node_type, name, config
 * - edges: 连接线数组，包含 source, target
 * 
 * @returns 标准格式的画布数据
 */
function buildCanvasData(): CanvasData {
  const orchId = 'orch_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
  const resultNodes: any[] = []
  for (const n of store.nodes) {
    resultNodes.push({
      node_id: n.id,
      node_type: (n.data.type as string).toUpperCase(),
      name: (n.data.label as string) || '',
      config: extractConfig(n.data)
    })
  }
  const resultEdges: any[] = []
  for (const e of store.edges) {
    resultEdges.push({ source: e.source, target: e.target })
  }
  return { orchestration_id: orchId, nodes: resultNodes, edges: resultEdges }
}

/**
 * 节点配置字段映射表
 * 
 * 定义每种节点类型需要提取的配置字段，用于从节点数据中提取业务配置。
 */
const configKeysMap: Record<string, any[]> = {
  grouping: ['groupCount', 'groupMethod'],
  resource_read: ['resourceName', 'resourceId'],
  task_distribute: ['taskTitle', 'taskDesc'],
  req_upload: ['scenario', 'showWordCloud'],
  plan_upload: ['uploadReq', 'format'],
  homework: ['time_limit_mins', 'source_mode', 'ai_question_type', 'ai_question_count', 'ai_difficulty', 'bank_id', 'bank_count', 'isAIGenerated', 'questionCount'],
  exam: ['duration', 'totalScore'],
  student_peer_review: ['scenario', 'dimension'],
  inter_group_review: ['scenario', 'dimension'],
  teacher_eval: ['weight'],
  theory_class: ['topic', 'class_material_id'],
  coding_class: ['envType', 'image', 'coding_env_id'],
  mindmap_preview: ['scenario'],
  mindmap_draw: ['scenario'],
  knowledge_eval: ['scenario'],
  start: ['desc'],
  end: ['desc']
}

/**
 * 从节点数据中提取业务配置
 * 
 * 根据节点类型，从节点的 data 对象中提取对应的配置字段。
 * 对特殊节点（如 homework）有额外的处理逻辑。
 * 
 * @param data 节点数据对象
 * @returns 配置对象
 */
function extractConfig(data: any): Record<string, unknown> {
  const type = data.type as string
  const keys = configKeysMap[type] || []
  const config: Record<string, unknown> = {}
  for (const k of keys) {
    if (data[k] !== undefined) config[k] = data[k]
  }
  // 课后作业节点的特殊处理逻辑
  if (type === 'homework') {
    if (!config.source_mode) config.source_mode = data.isAIGenerated ? 'ai' : 'bank'
    if (!config.time_limit_mins) config.time_limit_mins = 30
    if (data.isAIGenerated && !config.ai_question_count) config.ai_question_count = data.questionCount || 5
  }
  return config
}

/**
 * 验证画布是否符合发布要求
 * 
 * 检查画布是否包含必要的节点：开始节点和结束节点。
 * 
 * @returns 错误信息，如果验证通过则返回 null
 */
function validateCanvas(): string | null {
  if (store.nodes.length === 0) return '画布为空，请先添加节点'
  const hasStart = store.nodes.some(n => n.data.type === 'start')
  const hasEnd = store.nodes.some(n => n.data.type === 'end')
  if (!hasStart) return '缺少「开始实训」节点'
  if (!hasEnd) return '缺少「结束实训」节点'
  return null
}

// === 发布功能 ===

/**
 * 处理发布按钮点击
 * 
 * 先验证画布是否符合要求，验证通过后打开发布弹窗。
 */
function handlePublish() {
  const err = validateCanvas()
  if (err) { message.warning(err); return }
  publishForm.value.templateName = ''
  showPublishModal.value = true
}

/**
 * 确认发布模板
 * 
 * 验证表单后，将画布数据转换为标准格式并提交到后端。
 * 提交成功后跳转到模板管理页面。
 */
async function confirmPublish() {
  try { await publishFormRef.value?.validate() } catch { return }
  publishing.value = true
  try {
    const canvasData = buildCanvasData()
    const res = await createTemplate({ templateName: publishForm.value.templateName, canvasData })
    if (res.code === 200) {
      message.success('模板提交成功，AI正在生成标准执行资源...')
      showPublishModal.value = false
      router.push('/teacher/training-manage')
    } else {
      message.error(res.message || '提交失败')
    }
  } catch { message.error('操作失败') } finally { publishing.value = false }
}

/**
 * 组件卸载时清理状态
 * 
 * 清空全局状态中的节点和连接线数据，避免影响其他页面。
 */
onUnmounted(() => { store.nodes = []; store.edges = [] })
</script>

<style scoped>
.training-create-wrapper { display: flex; height: calc(100vh - 64px); background: #F8FAFC; overflow: hidden; box-sizing: border-box; position: relative; }
.panel-main-title { font-size: 22px; font-weight: 800; color: #0F172A; gap: 8px; }
.material-sidebar { width: 320px; background: #FFFFFF; border-right: 1px solid #E2E8F0; z-index: 20; display: flex; flex-direction: column; position: relative; transition: width 0.3s; flex-shrink: 0; }
.material-sidebar.is-collapsed { width: 0; border-right: none; }
.sidebar-inner { width: 320px; height: 100%; padding: 24px 20px; box-sizing: border-box; display: flex; flex-direction: column; overflow: hidden; }
.sidebar-toggle { position: absolute; right: -16px; top: 50%; transform: translateY(-50%); width: 16px; height: 48px; background: #FFFFFF; border: 1px solid #E2E8F0; border-left: none; border-radius: 0 6px 6px 0; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #94A3B8; font-size: 10px; z-index: 21; }
.sidebar-toggle:hover { color: #4F46E5; background: #F8FAFC; }
.category-container { flex: 1; padding-right: 8px; overflow-y: auto; }
.category-container::-webkit-scrollbar { width: 4px; }
.category-container::-webkit-scrollbar-thumb { background: rgba(79,70,229,0.3); border-radius: 4px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 4px; cursor: pointer; }
.cat-label { font-weight: 600; color: #475569; font-size: 15px; }
.pure-chevron { width: 8px; height: 8px; border-right: 2px solid #94A3B8; border-bottom: 2px solid #94A3B8; transform: rotate(-45deg); transition: transform 0.3s; }
.pure-chevron.is-rotated { transform: rotate(45deg); }
.drawer-animator { display: grid; grid-template-rows: 0fr; transition: grid-template-rows 0.3s; }
.drawer-animator.is-open { grid-template-rows: 1fr; }
.node-list { overflow: hidden; display: flex; flex-direction: column; gap: 12px; padding: 0 4px; }
.draggable-node { padding: 14px 16px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; cursor: grab; display: flex; align-items: center; gap: 12px; transition: all 0.2s; margin-top: 4px; }
.draggable-node:hover { background: #FFFFFF; border-color: #818CF8; box-shadow: 0 4px 12px rgba(79,70,229,0.08); transform: translateY(-2px); }
.canvas-container { flex: 1; position: relative; height: 100%; }
.canvas-top-toolbar { position: absolute; top: 24px; right: 24px; z-index: 50; display: flex; align-items: center; gap: 12px; background: rgba(255,255,255,0.9); backdrop-filter: blur(10px); padding: 8px 16px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #E2E8F0; }
.divider { width: 1px; height: 20px; background: #E2E8F0; margin: 0 4px; }
.glass-btn { padding: 6px 12px; background: transparent; border: none; font-size: 15px; font-weight: 600; color: #475569; cursor: pointer; border-radius: 6px; }
.glass-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.glass-btn:hover:not(:disabled) { background: #F1F5F9; color: #0F172A; }
.secondary-action-btn { padding: 8px 16px; background: #0F172A; color: #FFF; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; }
.secondary-action-btn:hover { background: #334155; }
:deep(.minimal-controls) { background: rgba(255,255,255,0.95); padding: 4px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); border: 1px solid #E2E8F0; right: 24px !important; bottom: 24px !important; }

/* 配置面板 */
.config-panel { width: 300px; background: #FFFFFF; border-left: 1px solid #E2E8F0; z-index: 20; padding: 0; display: flex; flex-direction: column; overflow-y: auto; flex-shrink: 0; }
.config-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #E2E8F0; }
.config-header h3 { font-size: 16px; font-weight: 700; color: #0F172A; margin: 0; }
.close-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #94A3B8; }
.close-btn:hover { color: #EF4444; }
.config-body { padding: 16px 20px; display: flex; flex-direction: column; gap: 14px; }
.config-field { display: flex; flex-direction: column; gap: 6px; }
.config-field label { font-size: 13px; font-weight: 700; color: #475569; }
.config-input, .config-textarea { padding: 8px 12px; border: 1px solid #E2E8F0; border-radius: 8px; font-size: 14px; font-family: inherit; outline: none; }
.config-input:focus, .config-textarea:focus { border-color: #818CF8; box-shadow: 0 0 0 3px rgba(129,140,248,0.1); }
.config-textarea { resize: vertical; min-height: 60px; }
.hint { color: #94A3B8; font-size: 13px; text-align: center; padding: 20px 0; }
.panel-slide-enter-active, .panel-slide-leave-active { transition: all 0.25s ease; }
.panel-slide-enter-from, .panel-slide-leave-to { width: 0; opacity: 0; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; }
</style>
