<template>
  <div class="template-preview">
    <header class="preview-header">
      <div class="header-left">
        <n-button quaternary circle @click="handleBack">
          <template #icon><span class="back-icon">←</span></template>
        </n-button>
        <h1 class="preview-title">{{ previewData?.template_name || '模板预览' }}</h1>
        <n-tag v-if="previewData" :type="templateAiStatusType" size="small">
          {{ templateAiStatusLabel }}
        </n-tag>
      </div>
      <div class="header-right">
        <n-switch v-model:value="studentView" size="medium">
          <template #checked>学生视角</template>
          <template #unchecked>教师视角</template>
        </n-switch>
      </div>
    </header>

    <n-spin :show="loading" class="preview-body">
      <div v-if="!loading && !previewData" class="empty-state">
        <p>暂无预览数据</p>
      </div>

      <div v-if="previewData && previewData.phases.length === 0" class="empty-state">
        <p>该模板尚未编排任何节点，请先在编排页面添加节点后再预览。</p>
      </div>

      <div v-if="previewData && previewData.phases.length > 0" class="phases-container">
        <n-collapse :default-expanded-names="expandedPhases">
          <n-collapse-item
            v-for="phase in previewData.phases"
            :key="phase.phase_id"
            :title="phase.phase_name"
            :name="phase.phase_id"
          >
            <template #header>
              <div class="phase-header">
                <span class="phase-name">{{ phase.phase_name }}</span>
                <n-tag size="tiny" :bordered="false" type="info">
                  {{ phase.nodes.length }} 个节点
                </n-tag>
              </div>
            </template>

            <div class="nodes-grid">
              <div
                v-for="node in phase.nodes"
                :key="node.node_id"
                class="node-card"
                :class="{ expanded: expandedNodeId === node.node_id }"
                @click="toggleNodeExpand(node.node_id)"
              >
                <!-- Node summary row -->
                <div class="node-summary">
                  <span class="node-icon">{{ getNodeIcon(node.node_type) }}</span>
                  <div class="node-info">
                    <span class="node-name">{{ node.node_name }}</span>
                    <span class="node-type-label">{{ getNodeTypeLabel(node.node_type) }}</span>
                  </div>
                  <div class="node-meta">
                    <span v-if="!studentView" class="config-summary">
                      {{ formatConfigSummary(node.config_summary) }}
                    </span>
                    <n-tag
                      :type="nodeAiStatusType(node.ai_status)"
                      size="small"
                      :bordered="false"
                    >
                      {{ nodeAiStatusLabel(node.ai_status) }}
                    </n-tag>
                  </div>
                  <span class="expand-arrow" :class="{ rotated: expandedNodeId === node.node_id }">▾</span>
                </div>

                <!-- Expanded detail -->
                <div v-if="expandedNodeId === node.node_id" class="node-detail" @click.stop>
                  <!-- Student view: simulate student page rendering -->
                  <template v-if="studentView">
                    <div class="student-view-section">
                      <div v-if="node.ai_status === 2 && node.ai_generated_content" class="ai-content-block">
                        <div class="ai-content-header">
                          <span class="ai-badge">✨ AI 生成内容</span>
                        </div>
                        <div class="ai-content-body">
                          <template v-for="(value, key) in node.ai_generated_content" :key="key">
                            <div class="ai-field">
                              <span class="ai-field-label">{{ formatFieldLabel(String(key)) }}</span>
                              <div class="ai-field-value">{{ formatFieldValue(value) }}</div>
                            </div>
                          </template>
                        </div>
                      </div>
                      <div v-else class="student-placeholder">
                        <p v-if="node.ai_status === 0">⏳ AI 内容待生成，发布模板后自动处理</p>
                        <p v-else-if="node.ai_status === 1">⏳ AI 正在处理中...</p>
                        <p v-else-if="node.ai_status === 3">⚠️ AI 处理失败，请重试</p>
                        <p v-else>暂无可预览的学生端内容</p>
                      </div>
                    </div>
                  </template>

                  <!-- Teacher view: show full config and AI content -->
                  <template v-else>
                    <div class="config-detail-section">
                      <h4 class="detail-section-title">节点配置</h4>
                      <div class="config-fields">
                        <template v-for="(value, key) in node.config_summary" :key="key">
                          <div class="config-field">
                            <span class="config-field-label">{{ formatFieldLabel(String(key)) }}</span>
                            <span class="config-field-value">{{ formatFieldValue(value) }}</span>
                          </div>
                        </template>
                        <div v-if="Object.keys(node.config_summary).length === 0" class="no-config">
                          暂无配置信息
                        </div>
                      </div>
                    </div>

                    <div v-if="node.ai_status === 2 && node.ai_generated_content" class="ai-content-section">
                      <h4 class="detail-section-title">
                        <span class="ai-badge">✨ AI 生成内容</span>
                      </h4>
                      <div class="ai-content-body">
                        <template v-for="(value, key) in node.ai_generated_content" :key="key">
                          <div class="ai-field">
                            <span class="ai-field-label">{{ formatFieldLabel(String(key)) }}</span>
                            <div class="ai-field-value">{{ formatFieldValue(value) }}</div>
                          </div>
                        </template>
                      </div>
                    </div>

                    <div v-else-if="node.ai_status !== 2" class="ai-status-note">
                      <span v-if="node.ai_status === 0">💡 发布模板后 AI 将自动生成内容</span>
                      <span v-else-if="node.ai_status === 1">⏳ AI 正在处理中...</span>
                      <span v-else-if="node.ai_status === 3">❌ AI 处理失败</span>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </n-collapse-item>
        </n-collapse>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCollapse, NCollapseItem, NTag, NSpin, NSwitch, useMessage } from 'naive-ui'
