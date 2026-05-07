<template>
  <div class="ide-wrapper">
    <div v-if="context?.isGrouped" class="team-bar"><n-tag v-for="m in mockMembers" :key="m.name" :type="m.online ? 'success' : 'default'" size="small">{{ m.name }}{{ m.online ? '●' : '○' }}</n-tag></div>
    <div class="ide-layout">
      <aside class="req-panel"><div class="panel-title">需求描述</div><div class="panel-body"><p>{{ config?.scenario || config?.desc || '请在右侧编辑器中编写代码，然后点击模拟运行验证。' }}</p></div></aside>
      <section class="editor-column">
        <div class="editor-header">
          <div class="mac-dots"><span class="dot close" /><span class="dot min" /><span class="dot max" /></div>
          <span class="file-tab">main.py</span>
        </div>
        <div class="editor-body"><pre><code v-html="highlightedCode"></code></pre></div>
        <div class="terminal-bar"><span>模拟终端</span></div>
        <div class="terminal-body"><div v-for="(l, i) in terminalLines" :key="i" :class="['term-line', l.type]">{{ l.text }}</div></div>
      </section>
    </div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleRun" :loading="running">模拟运行</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NTag } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const runSuccess = ref(false); const running = ref(false); const terminalLines = ref<{ text: string; type: string }[]>([])
const mockMembers = [{ name: '张三', online: true }, { name: '李四', online: true }, { name: '王五', online: false }]
const canProceed = computed(() => runSuccess.value)
const sampleCode = `def main():\n    print("模拟编码环境已就绪")\n    # 请在此处编写代码\n    pass\n\nif __name__ == "__main__":\n    main()`
const highlightedCode = computed(() => sampleCode.replace(/\b(def|if|print|pass|import)\b/g, '<span style="color:#0000ff">$1</span>').replace(/(".*?")/g, '<span style="color:#a31515">$1</span>').replace(/(#.*)/g, '<span style="color:#008000">$1</span>'))
function handleRun() {
  running.value = true; terminalLines.value = [{ text: '$ python3 main.py', type: 'info' }]
  setTimeout(() => { terminalLines.value.push({ text: '模拟编码环境已就绪', type: 'info' }) }, 400)
  setTimeout(() => { terminalLines.value.push({ text: '>>> 模拟运行成功，输出符合预期', type: 'success' }); runSuccess.value = true; running.value = false; emit('step-complete'); emit('update-data', { runSuccess: true }) }, 1200)
}
</script>
<style scoped>
.ide-wrapper { display: flex; flex-direction: column; height: 100%; }
.team-bar { display: flex; gap: 8px; padding: 8px 16px; border-bottom: 1px solid #E2E8F0; background: #F8FAFC; }
.ide-layout { display: flex; flex: 1; min-height: 0; }
.req-panel { width: 240px; border-right: 1px solid #E2E8F0; display: flex; flex-direction: column; flex-shrink: 0; }
.panel-title { padding: 12px; font-weight: 700; font-size: 13px; border-bottom: 1px solid #E2E8F0; background: #F1F5F9; }
.panel-body { padding: 16px; font-size: 13px; color: #475569; line-height: 1.6; }
.editor-column { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.editor-header { display: flex; align-items: center; gap: 12px; padding: 0 12px; height: 32px; background: #F3F3F3; border-bottom: 1px solid #E2E8F0; }
.mac-dots { display: flex; gap: 6px; } .dot { width: 10px; height: 10px; border-radius: 50%; } .close { background: #FF5F56; } .min { background: #FFBD2E; } .max { background: #27C93F; }
.file-tab { font-size: 12px; color: #333; background: #fff; padding: 2px 12px; border: 1px solid #E2E8F0; border-bottom: none; }
.editor-body { flex: 1; overflow-y: auto; padding: 12px 16px; background: #fff; font-family: Consolas, monospace; font-size: 13px; line-height: 1.5; }
.editor-body pre { margin: 0; white-space: pre-wrap; }
.terminal-bar { padding: 6px 12px; font-size: 11px; color: #8A8A8A; background: #F3F3F3; border-top: 1px solid #E2E8F0; border-bottom: 1px solid #E2E8F0; }
.terminal-body { height: 100px; overflow-y: auto; padding: 8px 12px; background: #fff; font-family: Consolas, monospace; font-size: 12px; }
.term-line { margin-bottom: 2px; } .info { color: #333; } .success { color: #107c10; font-weight: bold; }
.actions { display: flex; justify-content: center; padding: 16px; border-top: 1px solid #E2E8F0; }
</style>
