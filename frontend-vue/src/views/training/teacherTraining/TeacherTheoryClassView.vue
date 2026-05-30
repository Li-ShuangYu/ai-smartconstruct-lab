<script setup lang="ts">
/**
 * 理论实训幻灯片展示组件（教师版）
 *
 * 教师端预览理论实训内容。
 * 完整实现将在后续任务中完成。
 *
 * @module views/training/teacherTraining/TeacherTheoryClassView
 */

defineProps<{
  nodeInstanceId?: number
  nodeConfig?: {
    slides: Array<{
      type: 'theory' | 'intro' | 'code' | 'quiz' | 'summary'
      title: string
      content: string
      bullet_points?: string[]
      key_points?: string[]
      code?: string
      output?: string
      questions?: Array<{
        question: string
        options: string[]
        answer: string
      }>
    }>
  }
}>()
</script>

<template>
  <div class="teacher-theory-class-view">
    <p>TeacherTheoryClassView 组件占位 — 完整实现见后续任务</p>
  </div>
</template>

<style scoped>
.teacher-theory-class-view {
  padding: var(--spacing-lg);
}
</style>
