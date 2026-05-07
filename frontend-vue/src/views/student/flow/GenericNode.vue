<template>
  <div class="node-card">
    <div class="icon-wrap">{{ icon }}</div>
    <h2>{{ config?.name || '流程节点' }}</h2>
    <p class="hint">{{ desc }}</p>
    <n-button type="primary" style="margin-top:16px" @click="emit('next')">下一步</n-button>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NButton } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; taskId?: number }>()
const emit = defineEmits<{ next: [] }>()
const typeMap: Record<string, { icon: string; desc: string }> = {
  START: { icon: '▶️', desc: '实训流程即将开始，请按步骤完成各项任务' },
  END: { icon: '✅', desc: '你已完成所有节点，点击按钮提交实训' },
  TASK: { icon: '📋', desc: props.config?.config?.task_content || props.config?.config?.taskTitle || '请完成下发任务' },
  CODING: { icon: '💻', desc: `环境: ${props.config?.config?.coding_env_id || props.config?.config?.envType || 'K8s'}` },
  MINDMAP: { icon: '🧠', desc: props.config?.config?.mindmap_scene || props.config?.config?.scenario || '思维导图场景' },
  PEER_REVIEW: { icon: '🤝', desc: `评价维度: ${props.config?.config?.review_content || props.config?.config?.dimension || '综合表现'}` },
  EVAL: { icon: '🏆', desc: `权重: ${((props.config?.config?.weight || 0.5) * 100).toFixed(0)}%` },
}
const nt = (props.config?.node_type || '').toUpperCase().replace(/[ _-]/g, '_')
const match = Object.entries(typeMap).find(([k]) => nt.includes(k))
const icon = computed(() => match?.[1]?.icon || '📋')
const desc = computed(() => match?.[1]?.desc || formatConfig(props.config?.config))
function formatConfig(cfg: Record<string, unknown>): string {
  if (!cfg) return '无配置'
  return Object.entries(cfg).filter(([,v]) => v != null && v !== '').map(([k,v]) => `${k}: ${typeof v === 'object' ? JSON.stringify(v) : v}`).join('；')
}
</script>
<style scoped>
.node-card { text-align: center; padding: 40px; background: #fff; border-radius: 16px; border: 1px solid #E2E8F0; max-width: 500px; margin: 0 auto; }
.icon-wrap { font-size: 48px; margin-bottom: 16px; }
h2 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.hint { color: #64748B; font-size: 13px; margin: 8px 0; line-height: 1.5; }
</style>
