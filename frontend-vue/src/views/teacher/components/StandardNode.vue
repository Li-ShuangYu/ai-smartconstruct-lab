<template>
  <div class="integrated-node" :class="{ selected: selected }">
    <Handle type="target" :position="Position.Left" class="custom-handle target-handle" />
    <header class="node-header">
      <div class="header-left">
        <span class="icon">{{ data.icon }}</span>
        <h3 class="title">{{ data.label }}</h3>
      </div>
      <div class="header-actions">
        <button class="action-icon" title="运行测试">▶</button>
        <button class="action-icon" title="更多">⋮</button>
        <button class="action-icon delete-btn nodrag" title="删除此节点" @click="handleDelete">🗑️</button>
      </div>
    </header>

    <main class="node-body">
      <div class="form-item">
        <label>选择模型模型</label>
        <select v-model="data.modelSelect" class="node-select nodrag">
          <option value="qwen-max">Qwen-Max</option>
          <option value="qwen-turbo">Qwen-Turbo</option>
          <option value="gpt-4">GPT-4</option>
        </select>
      </div>

      <div class="form-item">
        <label>系统提示词 (System Prompt)</label>
        <textarea 
          v-model="data.systemPrompt" 
          class="node-textarea nodrag" 
          placeholder="请输入指令..."
          rows="3"
        ></textarea>
      </div>

      <div class="form-row">
        <div class="form-item">
          <label>温度设置 (Temperature)</label>
          <input type="number" v-model="data.temperature" class="node-input nodrag" step="0.1" min="0" max="1" />
        </div>
      </div>
    </main>
    
    <Handle type="source" :position="Position.Right" class="custom-handle source-handle" />
  </div>
</template>

<script setup lang="ts">
// 修改点 2：引入 useVueFlow
import { Handle, Position, useVueFlow } from '@vue-flow/core'

// 提取删除节点的方法
const { removeNodes } = useVueFlow()

const props = defineProps({
  // 修改点 3：必须接收 id 属性，作为删除的凭证
  id: {
    type: String,
    required: true
  },
  data: {
    type: Object,
    required: true
  },
  selected: {
    type: Boolean,
    default: false
  }
})

// 修改点 4：定义点击删除时的执行逻辑
const handleDelete = () => {
  removeNodes([props.id])
}
</script>


<style scoped>
/* 还原大尺寸配置节点外观 */
.integrated-node {
  width: 320px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
  transition: all 0.2s;
  font-size: 15px; /* 全局15px基准 */
}

.integrated-node.selected {
  border-color: #818CF8;
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.15), 0 10px 25px rgba(0,0,0,0.08);
}

.node-header {
  padding: 16px;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8FAFC;
  border-radius: 12px 12px 0 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  padding: 6px;
  border-radius: 8px;
  font-size: 18px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}

.header-actions .action-icon {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #94A3B8;
  font-size: 16px;
}
.header-actions .action-icon:hover { color: #0F172A; }

/* 修改点 5：为删除按钮添加悬停变红的警示交互 */
.header-actions .delete-btn:hover {
  color: #EF4444;
  transform: scale(1.15);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.node-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-item label {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
}

/* 使用 nodrag 类防止在表单中拖拽时意外移动节点 */
.node-select,
.node-input,
.node-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  background: #F8FAFC;
  color: #0F172A;
  font-size: 15px;
  font-family: inherit;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.node-select:focus,
.node-input:focus,
.node-textarea:focus {
  outline: none;
  border-color: #818CF8;
  background: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.1);
}

.node-textarea { resize: vertical; min-height: 80px; }

/* 优雅的连接点 */
/* 优雅稳定的连接点 */
.custom-handle {
  width: 12px;
  height: 12px;
  background: #FFFFFF;
  border: 2px solid #818CF8;
  border-radius: 50%;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-handle:hover {
  background: #818CF8;
  /* 重点：用光晕替代 scale 放大，彻底杜绝 transform 覆盖导致的跳动 */
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.2);
}

/* 确保连线点绝对居中，不再受 hover 干扰 */
.target-handle { left: -6px; }
.source-handle { right: -6px; }
</style>