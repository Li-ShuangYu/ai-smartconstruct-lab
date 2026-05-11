<template>
  <div class="builder">
    <h2 class="title">学生分组</h2>
    <p class="hint">共 {{ groupCount }} 个小组，请选择加入</p>
    <n-grid :x-gap="16" :y-gap="16" cols="1 s:2 m:3 l:4">
      <n-gi v-for="g in groups" :key="g.id">
        <n-card :class="['group-card', { selected: selected === g.id }]" hoverable @click="selectGroup(g.id)" size="small">
          <template #header>{{ g.name }}</template>
          <p>{{ g.desc }}</p>
          <n-tag type="info" size="small">组长: {{ g.leader }}</n-tag>
          <div class="members"><n-tag v-for="m in g.members" :key="m" size="tiny">{{ m }}</n-tag></div>
        </n-card>
      </n-gi>
    </n-grid>
    <div v-if="assigned" class="result-bar"><n-alert type="success">系统已分配: 第 {{ assigned }} 组</n-alert></div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleConfirm">确认分组</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NCard, NTag, NGrid, NGi, NAlert } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const groupCount = (props.config as any)?.group_count || 4
const selected = ref<number | null>(null); const assigned = ref<number | null>(null)
const groups = Array.from({ length: groupCount }, (_, i) => ({
  id: i + 1, name: `第${i + 1}组`, desc: ['低功耗优化', '侧信道防护', '抗重放攻击', '后量子适配'][i] || `方向${i + 1}`,
  leader: ['张三', '李四', '王五', '赵六'][i] || '待定', members: Array.from({ length: 3 + i % 3 }, (_, j) => `成员${String.fromCharCode(65 + i * 3 + j)}`)
}))
const canProceed = computed(() => selected.value !== null || assigned.value !== null)
function selectGroup(id: number) { selected.value = id }
function handleConfirm() { emit('step-complete'); emit('update-data', { groupId: selected.value || assigned.value }) }
</script>
<style scoped>
.builder { max-width: 900px; margin: 0 auto; padding: 40px 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 24px 0; }
.group-card { cursor: pointer; transition: all .2s; } .group-card.selected { border-color: #4F46E5; background: #F5F7FF; }
.members { display: flex; gap: 4px; flex-wrap: wrap; margin-top: 8px; }
.result-bar { margin: 16px 0; }
.actions { display: flex; justify-content: center; margin-top: 24px; }
</style>
