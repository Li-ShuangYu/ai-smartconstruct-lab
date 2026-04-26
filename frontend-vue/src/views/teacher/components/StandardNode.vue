<template>
  <div class="integrated-node" :class="{ selected: selected }">
<Handle 
  id="target"
  v-if="data.type !== 'start'" 
  type="target" 
  :position="Position.Left" 
  class="custom-handle target-handle" 
/>
    
    <header class="node-header">
      <div class="header-left">
        <span class="icon">{{ data.icon }}</span>
        <h3 class="title">{{ data.label }}</h3>
      </div>
      <div class="header-actions">
        <button class="action-icon delete-btn nodrag" title="删除此节点" @click="handleDelete">🗑️</button>
      </div>
    </header>

    <main class="node-body">
      <template v-if="data.type === 'grouping'">
        <div class="form-row">
          <div class="form-item flex-1">
            <label>分组数量</label>
            <input type="number" v-model="data.groupCount" class="node-input nodrag" min="1" />
          </div>
          <div class="form-item flex-1">
            <label>分组方式</label>
            <select v-model="data.groupMethod" class="node-select nodrag">
              <option value="random">随机分配</option>
              <option value="score">按成绩分配</option>
              <option value="manual">手动分配</option>
            </select>
          </div>
        </div>
      </template>

      <template v-else-if="data.type === 'resource_read'">
        <div class="form-item">
          <label>关联学习资源</label>
          <select v-model="data.resourceName" class="node-select nodrag">
            <option value="" disabled>请选择关联资源</option>
            <option value="SM4算法详解.pdf">《SM4算法详解》</option>
            <option value="抗重放攻击实验指导.docx">《抗重放攻击实验指导》</option>
            <option value="Vue核心原理.mp4">视频: Vue核心原理</option>
          </select>
        </div>
      </template>

      <template v-else-if="data.type === 'task_distribute'">
        <div class="form-item">
          <label>任务标题</label>
          <input type="text" v-model="data.taskTitle" class="node-input nodrag" placeholder="输入任务名称" />
        </div>
        <div class="form-item">
          <label>任务描述</label>
          <textarea v-model="data.taskDesc" class="node-textarea nodrag" rows="2"></textarea>
        </div>
      </template>

      <template v-else-if="['req_upload', 'mindmap_preview', 'mindmap_draw', 'knowledge_eval'].includes(data.type)">
        <div class="form-item">
          <label>设定业务场景</label>
          <textarea v-model="data.scenario" class="node-textarea nodrag" rows="2" placeholder="设定需要学员分析的背景..."></textarea>
        </div>
        <div class="form-item" v-if="data.type === 'req_upload'">
          <label class="checkbox-label">
            <input type="checkbox" v-model="data.showWordCloud" /> 开启高频词云分析
          </label>
        </div>
      </template>
      
      <template v-else-if="data.type === 'plan_upload'">
        <div class="form-item">
          <label>上传要求说明</label>
          <input type="text" v-model="data.uploadReq" class="node-input nodrag" />
        </div>
        <div class="form-item">
          <label>允许的格式后缀</label>
          <input type="text" v-model="data.format" class="node-input nodrag" placeholder="如: pdf,docx,zip" />
        </div>
      </template>

      <template v-else-if="data.type === 'homework'">
        <div class="form-row">
          <div class="form-item flex-1">
            <label>生成方式</label>
            <select v-model="data.isAIGenerated" class="node-select nodrag">
              <option :value="true">AI 智能组卷</option>
              <option :value="false">题库手动选择</option>
            </select>
          </div>
          <div class="form-item" style="width: 80px;">
            <label>生成题数</label>
            <input type="number" v-model="data.questionCount" class="node-input nodrag" min="1" />
          </div>
        </div>
      </template>

      <template v-else-if="data.type === 'exam'">
        <div class="form-row">
          <div class="form-item flex-1">
            <label>考试时长 (分钟)</label>
            <input type="number" v-model="data.duration" class="node-input nodrag" min="1" />
          </div>
          <div class="form-item flex-1">
            <label>卷面总分</label>
            <input type="number" v-model="data.totalScore" class="node-input nodrag" min="1" />
          </div>
        </div>
      </template>

      <template v-else-if="['student_peer_review', 'inter_group_review'].includes(data.type)">
        <div class="form-item">
          <label>互评维度 (逗号分隔)</label>
          <input type="text" v-model="data.dimension" class="node-input nodrag" placeholder="如: 代码规范,创新性" />
        </div>
      </template>
      <template v-else-if="data.type === 'teacher_eval'">
        <div class="form-item">
          <label>总评成绩权重比</label>
          <div class="weight-control">
            <input type="range" v-model="data.weight" min="0" max="1" step="0.1" class="nodrag" />
            <span>{{ (data.weight * 100).toFixed(0) }}%</span>
          </div>
        </div>
      </template>

      <template v-else-if="data.type === 'theory_class'">
        <div class="form-item">
          <label>授课主题</label>
          <input type="text" v-model="data.topic" class="node-input nodrag" placeholder="输入理论课主题" />
        </div>
      </template>
      <template v-else-if="data.type === 'coding_class'">
        <div class="form-row">
          <div class="form-item flex-1">
            <label>运行环境</label>
            <select v-model="data.envType" class="node-select nodrag">
              <option value="k8s">K8s 容器环境</option>
              <option value="web">轻量 Web 模拟</option>
            </select>
          </div>
          <div class="form-item flex-1">
            <label>底层镜像</label>
            <input type="text" v-model="data.image" class="node-input nodrag" />
          </div>
        </div>
      </template>

      <template v-else>
        <div class="desc-text">{{ data.desc || '流程节点' }}</div>
      </template>

    </main>
    
   <Handle 
  id="source"
  v-if="data.type !== 'end'" 
  type="source" 
  :position="Position.Right" 
  class="custom-handle source-handle" 
