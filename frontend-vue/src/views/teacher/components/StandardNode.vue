<template>
  <div class="elegant-node" :class="{ 'is-selected': selected }">
    <Handle type="target" :position="Position.Left" class="handle-port left-port" />
    
    <div class="node-content">
      <div class="icon-wrapper">{{ data.icon || '✨' }}</div>
      <div class="text-wrapper">
        <span class="title">{{ data.label }}</span>
        <span class="subtitle">{{ data.type === 'start' ? '流程起点' : '执行节点' }}</span>
      </div>
    </div>

    <Handle type="source" :position="Position.Right" class="handle-port right-port" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: { type: Object, required: true },
  selected: { type: Boolean, default: false }
})
</script>

<style scoped>
.elegant-node {
  min-width: 180px;
  background: rgba(255, 255, 255, 0.85); /* 半透明质感 */
  backdrop-filter: blur(8px);
  border: 1px solid rgba(229, 231, 235, 0.5); /* 极弱的边框 */
  border-radius: 24px; /* 优雅的大圆角 */
  padding: 8px 16px 8px 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04), 0 2px 4px rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.elegant-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.12);
}

.elegant-node.is-selected {
  border: 1px solid #4F46E5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

.node-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  width: 36px;
  height: 36px;
  background: #EEF2FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.text-wrapper {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 14px;
  font-weight: 600;
  color: #1F2937;
}

.subtitle {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 2px;
}

/* 优雅的连接点 */
.handle-port {
  width: 10px;
  height: 10px;
  background: #fff;
  border: 2px solid #4F46E5;
  border-radius: 50%; /* 纯圆 */
  transition: all 0.2s;
  opacity: 0; /* 默认隐藏，悬浮时显示以保持画面干净 */
}

.elegant-node:hover .handle-port,
.elegant-node.is-selected .handle-port {
  opacity: 1;
}

.left-port { left: -5px; }
.right-port { right: -5px; }
</style>