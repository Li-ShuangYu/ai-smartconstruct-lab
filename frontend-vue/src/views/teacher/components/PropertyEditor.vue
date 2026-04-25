<template>
  <div class="property-editor">
    <div class="editor-header">
      <h3>节点属性配置</h3>
      <p class="node-name">{{ currentNode?.data?.label || '未选中节点' }}</p>
    </div>

    <div v-if="currentNode && currentNode.data.type === 'ai_grading'" class="form-container">
      <div class="schema-field" v-for="field in aiGradingSchema" :key="field.key">
        <label>{{ field.label }}</label>
        
        <select v-if="field.type === 'select'" v-model="formData[field.key]" class="input-base">
          <option v-for="opt in field.options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>

        <textarea 
          v-else-if="field.type === 'matrix'" 
          v-model="formData[field.key]" 
          class="input-base textarea-base"
          rows="5"
          placeholder="例如：[{ dimension: '代码规范', weight: 0.3 }]"
        ></textarea>
      </div>

      <button class="save-btn" @click="saveConfig">保存写入状态机</button>
    </div>
    
    <div v-else-if="currentNode" class="empty-hint">
      当前节点暂无复杂配置
    </div>
    <div v-else class="empty-hint">
      请在左侧画布选中一个节点
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useTrainingStore } from '@/stores/modules/training.store'

const store = useTrainingStore()
const currentNode = computed(() => store.nodes.find(n => n.id === store.selectedNodeId))

// 模拟从后端获取的 JSON Schema
const aiGradingSchema = [
  { key: 'engine', label: '大模型引擎', type: 'select', options: [{ label: 'GPT-4 Turbo', value: 'gpt-4' }, { label: 'Qwen Max', value: 'qwen' }] },
  { key: 'prompt_strategy', label: '苏格拉底引导策略', type: 'select', options: [{ label: '直接指出错误', value: 'direct' }, { label: '反问启发 (不给代码)', value: 'socratic' }] },
  { key: 'rubric_matrix', label: '二维评分矩阵 (JSON)', type: 'matrix' }
]

// 隔离的表单草稿状态
const formData = ref<Record<string, any>>({})

// 监听选中节点变化，回显数据
watch(() => store.selectedNodeId, (newId) => {
  if (newId) {
    formData.value = { ...store.nodeConfigMap[newId] }
  }
})

const saveConfig = () => {
  if (store.selectedNodeId) {
    store.updateNodeConfig(store.selectedNodeId, formData.value)
    alert('配置已同步至 Store DSL')
  }
}
</script>

<style scoped>
.property-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.editor-header { padding-bottom: 16px; border-bottom: 1px solid #E5E7EB; margin-bottom: 16px; }
.editor-header h3 { font-size: 14px; font-weight: 600; margin: 0 0 4px 0; }
.node-name { font-size: 12px; color: #6B7280; margin: 0; }

.schema-field { margin-bottom: 16px; }
.schema-field label { display: block; font-size: 12px; color: #374151; margin-bottom: 8px; font-weight: 500; }

/* 工业级输入框 */
.input-base { width: 100%; padding: 8px; border: 1px solid #D1D5DB; border-radius: 4px; font-size: 13px; outline: none; box-sizing: border-box;}
.input-base:focus { border-color: #4F46E5; box-shadow: 0 0 0 1px #4F46E5; }
.textarea-base { resize: vertical; font-family: monospace; }

.save-btn { width: 100%; background: #111827; color: #fff; border: none; padding: 10px; border-radius: 4px; cursor: pointer; font-weight: 500; transition: background 0.2s;}
.save-btn:hover { background: #374151; }
.empty-hint { font-size: 13px; color: #9CA3AF; text-align: center; margin-top: 40px; }
</style>