/>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position, useVueFlow } from '@vue-flow/core'

const { removeNodes } = useVueFlow()

const props = defineProps({
  id: { type: String, required: true },
  data: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})

const handleDelete = () => {
  removeNodes([props.id])
}
</script>

<style scoped>
.integrated-node {
  width: 320px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
  transition: all 0.2s;
  font-size: 15px;
}
.integrated-node.selected { border-color: #818CF8; box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.15), 0 10px 25px rgba(0,0,0,0.08); }

.node-header { padding: 14px 16px; border-bottom: 1px solid #F1F5F9; display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; border-radius: 12px 12px 0 0; }
.header-left { display: flex; align-items: center; gap: 10px; }
.icon { background: #FFFFFF; border: 1px solid #E2E8F0; padding: 6px; border-radius: 8px; font-size: 18px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.title { font-size: 16px; font-weight: 700; color: #0F172A; margin: 0; }

.header-actions .action-icon { background: transparent; border: none; cursor: pointer; color: #94A3B8; font-size: 16px; }
.header-actions .delete-btn:hover { color: #EF4444; transform: scale(1.15); transition: all 0.2s; }

.node-body { padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.form-item { display: flex; flex-direction: column; gap: 8px; }
.form-row { display: flex; gap: 12px; }
.flex-1 { flex: 1; }

.form-item label { font-size: 13px; font-weight: 700; color: #475569; }
.checkbox-label { display: flex; align-items: center; gap: 6px; cursor: pointer; font-weight: 500 !important; }

.node-select, .node-input, .node-textarea {
  width: 100%; padding: 10px 12px; border: 1px solid #E2E8F0; border-radius: 8px; background: #F8FAFC; color: #0F172A; font-size: 14px; font-family: inherit; transition: border-color 0.2s; box-sizing: border-box;
}
.node-select:focus, .node-input:focus, .node-textarea:focus { outline: none; border-color: #818CF8; background: #FFFFFF; box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.1); }
.node-textarea { resize: vertical; min-height: 60px; }

.weight-control { display: flex; align-items: center; gap: 12px; }
.weight-control input[type="range"] { flex: 1; cursor: pointer; }
.weight-control span { font-size: 14px; font-weight: 700; color: #4F46E5; width: 40px; }

.desc-text { font-size: 13px; color: #94A3B8; text-align: center; padding: 10px 0; font-style: italic; }

.custom-handle { width: 12px; height: 12px; background: #FFFFFF; border: 2px solid #818CF8; border-radius: 50%; transition: all 0.2s; }
.custom-handle:hover { background: #818CF8; box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.2); }
.target-handle { left: -6px; }
.source-handle { right: -6px; }
</style>