<template>
  <div class="pipeline-create-wrapper">
    <aside class="material-sidebar" :class="{ 'is-collapsed': isSidebarCollapsed }">
      <div class="sidebar-inner">
        <h1 class="panel-main-title">实训节点库</h1>
        <div class="category-container">
          <div v-for="cat in MATERIAL_CATEGORIES" :key="cat.type" class="cat-group">
            <div class="cat-header" @click="toggleCategory(cat.type)">
              <span class="cat-label">{{ cat.label }}</span>
              <i class="pure-chevron" :class="{ 'is-rotated': expandedCats.includes(cat.type) }"></i>
            </div>
            <div class="drawer-animator" :class="{ 'is-open': expandedCats.includes(cat.type) }">
              <div class="node-list">
                <div v-for="item in cat.items" :key="item.type" class="draggable-node"
                  draggable="true" @dragstart="onDragStartLibrary($event, item)">
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

    <main class="canvas-container">
      <div class="canvas-top-toolbar">
        <span class="pipeline-title">实训流程编排 (线性模式)</span>
        <div class="flex-spacer"></div>
        <button class="secondary-action-btn" @click="handlePublish">🚀 发布实训</button>
      </div>
      
      <div class="pipeline-scroll-area" 
           @dragover.prevent 
           @drop="onDropCanvas">
        <div class="pipeline-queue" v-if="orchestrationList.length > 0">
          <transition-group name="list-anim">
            <div v-for="(node, index) in orchestrationList" :key="node.id" class="pipeline-item-wrapper">
              
              <div class="pipeline-node" 
                   :class="{ 'is-active': selectedNodeId === node.id }"
                   @click="selectNode(node)">
                <div class="node-index">{{ index + 1 }}</div>
                <div class="node-content">
                  <div class="node-title">
                    <span class="node-icon">{{ getNodeIcon(node.type) }}</span>
                    {{ node.name }}
                  </div>
                  <div class="node-type-tag">{{ getCategoryLabel(node.type) }}</div>
                </div>
                <div class="node-actions">
                  <button class="icon-btn" @click.stop="moveNodeUp(index)" :disabled="index === 0">↑</button>
                  <button class="icon-btn" @click.stop="moveNodeDown(index)" :disabled="index === orchestrationList.length - 1">↓</button>
                  <button class="icon-btn danger" @click.stop="deleteNode(index)">✕</button>
                </div>
              </div>

              <div class="pipeline-connector" v-if="index !== orchestrationList.length - 1">
                <div class="line"></div>
                <div class="arrow">▼</div>
              </div>

            </div>
          </transition-group>
        </div>
        
        <div v-else class="empty-canvas-hint">
          <div class="hint-icon">📥</div>
          <p>从左侧拖拽节点到此处开始编排</p>
        </div>
        
        <div class="drop-zone-indicator">将节点拖拽至此处追加</div>
      </div>
    </main>

    <transition name="panel-slide">
      <aside v-if="activeNode" class="config-panel">
        <div class="config-header">
          <h3>{{ activeNode.name }} 配置</h3>
          <button class="close-btn" @click="selectedNodeId = null">✕</button>
        </div>
        
        <div class="config-body">
          <div class="section-title">基础配置</div>
          
          <template v-if="activeNode.type === 'start'">
            <div class="config-field">
              <label><span class="required">*</span>实训整体说明(场景)</label>
              <textarea v-model.lazy="activeNode.config.desc" class="config-textarea" placeholder="设置实训的场景"></textarea>
            </div>
            <div class="config-field">
              <label>实训学生背景</label>
              <textarea v-model.lazy="activeNode.config.student_bg" class="config-textarea" placeholder="介绍学生的背景、基础"></textarea>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'end'">
            <div class="config-field">
              <label>结束页说明文字</label>
              <input v-model.lazy="activeNode.config.end_desc" class="config-input" />
            </div>
          </template>

          <template v-else-if="activeNode.type === 'resource_read'">
            <div class="config-field">
              <label><span class="required">*</span>阅读文件列表</label>
              <div v-for="(file, i) in activeNode.config.files" :key="i" class="list-item">
                <span class="idx">{{ Number(i) + 1 }}</span>
                <input v-model.lazy="file.original_name" class="config-input flex-1" placeholder="上传文件名(模拟)" />
                <input v-model.lazy="file.display_name" class="config-input flex-1" placeholder="展示名" />
                <input type="number" v-model.lazy="file.min_progress" class="config-input w-20" placeholder="进度%" min="0" max="100" />
                <button class="icon-btn" @click="activeNode.config.files.splice(i, 1)">-</button>
              </div>
              <button class="add-btn" @click="activeNode.config.files.push({ original_name: '', display_name: '', min_progress: 100 })">+ 添加文件</button>
            </div>
          </template>

          <template v-else-if="activeNode.type === 'video_watch'">
            <div class="config-field"><label><span class="required">*</span>视频文件 (模拟上传)</label><input v-model.lazy="activeNode.config.video_file" class="config-input" placeholder="请选择视频文件" /></div>
            <div class="config-field"><label>展示标题</label><input v-model.lazy="activeNode.config.video_title" class="config-input" /></div>
            <div class="config-field"><label>最少播放进度(%)</label><input type="number" v-model.lazy="activeNode.config.min_progress" class="config-input" min="0" max="100" /></div>
            <div class="config-field flex-row"><label>允许拖拽进度条</label><input type="checkbox" v-model="activeNode.config.allow_drag" /></div>
          </template>

          <template v-else-if="['mindmap_preview', 'mindmap_draw'].includes(activeNode.type)">
            <div class="config-field flex-row"><label>由 AI 自动生成导图</label><input type="checkbox" v-model="activeNode.config.ai_generated" /></div>
            <template v-if="activeNode.config.ai_generated">
              <div class="config-field"><label><span class="required">*</span>导图主题</label><input v-model.lazy="activeNode.config.topic" class="config-input" /></div>
              <div class="config-field"><label>最大节点数</label><input type="number" v-model.lazy="activeNode.config.max_nodes" class="config-input" /></div>
            </template>
            <template v-else>
              <div class="config-field"><label>初始导图结构</label><button class="secondary-action-btn w-full">进入画板实时手绘</button></div>
            </template>
          </template>

          <template v-else-if="activeNode.type === 'ai_practice'">
            <div class="config-field"><label>考核知识点 (不填则AI生成)</label><textarea v-model.lazy="activeNode.config.knowledge_points" class="config-textarea"></textarea></div>
            <div class="config-field"><label>通关标准描述</label><textarea v-model.lazy="activeNode.config.pass_criteria" class="config-textarea"></textarea></div>
            <div class="config-field"><label>最大对话轮数</label><input type="number" v-model.lazy="activeNode.config.max_rounds" class="config-input" /></div>
          </template>

          <template v-else-if="activeNode.type === 'task_distribute'">
            <div class="config-field"><label>任务标题</label><input v-model.lazy="activeNode.config.title" class="config-input" /></div>
            <div class="config-field"><label>具体要求</label><textarea v-model.lazy="activeNode.config.requirement" class="config-textarea"></textarea></div>
            <div class="config-field"><label>截止时间</label><input type="date" v-model.lazy="activeNode.config.deadline" class="config-input" /></div>
          </template>

          <template v-else-if="activeNode.type === 'req_upload'">
            <div class="config-field"><label>场景描述</label><textarea v-model.lazy="activeNode.config.scenario" class="config-textarea"></textarea></div>
            <div class="config-field"><label>最低字数要求</label><input type="number" v-model.lazy="activeNode.config.min_words" class="config-input" /></div>
          </template>

          <template v-else-if="activeNode.type === 'plan_upload'">
            <div class="config-field"><label>最大文件大小(MB)</label><input type="number" v-model.lazy="activeNode.config.max_size" class="config-input" /></div>
            <div class="config-field"><label>上传具体要求</label><textarea v-model.lazy="activeNode.config.requirement" class="config-textarea"></textarea></div>
          </template>

          <template v-else-if="activeNode.type === 'homework'">
            <div class="config-field"><label>答题限时(分钟)</label><input type="number" v-model.lazy="activeNode.config.time_limit" class="config-input" /></div>
            <div class="config-field">
              <label>题目来源</label>
              <select v-model="activeNode.config.source_mode" class="config-input">
                <option value="ai">AI 生成</option><option value="bank">题库抽取</option>
              </select>
            </div>
            <template v-if="activeNode.config.source_mode === 'ai'">
              <div class="config-field flex-row"><label>单选数</label><input type="number" v-model.lazy="activeNode.config.count_single" class="config-input w-20"/></div>
              <div class="config-field flex-row"><label>多选数</label><input type="number" v-model.lazy="activeNode.config.count_multi" class="config-input w-20"/></div>
              <div class="config-field flex-row"><label>简答数</label><input type="number" v-model.lazy="activeNode.config.count_essay" class="config-input w-20"/></div>
              <div class="config-field flex-row"><label>难度</label>
                <select v-model="activeNode.config.difficulty" class="config-input w-20">
                  <option value="easy">易</option><option value="medium">中</option><option value="hard">难</option>
                </select>
              </div>
              <div class="config-field flex-row"><label>生成后保存到题库</label><input type="checkbox" v-model="activeNode.config.save_to_bank" /></div>
            </template>
            <template v-else>
              <div class="config-field flex-row"><label>选题方式</label>
                <select v-model="activeNode.config.select_mode" class="config-input w-32">
                  <option value="manual">手动选取</option><option value="random">随机抽取</option>
                </select>
              </div>
            </template>
          </template>

          <template v-else-if="activeNode.type === 'coding_class'">
            <div class="config-field"><label>初始代码模板</label><textarea v-model.lazy="activeNode.config.template" class="config-textarea font-mono" placeholder="可为空"></textarea></div>
          </template>

          <template v-else-if="activeNode.type === 'student_peer_review'">
            <div class="config-field"><label>评价产出物关联</label>
              <select v-model="activeNode.config.target_node" class="config-input">
                <option value="">请选择前置产出节点</option>
                <option v-for="n in orchestrationList.filter(n => ['req_upload', 'plan_upload', 'mindmap_draw'].includes(n.type))" :key="n.id" :value="n.id">{{ n.name }}</option>
              </select>
            </div>
            <div class="config-field"><label>每人需评份数</label><input type="number" v-model.lazy="activeNode.config.count" class="config-input" min="3" max="5" /></div>
            <div class="config-field"><label>评价权重(成绩占比%)</label><input type="number" v-model.lazy="activeNode.config.weight" class="config-input" /></div>
          </template>

          <template v-else>
            <div class="hint">该节点基础配置已默认完善</div>
          </template>

          <div class="ai-config-wrapper" v-if="hasAIConfig(activeNode.type)">
            <div class="ai-config-header" @click="toggleAIConfig">
              <span class="ai-title">✨ AI 增强配置</span>
              <i class="pure-chevron" :class="{ 'is-rotated': isAIExpanded }"></i>
            </div>
            <div class="drawer-animator" :class="{ 'is-open': isAIExpanded }">
              <div class="ai-config-body">
                <div class="config-field flex-row" v-if="'ai_qa' in activeNode.ai_config">
                  <label>允许 AI 随时问答</label><input type="checkbox" v-model="activeNode.ai_config.ai_qa" />
                </div>
                
                <template v-if="activeNode.type === 'start'">
                  <div class="config-field flex-row"><label>开启 AI 个性化欢迎</label><input type="checkbox" v-model="activeNode.ai_config.ai_welcome_enabled" /></div>
                  <div class="config-field" v-if="activeNode.ai_config.ai_welcome_enabled">
                    <label>欢迎语模板</label><textarea v-model.lazy="activeNode.ai_config.ai_welcome_tpl" class="config-textarea"></textarea>
                  </div>
                </template>

                <template v-if="activeNode.type === 'resource_read'">
                  <div class="config-field flex-row"><label>AI 提炼摘要</label><input type="checkbox" v-model="activeNode.ai_config.ai_summary" /></div>
                  <div class="config-field flex-row"><label>AI 提炼重点</label><input type="checkbox" v-model="activeNode.ai_config.ai_focus" /></div>
                  <div class="config-field flex-row"><label>AI 提炼快捷导航</label><input type="checkbox" v-model="activeNode.ai_config.ai_nav" /></div>
                </template>

                <template v-if="activeNode.type === 'video_watch'">
                  <div class="config-field flex-row"><label>AI 实时字幕</label><input type="checkbox" v-model="activeNode.ai_config.ai_subtitles" /></div>
                  <div class="config-field flex-row"><label>AI 知识点切片</label><input type="checkbox" v-model="activeNode.ai_config.ai_chapters" /></div>
                </template>

                <template v-if="activeNode.type === 'ai_practice'">
                  <div class="config-field flex-row"><label>难度自适应</label><input type="checkbox" v-model="activeNode.ai_config.ai_adaptive" /></div>
                  <div class="config-field flex-row"><label>核心概念检测</label><input type="checkbox" v-model="activeNode.ai_config.ai_core_concept" /></div>
                </template>
                
                <template v-if="activeNode.type === 'coding_class'">
                  <div class="config-field flex-row"><label>开启 AI 辅助</label><input type="checkbox" v-model="activeNode.ai_config.ai_assist" /></div>
                  <div class="config-field flex-row" v-if="activeNode.ai_config.ai_assist"><label>辅助模式</label>
                    <select v-model="activeNode.ai_config.ai_mode" class="config-input w-24">
                      <option value="guide">Guide</option><option value="hint">Hint</option><option value="full">Full</option>
                    </select>
                  </div>
                  <div class="config-field flex-row"><label>AI 代码审查</label><input type="checkbox" v-model="activeNode.ai_config.ai_review" /></div>
                </template>
              </div>
            </div>
          </div>

        </div>
      </aside>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// ==================== 数据结构与常量 ====================

