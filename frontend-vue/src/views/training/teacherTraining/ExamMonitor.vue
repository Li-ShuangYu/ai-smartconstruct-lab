<template>
  <div class="monitor exam-bg"><h3 class="title">考试监控大屏</h3>
    <n-statistic label="实时交卷率" :value="`${examData?.submitRate||0}%`" v-if="examData" />
    <div v-if="cheatWarnings && cheatWarnings.length" class="warnings"><n-alert v-for="w in cheatWarnings" :key="w.studentId" type="error" :title="`${w.realName} 切屏 ${w.count} 次`"><n-button size="tiny" @click="emit('intervene','warn',{studentId:w.studentId})">发警告信</n-button><n-button size="tiny" type="error" style="margin-left:4px" @click="emit('intervene','terminate_exam',{studentId:w.studentId})">终止考试</n-button></n-alert></div>
    <n-empty v-else description="无切屏告警" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NStatistic, NAlert, NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const examData = computed(() => props.liveData as {submitRate?:number;cheatWarnings?:any[]}|null)
const cheatWarnings = computed(() => examData.value?.cheatWarnings || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.exam-bg{background:#FEF2F2;border-radius:8px}.warnings{display:flex;flex-direction:column;gap:8px;margin-top:12px}</style>
