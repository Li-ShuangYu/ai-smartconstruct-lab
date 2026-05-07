<template>
  <div class="dashboard">
    <h2 class="title">成绩单</h2>
    <n-card v-if="!scorePublished" class="wait-card"><n-spin /><p>等待教师发布成绩...</p></n-card>
    <n-card v-else class="score-card">
      <div class="total-score">{{ totalScore }} <span>/ 100</span></div>
      <n-divider />
      <div class="details">
        <div v-for="(v, k) in scoreDetails" :key="k" class="detail-row"><span>{{ k }}</span><span>{{ v }}</span></div>
      </div>
      <n-divider />
      <p class="comment">评语: {{ mockComment }}</p>
    </n-card>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleNext">查看完成，下一步</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NButton, NCard, NDivider, NSpin } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const scorePublished = ref(false); const totalScore = ref(85); const mockComment = ref('整体表现优秀，方案设计完整，安全性考虑充分。建议在编码实现层面进一步优化算法效率。')
const scoreDetails = { '保密性': '90', '完整性': '88', '可用性': '78', '创新性': '84', '成本控制': '85' }
const canProceed = computed(() => scorePublished.value)
onMounted(() => { setTimeout(() => { scorePublished.value = true }, 1500) })
function handleNext() { emit('step-complete') }
</script>
<style scoped>
.dashboard { max-width: 600px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.wait-card { text-align: center; padding: 40px; } .wait-card p { color: #64748B; }
.score-card { text-align: center; } .total-score { font-size: 48px; font-weight: 900; color: #4F46E5; } .total-score span { font-size: 18px; color: #94A3B8; }
.detail-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; color: #475569; }
.comment { color: #64748B; font-size: 14px; line-height: 1.6; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