// 左侧物料库定义
const MATERIAL_CATEGORIES = [
  {
    label: '通用流程', type: 'general',
    items: [
      { type: 'start', label: '开始实训', icon: '🏁' },
      { type: 'end', label: '结束实训', icon: '🛑' }
    ]
  },
  {
    label: '预习环节', type: 'pre_learning',
    items: [
      { type: 'resource_read', label: '资源阅读', icon: '📄' },
      { type: 'video_watch', label: '视频观看', icon: '🎬' },
      { type: 'mindmap_preview', label: '导图预习', icon: '🗺️' },
      { type: 'survey', label: '学情调查', icon: '📊' }
    ]
  },
  {
    label: '理论实训', type: 'theory',
    items: [
      { type: 'mindmap_draw', label: '导图绘制', icon: '✍️' },
      { type: 'ai_practice', label: 'AI 对练', icon: '🤖' },
      { type: 'theory_class', label: '理论学习', icon: '📚' }
    ]
  },
  {
    label: '实践实训', type: 'practice',
    items: [
      { type: 'task_distribute', label: '任务下发', icon: '📋' },
      { type: 'req_upload', label: '需求上传', icon: '📤' },
      { type: 'plan_upload', label: '方案上传', icon: '📑' },
      { type: 'homework', label: '课后作业', icon: '📝' },
      { type: 'coding_class', label: '编码实训', icon: '💻' }
    ]
  },
  {
    label: '点评环节', type: 'evaluation',
    items: [
      { type: 'student_peer_review', label: '学生互评', icon: '👥' },
      { type: 'teacher_eval', label: '教师点评', icon: '👨‍🏫' }
    ]
  }
]

