<template>
  <div class="simulated-ide">
    <!-- IDE Header -->
    <header class="simulated-ide__header">
      <div class="simulated-ide__tabs">
        <div class="simulated-ide__tab simulated-ide__tab--active">
          <span class="simulated-ide__tab-icon">📄</span>
          <span class="simulated-ide__tab-name">{{ fileName }}</span>
        </div>
      </div>
      <div class="simulated-ide__actions">
        <button class="simulated-ide__action-btn simulated-ide__action-btn--run" @click="handleRun">
          ▶ 运行
        </button>
        <button class="simulated-ide__action-btn simulated-ide__action-btn--submit" @click="handleSubmit">
          📤 提交
        </button>
      </div>
    </header>

    <!-- IDE Body -->
    <div class="simulated-ide__body">
      <!-- Code Editor Area -->
      <div class="simulated-ide__editor-panel">
        <div class="simulated-ide__line-numbers">
          <span
            v-for="n in lineCount"
            :key="n"
            class="simulated-ide__line-num"
          >{{ n }}</span>
        </div>
        <textarea
          class="simulated-ide__code-area"
          :value="codeContent"
          spellcheck="false"
          @input="handleCodeInput"
        ></textarea>
      </div>

      <!-- Output / AI Review Panel -->
      <div class="simulated-ide__output-panel">
        <div class="simulated-ide__output-tabs">
          <button
            class="simulated-ide__output-tab"
            :class="{ 'simulated-ide__output-tab--active': activeOutputTab === 'output' }"
            @click="activeOutputTab = 'output'"
          >
            输出
          </button>
          <button
            class="simulated-ide__output-tab"
            :class="{ 'simulated-ide__output-tab--active': activeOutputTab === 'review' }"
            @click="activeOutputTab = 'review'"
          >
            <span class="simulated-ide__ai-badge">AI</span>
            代码评审
          </button>
        </div>

        <!-- Output Content -->
        <div v-if="activeOutputTab === 'output'" class="simulated-ide__output-content">
          <pre class="simulated-ide__output-text">{{ runOutput }}</pre>
        </div>

        <!-- AI Review Content -->
        <div v-if="activeOutputTab === 'review'" class="simulated-ide__review-content">
          <div v-if="aiReview" class="simulated-ide__review-result">
            <div class="simulated-ide__review-score">
              <span class="simulated-ide__review-score-value">{{ aiReview.score }}</span>
              <span class="simulated-ide__review-score-label">/100</span>
            </div>
            <div class="simulated-ide__review-items">
              <div
                v-for="item in aiReview.items"
                :key="item.category"
                class="simulated-ide__review-item"
              >
                <span class="simulated-ide__review-category">{{ item.category }}</span>
                <span class="simulated-ide__review-comment">{{ item.comment }}</span>
              </div>
            </div>
          </div>
          <div v-else class="simulated-ide__review-empty">
            提交代码后将获得 AI 评审反馈
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface AiReviewItem {
  category: string
  comment: string
}

interface AiReview {
  score: number
  items: AiReviewItem[]
}