import { getTemplatePreview } from '@/services/modules/orchestration.service'
import { MATERIAL_CATEGORIES } from '@/views/teacher/data/node-templates'
import type { TemplatePreviewData } from '@/services/types/orchestration'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(false)
const previewData = ref<TemplatePreviewData | null>(null)
const studentView = ref(false)
const expandedNodeId = ref<string>('')

const templateId = computed(() => String(route.params.templateId || route.query.templateId || ''))

const expandedPhases = computed(() => {
  if (!previewData.value) return []
  return previewData.value.phases.map(p => p.phase_id)
})

const templateAiStatusLabel = computed(() => {
  if (!previewData.value) return ''
  const s = previewData.value.ai_status
  if (s === 0) return '草稿'
  if (s === 1) return 'AI处理中'
  if (s === 2) return '已就绪'
  return '异常'
})

const templateAiStatusType = computed<'default' | 'warning' | 'success' | 'error'>(() => {
  if (!previewData.value) return 'default'
  const s = previewData.value.ai_status
  if (s === 0) return 'default'
  if (s === 1) return 'warning'
  if (s === 2) return 'success'
  return 'error'
})

// Build a flat lookup map from MATERIAL_CATEGORIES
const nodeTypeMap = computed(() => {
  const map: Record<string, { label: string; icon: string }> = {}
  for (const category of MATERIAL_CATEGORIES) {
    for (const item of category.items) {
      map[item.type] = { label: item.label, icon: item.icon }
    }
  }
  // Add learning_survey which may not be in MATERIAL_CATEGORIES yet
  if (!map['learning_survey']) {
    map['learning_survey'] = { label: '学情调查', icon: '📊' }
  }
  return map
})

function getNodeIcon(nodeType: string): string {
  return nodeTypeMap.value[nodeType]?.icon || '📦'
}

function getNodeTypeLabel(nodeType: string): string {
  return nodeTypeMap.value[nodeType]?.label || nodeType
}

function nodeAiStatusLabel(status: number): string {
  if (status === 0) return '待处理'
  if (status === 1) return '处理中'
  if (status === 2) return '已完成'
  return '失败'
}

function nodeAiStatusType(status: number): 'default' | 'warning' | 'success' | 'error' {
  if (status === 0) return 'default'
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  return 'error'
}

function formatConfigSummary(summary: Record<string, unknown>): string {
  const entries = Object.entries(summary)
  if (entries.length === 0) return ''
  const parts = entries.slice(0, 3).map(([key, val]) => {
    const label = formatFieldLabel(key)
    const value = typeof val === 'boolean' ? (val ? '是' : '否') : String(val ?? '-')
    return `${label}: ${value.length > 20 ? value.slice(0, 20) + '...' : value}`
  })
  if (entries.length > 3) parts.push(`+${entries.length - 3}项`)
  return parts.join(' | ')
}