// 节点默认数据工厂
const createNodeDefaultData = (type: string, name: string) => {
  const baseNode = {
    id: `node_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
    type,
    name,
    config: {} as Record<string, any>,
    ai_config: { ai_qa: true } as Record<string, any> // 默认都有 AI 问答
  }

  // 基础配置初始化
  switch (type) {
    case 'start':
      baseNode.config = { desc: '', student_bg: '' }
      baseNode.ai_config = { ...baseNode.ai_config, ai_welcome_enabled: true, ai_welcome_tpl: '同学你好，欢迎来到本次实训，在这里你将学习到...' }
      break
    case 'end':
      baseNode.config = { end_desc: '恭喜你完成了本次实训所有环节！' }
      break
    case 'resource_read':
      baseNode.config = { files: [{ original_name: '', display_name: '', min_progress: 100 }] }
      baseNode.ai_config = { ...baseNode.ai_config, ai_summary: true, ai_focus: true, ai_nav: true }
      break
    case 'video_watch':
      baseNode.config = { video_file: '', video_title: '', min_progress: 100, allow_drag: false, speeds: [1.25, 1.5, 2] }
      baseNode.ai_config = { ...baseNode.ai_config, ai_subtitles: true, ai_chapters: true }
      break
    case 'mindmap_preview':
    case 'mindmap_draw':
      baseNode.config = { ai_generated: true, topic: '', max_nodes: type === 'mindmap_preview' ? 50 : 10, manual_structure: null }
      break
    case 'ai_practice':
      baseNode.config = { knowledge_points: '', pass_criteria: '', max_rounds: 10 }
      baseNode.ai_config = { ...baseNode.ai_config, ai_adaptive: true, ai_core_concept: true }
      break
    case 'task_distribute':
      baseNode.config = { title: '', requirement: '', deadline: '', files: [] }
      baseNode.ai_config = { ...baseNode.ai_config, ai_task_breakdown: true }
      break
    case 'req_upload':
      baseNode.config = { scenario: '', min_words: 10 }
      break
    case 'plan_upload':
      baseNode.config = { formats: ['pdf', 'word', 'md'], max_size: 100, requirement: '' }
      baseNode.ai_config = { ...baseNode.ai_config, ai_pre_eval: true }
      break
    case 'homework':
      baseNode.config = { 
        source_mode: 'ai', time_limit: 30,
        count_single: 5, count_multi: 5, count_fill: 5, count_judge: 5, count_essay: 5, difficulty: 'medium', save_to_bank: true,
        select_mode: 'random'
      }
      break
    case 'coding_class':
      baseNode.config = { template: '' }
      baseNode.ai_config = { ...baseNode.ai_config, ai_assist: true, ai_mode: 'guide', ai_review: true }
      break
    case 'student_peer_review':
      baseNode.config = { target_node: '', count: 3, dimensions: [], weight: 30 }
      break
  }
  return baseNode
}

// ==================== 状态管理 ====================

const isSidebarCollapsed = ref(false)
const expandedCats = ref(MATERIAL_CATEGORIES.map(c => c.type))
const orchestrationList = ref<any[]>([])
const selectedNodeId = ref<string | null>(null)
const isAIExpanded = ref(false)

const activeNode = computed(() => orchestrationList.value.find(n => n.id === selectedNodeId.value))

// ==================== 交互方法 ====================

const toggleCategory = (type: string) => {
  const idx = expandedCats.value.indexOf(type)
  if (idx > -1) expandedCats.value.splice(idx, 1); else expandedCats.value.push(type)
}

const toggleAIConfig = () => { isAIExpanded.value = !isAIExpanded.value }

// 判断节点是否有除了自身基础属性外的 AI 配置
const hasAIConfig = (type: string) => {
  return !['end'].includes(type) // 绝大部分都有 AI，除了结束节点等少数
}

// 辅助方法获取图标与分类名
const getNodeIcon = (type: string) => {
  for (const cat of MATERIAL_CATEGORIES) {
    const item = cat.items.find(i => i.type === type)
    if (item) return item.icon
  }
  return '📦'
}
const getCategoryLabel = (type: string) => {
  for (const cat of MATERIAL_CATEGORIES) {
    if (cat.items.some(i => i.type === type)) return cat.label
  }
  return '未知分类'
}

// ==================== 拖拽与列表逻辑 ====================

const onDragStartLibrary = (e: DragEvent, item: any) => {
  if (e.dataTransfer) {
    e.dataTransfer.setData('application/node-type', item.type)
    e.dataTransfer.setData('application/node-label', item.label)
    e.dataTransfer.effectAllowed = 'copy'
  }
}

const onDropCanvas = (e: DragEvent) => {
  const type = e.dataTransfer?.getData('application/node-type')
  const label = e.dataTransfer?.getData('application/node-label')
  if (!type) return
  
  const newNode = createNodeDefaultData(type, label!)
  orchestrationList.value.push(newNode)
  // 自动选中新加的节点
  selectNode(newNode)
}

const selectNode = (node: any) => {
  selectedNodeId.value = node.id
  isAIExpanded.value = false // 切换节点时默认收起 AI 配置
}

const deleteNode = (index: number) => {
  const node = orchestrationList.value[index]
  if (selectedNodeId.value === node.id) selectedNodeId.value = null
  orchestrationList.value.splice(index, 1)
}

const moveNodeUp = (index: number) => {
  if (index <= 0) return
  const temp = orchestrationList.value[index]
  orchestrationList.value[index] = orchestrationList.value[index - 1]
  orchestrationList.value[index - 1] = temp
}

const moveNodeDown = (index: number) => {
  if (index >= orchestrationList.value.length - 1) return
  const temp = orchestrationList.value[index]
  orchestrationList.value[index] = orchestrationList.value[index + 1]
  orchestrationList.value[index + 1] = temp
}

const handlePublish = () => {
  // 简单验证
  if (orchestrationList.value.length === 0) {
    alert('请至少添加一个节点！')
    return
  }
  console.log('Publishing Pipeline Data:', JSON.parse(JSON.stringify(orchestrationList.value)))
  alert('实训流程保存并发布成功！（F12查看产出数据）')
}
</script>

<style scoped>
/* =========== 整体布局 =========== */
.pipeline-create-wrapper { display: flex; height: calc(100vh - 64px); background: #F1F5F9; overflow: hidden; box-sizing: border-box; font-family: system-ui, -apple-system, sans-serif; }
.flex-1 { flex: 1; }
.w-full { width: 100%; }
.w-20 { width: 80px; }
.w-24 { width: 96px; }
.w-32 { width: 128px; }
.flex-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }

/* =========== 左侧栏 =========== */
.material-sidebar { width: 280px; background: #FFFFFF; border-right: 1px solid #E2E8F0; display: flex; flex-direction: column; position: relative; transition: width 0.3s; flex-shrink: 0; z-index: 10; }
.material-sidebar.is-collapsed { width: 0; border-right: none; }
.sidebar-inner { width: 280px; height: 100%; padding: 20px 16px; box-sizing: border-box; display: flex; flex-direction: column; overflow: hidden; }
.panel-main-title { font-size: 18px; font-weight: 800; color: #0F172A; margin-bottom: 16px; }
.sidebar-toggle { position: absolute; right: -16px; top: 50%; transform: translateY(-50%); width: 16px; height: 48px; background: #FFFFFF; border: 1px solid #E2E8F0; border-left: none; border-radius: 0 6px 6px 0; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #94A3B8; font-size: 10px; z-index: 21; }
.sidebar-toggle:hover { color: #4F46E5; }
.category-container { flex: 1; overflow-y: auto; padding-right: 4px; }
.category-container::-webkit-scrollbar { width: 4px; }
.category-container::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.cat-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 4px; cursor: pointer; }
.cat-label { font-weight: 600; color: #475569; font-size: 14px; }
.pure-chevron { width: 6px; height: 6px; border-right: 2px solid #94A3B8; border-bottom: 2px solid #94A3B8; transform: rotate(-45deg); transition: transform 0.3s; }
.pure-chevron.is-rotated { transform: rotate(45deg); }
.drawer-animator { display: grid; grid-template-rows: 0fr; transition: grid-template-rows 0.3s; }
.drawer-animator.is-open { grid-template-rows: 1fr; }
.node-list { overflow: hidden; display: flex; flex-direction: column; gap: 8px; padding-bottom: 8px; }
.draggable-node { padding: 10px 14px; background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px; cursor: grab; display: flex; align-items: center; gap: 10px; transition: all 0.2s; font-size: 14px; color: #334155; }
.draggable-node:hover { background: #EEF2FF; border-color: #818CF8; color: #4F46E5; }

/* =========== 中央编排区 =========== */
.canvas-container { flex: 1; display: flex; flex-direction: column; position: relative; }
.canvas-top-toolbar { display: flex; align-items: center; padding: 16px 24px; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); border-bottom: 1px solid #E2E8F0; z-index: 5; }
.pipeline-title { font-size: 16px; font-weight: 700; color: #0F172A; }
.flex-spacer { flex: 1; }
.secondary-action-btn { padding: 8px 20px; background: #4F46E5; color: #FFF; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.secondary-action-btn:hover { background: #4338CA; }

.pipeline-scroll-area { flex: 1; overflow-y: auto; padding: 40px; display: flex; flex-direction: column; align-items: center; }
.pipeline-queue { width: 100%; max-width: 560px; display: flex; flex-direction: column; align-items: center; }

/* 动画组配置 */
.list-anim-move, .list-anim-enter-active, .list-anim-leave-active { transition: all 0.4s ease; }
.list-anim-enter-from, .list-anim-leave-to { opacity: 0; transform: translateY(30px) scale(0.95); }
.list-anim-leave-active { position: absolute; }

.pipeline-item-wrapper { width: 100%; display: flex; flex-direction: column; align-items: center; }
.pipeline-node { width: 100%; background: #FFFFFF; border: 2px solid #E2E8F0; border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.pipeline-node:hover { border-color: #A5B4FC; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); transform: translateY(-2px); }
.pipeline-node.is-active { border-color: #4F46E5; background: #EEF2FF; box-shadow: 0 0 0 4px rgba(79,70,229,0.1); }

.node-index { width: 32px; height: 32px; background: #F1F5F9; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #64748B; flex-shrink: 0; }
.pipeline-node.is-active .node-index { background: #4F46E5; color: #FFF; }
.node-content { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.node-title { font-size: 16px; font-weight: 600; color: #0F172A; display: flex; align-items: center; gap: 8px; }
.node-type-tag { font-size: 12px; color: #64748B; background: #F1F5F9; padding: 2px 8px; border-radius: 4px; width: fit-content; }

.node-actions { display: flex; gap: 6px; opacity: 0; transition: opacity 0.2s; }
.pipeline-node:hover .node-actions { opacity: 1; }
.icon-btn { width: 28px; height: 28px; border-radius: 6px; border: 1px solid #E2E8F0; background: #FFF; color: #475569; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.icon-btn:hover:not(:disabled) { background: #F1F5F9; border-color: #CBD5E1; }
.icon-btn.danger:hover { background: #FEF2F2; color: #EF4444; border-color: #FECACA; }
.icon-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.pipeline-connector { height: 32px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94A3B8; }
.pipeline-connector .line { width: 2px; height: 20px; background: #CBD5E1; }
.pipeline-connector .arrow { font-size: 10px; margin-top: -4px; color: #CBD5E1; }

.empty-canvas-hint { text-align: center; color: #94A3B8; margin-top: 100px; }
.empty-canvas-hint .hint-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }
.drop-zone-indicator { width: 100%; max-width: 560px; border: 2px dashed #CBD5E1; border-radius: 12px; padding: 20px; text-align: center; color: #94A3B8; font-weight: 600; margin-top: 24px; background: rgba(255,255,255,0.5); }

/* =========== 右侧配置面板 =========== */
.config-panel { width: 340px; background: #FFFFFF; border-left: 1px solid #E2E8F0; display: flex; flex-direction: column; flex-shrink: 0; z-index: 10; box-shadow: -4px 0 15px rgba(0,0,0,0.03); }
.config-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #E2E8F0; background: #F8FAFC; }
.config-header h3 { font-size: 15px; font-weight: 700; color: #0F172A; margin: 0; }
.close-btn { background: none; border: none; font-size: 16px; cursor: pointer; color: #94A3B8; }
.close-btn:hover { color: #EF4444; }

.config-body { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.config-body::-webkit-scrollbar { width: 4px; }
.config-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }

.section-title { font-size: 13px; font-weight: 800; color: #64748B; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
.config-field { display: flex; flex-direction: column; gap: 8px; }
.config-field label { font-size: 13px; font-weight: 600; color: #334155; }
.required { color: #EF4444; margin-right: 4px; }
.config-input, .config-textarea, select.config-input { padding: 10px 12px; border: 1px solid #CBD5E1; border-radius: 6px; font-size: 13px; font-family: inherit; outline: none; background: #F8FAFC; transition: border 0.2s; color: #0F172A; }
.config-input:focus, .config-textarea:focus, select.config-input:focus { border-color: #4F46E5; background: #FFF; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
.config-textarea { resize: vertical; min-height: 70px; }

.list-item { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.list-item .idx { font-size: 12px; color: #94A3B8; font-weight: bold; width: 14px; }
.add-btn { background: none; border: 1px dashed #CBD5E1; color: #4F46E5; font-size: 13px; padding: 8px; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
.add-btn:hover { background: #EEF2FF; border-color: #A5B4FC; }

.hint { text-align: center; color: #94A3B8; font-size: 13px; padding: 20px 0; background: #F8FAFC; border-radius: 8px; border: 1px dashed #E2E8F0; }

/* AI 增强配置区 */
.ai-config-wrapper { margin-top: 16px; border: 1px solid #E0E7FF; border-radius: 8px; overflow: hidden; background: #FAFAFF; }
.ai-config-header { padding: 12px 16px; background: #EEF2FF; display: flex; justify-content: space-between; align-items: center; cursor: pointer; user-select: none; }
.ai-title { font-size: 13px; font-weight: 700; color: #4338CA; }
.ai-config-header .pure-chevron { border-color: #4338CA; }
.ai-config-body { padding: 16px; display: flex; flex-direction: column; gap: 14px; border-top: 1px solid #E0E7FF; }

/* 面板动画 */
.panel-slide-enter-active, .panel-slide-leave-active { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.panel-slide-enter-from, .panel-slide-leave-to { width: 0; opacity: 0; transform: translateX(20px); }
</style>