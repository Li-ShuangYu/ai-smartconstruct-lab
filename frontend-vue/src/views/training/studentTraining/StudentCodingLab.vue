<template>
  <div class="student-coding">
    <!-- Header -->
    <div class="coding-header">
      <div class="coding-header-left">
        <span class="coding-badge">💻 编码实训</span>
        <span class="coding-title">{{ taskTitle }}</span>
      </div>
      <div class="coding-header-right">
        <span class="lang-badge">{{ language || 'python' }}</span>
        <button class="coding-btn" @click="handleComplete" :disabled="completed">
          {{ completed ? '已完成 ✓' : '完成实训' }}
        </button>
      </div>
    </div>

    <!-- Task Description -->
    <div v-if="taskDescription" class="coding-desc" @click="showHints = !showHints">
      <div class="desc-label">📋 实训任务</div>
      <div class="desc-text">{{ taskDescription }}</div>
    </div>

    <!-- Hints -->
    <div v-if="hints.length > 0 && showHints" class="coding-hints">
      <div class="hints-label">💡 提示</div>
      <div v-for="(h, i) in hints" :key="i" class="hint-item">{{ i + 1 }}. {{ h }}</div>
    </div>

    <!-- Test Cases Preview -->
    <div v-if="testCases.length > 0" class="coding-tests">
      <div class="tests-label">🧪 测试用例</div>
      <div v-for="(tc, i) in testCases" :key="i" class="test-case">
        <span class="tc-desc">{{ tc.description || '用例' + (i+1) }}</span>
        <span class="tc-meta" v-if="tc.input">输入: {{ tc.input }}</span>
        <span class="tc-meta" v-if="tc.expected">期望: {{ tc.expected }}</span>
      </div>
    </div>

    <!-- Code Editor + Terminal -->
    <div class="coding-editor-area">
      <div class="editor-header">
        <span>代码编辑器</span>
        <button class="run-btn" @click="simulateRun">▶ 运行代码</button>
      </div>
      <textarea
        v-model="code"
        class="code-textarea"
        :placeholder="'# 请在此编写' + (language || 'Python') + '代码...'"
        spellcheck="false"
      ></textarea>
      <div class="terminal">
        <div class="terminal-header">
          <span>📟 终端输出</span>
          <button class="clear-btn" @click="terminalLines = []">清除</button>
        </div>
        <div class="terminal-body" ref="terminalRef">
          <div v-for="(line, i) in terminalLines" :key="i" class="term-line" :class="line.type">
            <span>{{ line.text }}</span>
          </div>
          <div v-if="terminalLines.length === 0" class="term-placeholder">点击「运行代码」查看输出</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: Record<string, any>
}>()

const emit = defineEmits<{
  complete: []
}>()

const completed = ref(false)
const code = ref('')
const taskDescription = ref('')
const language = ref('python')
const hints = ref<string[]>([])
const testCases = ref<any[]>([])
const showHints = ref(true)
const terminalLines = ref<{ text: string; type: string }[]>([])
const terminalRef = ref<HTMLElement | null>(null)

const taskTitle = computed(() => {
  const task = props.nodeConfig?.coding_task || {}
  return task.title || task.description?.slice(0, 40) || '编码实训'
})

// Parse coding_task from seeder data
function initData() {
  const task = props.nodeConfig?.coding_task || props.nodeConfig || {}
  code.value = task.code_template || task.template || ''
  taskDescription.value = task.description || task.desc || ''
  language.value = task.language || 'python'
  hints.value = task.hints || []
  testCases.value = task.test_cases || task.tests || []
}

initData()

function handleComplete() {
  completed.value = true
  emit('complete')
}

function simulateRun() {
  terminalLines.value.push({ text: '>>> 正在运行代码...', type: 'info' })
  const lines = code.value.split('\n')
  // Simple simulation: show the code being "run"
  setTimeout(() => {
    terminalLines.value.push({ text: '代码执行完毕 (exit: 0)', type: 'stdout' })
    terminalLines.value.push({ text: '所有测试用例通过 ✓', type: 'success' })
    nextTick(() => {
      if (terminalRef.value) {
        terminalRef.value.scrollTop = terminalRef.value.scrollHeight
      }
    })
  }, 500)
}
</script>

<style scoped>
.student-coding {
  display: flex; flex-direction: column; gap: 12px; padding: 16px;
  background: #0F172A; color: #E2E8F0; min-height: 400px; font-family: system-ui, sans-serif;
}
.coding-header { display: flex; justify-content: space-between; align-items: center; }
.coding-header-left { display: flex; align-items: center; gap: 10px; }
.coding-header-right { display: flex; align-items: center; gap: 10px; }
.coding-badge { background: #1A3A2A; color: #34D399; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 700; }
.coding-title { font-size: 16px; font-weight: 700; }
.lang-badge { background: #1E293B; color: #60A5FA; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.coding-btn { background: #4F46E5; color: #FFF; border: none; padding: 6px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 13px; }
.coding-btn:disabled { background: #334155; color: #94A3B8; cursor: not-allowed; }
.coding-desc { background: #1E293B; border-radius: 8px; padding: 12px 16px; cursor: pointer; }
.desc-label { font-weight: 700; color: #60A5FA; margin-bottom: 6px; font-size: 13px; }
.desc-text { font-size: 14px; line-height: 1.6; color: #CBD5E1; white-space: pre-wrap; }
.coding-hints { background: #1E293B; border-radius: 8px; padding: 12px 16px; }
.hints-label { font-weight: 700; color: #FBBF24; margin-bottom: 6px; font-size: 13px; }
.hint-item { font-size: 13px; color: #94A3B8; padding: 3px 0; }
.coding-tests { background: #1E293B; border-radius: 8px; padding: 12px 16px; }
.tests-label { font-weight: 700; color: #A78BFA; margin-bottom: 6px; font-size: 13px; }
.test-case { display: flex; gap: 12px; font-size: 12px; padding: 4px 0; border-bottom: 1px solid #334155; }
.test-case:last-child { border-bottom: none; }
.tc-desc { color: #34D399; font-weight: 600; min-width: 100px; }
.tc-meta { color: #94A3B8; }
.coding-editor-area { flex: 1; display: flex; flex-direction: column; border: 1px solid #334155; border-radius: 8px; overflow: hidden; }
.editor-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #1E293B; font-size: 13px; color: #94A3B8; }
.run-btn { background: #4F46E5; color: #FFF; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600; }
.code-textarea { flex: 1; min-height: 200px; background: #0F172A; color: #E2E8F0; border: none; outline: none; padding: 12px; font-family: 'Consolas', monospace; font-size: 14px; line-height: 1.6; resize: vertical; tab-size: 2; }
.terminal { height: 120px; border-top: 1px solid #334155; display: flex; flex-direction: column; }
.terminal-header { display: flex; justify-content: space-between; padding: 4px 12px; background: #1E293B; font-size: 12px; color: #94A3B8; }
.clear-btn { background: none; border: none; color: #EF4444; cursor: pointer; font-size: 12px; }
.terminal-body { flex: 1; overflow-y: auto; padding: 6px 12px; font-family: 'Consolas', monospace; font-size: 13px; }
.term-line { padding: 2px 0; color: #E2E8F0; }
.term-line.info { color: #60A5FA; }
.term-line.stdout { color: #E2E8F0; }
.term-line.success { color: #34D399; }
.term-placeholder { color: #475569; font-style: italic; }
</style>