function formatFieldLabel(key: string): string {
  const labelMap: Record<string, string> = {
    welcome_text: '欢迎语',
    flow_overview_json: '流程概览',
    summary_text: '摘要',
    key_points: '关键知识点',
    questions: '题目列表',
    enable_ai_summary: 'AI摘要',
    enable_ai_welcome: 'AI欢迎语',
    enable_ai_key_points: 'AI重点提取',
    enable_ai_grading: 'AI批改',
    enable_ai_code_review: 'AI代码审查',
    enable_ai_task_split: 'AI任务拆解',
    enable_ai_generate_map: 'AI生成导图',
    resource_id: '关联资源',
    topic_name: '主题名称',
    exam_duration: '考试时长',
    total_score: '总分',
    group_method: '分组方式',
    min_reading_duration: '最低阅读时长',
    source_mode: '来源模式',
    question_count: '题目数量'
  }
  return labelMap[key] || key.replace(/_/g, ' ')
}

function formatFieldValue(value: unknown): string {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (Array.isArray(value)) {
    if (value.length === 0) return '(空)'
    // If array of objects with 'title' or 'name', show those
    if (typeof value[0] === 'object' && value[0] !== null) {
      const items = value.slice(0, 5).map((item: Record<string, unknown>) =>
        String(item.title || item.name || item.question || JSON.stringify(item).slice(0, 50))
      )
      const suffix = value.length > 5 ? ` ... 共${value.length}项` : ''
      return items.join('；') + suffix
    }
    return value.join('、')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}

function toggleNodeExpand(nodeId: string) {
  expandedNodeId.value = expandedNodeId.value === nodeId ? '' : nodeId
}

function handleBack() {
  router.back()
}

async function fetchPreview() {
  if (!templateId.value) {
    message.warning('缺少模板ID')
    return
  }
  loading.value = true
  try {
    const res = await getTemplatePreview(templateId.value)
    if (res.code === 200) {
      previewData.value = res.data
    } else {
      message.error(res.message || '获取预览数据失败')
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    message.error(err?.response?.data?.message || '获取预览数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPreview()
})
</script>

<style scoped>
.template-preview {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 24px 32px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-icon {
  font-size: 18px;
  font-weight: bold;
}

.preview-title {
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-body {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #94A3B8;
  font-size: 14px;
}

.phases-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.phase-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.phase-name {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

.nodes-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

.node-card {
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.node-card:hover {
  border-color: #CBD5E1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.node-card.expanded {
  border-color: #818CF8;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08);
}

.node-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.node-icon {
  font-size: 24px;
  flex-shrink: 0;
  width: 36px;
  text-align: center;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 120px;
}

.node-name {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.node-type-label {
  font-size: 12px;
  color: #94A3B8;
}

.node-meta {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.config-summary {
  font-size: 12px;
  color: #64748B;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.expand-arrow {
  font-size: 14px;
  color: #94A3B8;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.expand-arrow.rotated {
  transform: rotate(180deg);
}

.node-detail {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #F1F5F9;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin: 0 0 12px 0;
}

.config-detail-section {
  margin-bottom: 16px;
}

.config-fields {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 8px;
}

.config-field {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 6px 10px;
  background: #F8FAFC;
  border-radius: 6px;
}

.config-field-label {
  font-size: 12px;
  color: #64748B;
  font-weight: 500;
  white-space: nowrap;
}

.config-field-value {
  font-size: 13px;
  color: #1E293B;
  word-break: break-all;
}

.no-config {
  font-size: 13px;
  color: #94A3B8;
  padding: 8px 0;
}

.ai-content-section {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #EEF2FF 0%, #F5F3FF 100%);
  border-radius: 10px;
  border: 1px solid #E0E7FF;
}

.ai-content-block {
  padding: 16px;
  background: linear-gradient(135deg, #EEF2FF 0%, #F5F3FF 100%);
  border-radius: 10px;
  border: 1px solid #E0E7FF;
}

.ai-content-header {
  margin-bottom: 12px;
}

.ai-badge {
  font-size: 12px;
  font-weight: 600;
  color: #4F46E5;
  background: #E0E7FF;
  padding: 4px 10px;
  border-radius: 6px;
}

.ai-content-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-field {
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #E0E7FF;
}

.ai-field-label {
  font-size: 11px;
  font-weight: 600;
  color: #6366F1;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ai-field-value {
  font-size: 13px;
  color: #1E293B;
  margin-top: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-status-note {
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 13px;
  color: #64748B;
}

.student-view-section {
  padding: 4px 0;
}

.student-placeholder {
  padding: 16px;
  background: #F8FAFC;
  border-radius: 8px;
  text-align: center;
  color: #64748B;
  font-size: 13px;
}

.student-placeholder p {
  margin: 0;
}
</style>