interface SimulatedIDEConfig {
  display?: {
    file_name?: string
    language?: string
    starter_code?: string
  }
  ai_processing?: {
    enable_ai_code_review?: boolean
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: SimulatedIDEConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const fileName = computed<string>(() =>
  props.nodeConfig.display?.file_name ?? 'main.py'
)

const starterCode = computed<string>(() =>
  props.nodeConfig.display?.starter_code ?? `# Python 数组操作练习
# 请完成以下函数实现

def find_max(arr):
    """找到数组中的最大值"""
    # TODO: 实现此函数
    pass

def sort_array(arr):
    """对数组进行升序排序"""
    # TODO: 实现此函数
    pass

# 测试代码
if __name__ == "__main__":
    test_arr = [3, 1, 4, 1, 5, 9, 2, 6]
    print(f"最大值: {find_max(test_arr)}")
    print(f"排序后: {sort_array(test_arr)}")
`
)

const codeContent = ref<string>(starterCode.value)
const activeOutputTab = ref<'output' | 'review'>('output')
const runOutput = ref<string>('点击"运行"按钮执行代码...')

/** Placeholder AI review */
const aiReview = ref<AiReview | null>({
  score: 78,
  items: [
    { category: '代码正确性', comment: '函数逻辑基本正确，但缺少空数组边界处理。' },
    { category: '代码风格', comment: '命名规范，缩进一致，建议添加类型注解。' },
    { category: '算法效率', comment: '排序使用了内置方法，时间复杂度O(n log n)，可接受。' },
    { category: '注释文档', comment: '函数文档字符串完整，建议补充参数和返回值说明。' }
  ]
})

const lineCount = computed<number>(() => {
  return codeContent.value.split('\n').length
})

function handleCodeInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  codeContent.value = target.value
}

function handleRun() {
  runOutput.value = `>>> 运行 ${fileName.value}...\n最大值: 9\n排序后: [1, 1, 2, 3, 4, 5, 6, 9]\n\n[进程已结束，退出代码 0]`
}

function handleSubmit() {
  activeOutputTab.value = 'review'
  emit('complete')
}
</script>

<style scoped>
.simulated-ide {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--color-gray-900, #0f172a);
  border-radius: var(--radius-lg, 0.75rem);
}

.simulated-ide__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background: var(--color-gray-800, #1e293b);
  border-bottom: 1px solid var(--color-gray-700, #334155);
}

.simulated-ide__tabs {
  display: flex;
  gap: 0.25rem;
}

.simulated-ide__tab {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-sm, 0.25rem);
  font-size: 0.8125rem;
  color: var(--color-gray-400, #94a3b8);
}

.simulated-ide__tab--active {
  background: var(--color-gray-700, #334155);
  color: var(--color-gray-100, #f1f5f9);
}

.simulated-ide__tab-icon {
  font-size: 0.75rem;
}

.simulated-ide__tab-name {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.simulated-ide__actions {
  display: flex;
  gap: 0.5rem;
}

.simulated-ide__action-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.simulated-ide__action-btn--run {
  background: #22c55e;
  color: white;
}

.simulated-ide__action-btn--run:hover {
  background: #16a34a;
}

.simulated-ide__action-btn--submit {
  background: var(--color-primary-500, #6366f1);
  color: white;
}

.simulated-ide__action-btn--submit:hover {
  background: var(--color-primary-600, #4f46e5);
}

.simulated-ide__body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.simulated-ide__editor-panel {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.simulated-ide__line-numbers {
  display: flex;
  flex-direction: column;
  padding: 0.75rem 0.5rem;
  background: var(--color-gray-800, #1e293b);
  border-right: 1px solid var(--color-gray-700, #334155);
  user-select: none;
  overflow: hidden;
  min-width: 2.5rem;
}

.simulated-ide__line-num {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  line-height: 1.5rem;
  color: var(--color-gray-500, #64748b);
  text-align: right;
}

.simulated-ide__code-area {
  flex: 1;
  padding: 0.75rem;
  background: var(--color-gray-900, #0f172a);
  color: #e2e8f0;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.8125rem;
  line-height: 1.5rem;
  border: none;
  resize: none;
  outline: none;
  overflow: auto;
  tab-size: 4;
}

.simulated-ide__output-panel {
  height: 200px;
  border-top: 1px solid var(--color-gray-700, #334155);
  display: flex;
  flex-direction: column;
}

.simulated-ide__output-tabs {
  display: flex;
  gap: 0.25rem;
  padding: 0.375rem 0.75rem;
  background: var(--color-gray-800, #1e293b);
  border-bottom: 1px solid var(--color-gray-700, #334155);
}

.simulated-ide__output-tab {
  padding: 0.25rem 0.625rem;
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  background: none;
  border: none;
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.simulated-ide__output-tab--active {
  background: var(--color-gray-700, #334155);
  color: var(--color-gray-100, #f1f5f9);
}

.simulated-ide__ai-badge {
  font-size: 0.5625rem;
  font-weight: 700;
  padding: 0.0625rem 0.25rem;
  border-radius: 2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
}

.simulated-ide__output-content {
  flex: 1;
  padding: 0.75rem;
  overflow: auto;
}

.simulated-ide__output-text {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  line-height: 1.5;
  color: #a5f3fc;
  white-space: pre-wrap;
  margin: 0;
}

.simulated-ide__review-content {
  flex: 1;
  padding: 0.75rem;
  overflow: auto;
}

.simulated-ide__review-result {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.simulated-ide__review-score {
  display: flex;
  align-items: baseline;
}

.simulated-ide__review-score-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #a5f3fc;
}

.simulated-ide__review-score-label {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.simulated-ide__review-items {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.simulated-ide__review-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
  line-height: 1.4;
}

.simulated-ide__review-category {
  color: #fbbf24;
  font-weight: 600;
  white-space: nowrap;
}

.simulated-ide__review-comment {
  color: var(--color-gray-300, #cbd5e1);
}

.simulated-ide__review-empty {
  color: var(--color-gray-500, #64748b);
  font-size: 0.8125rem;
  text-align: center;
  padding-top: 2rem;
}
</style>
