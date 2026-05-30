<template>
  <div class="execute-wrapper">
    <!-- 加载状态 -->
    <div v-if="store.loading && !currentComponent" class="loading-container">
      <n-spin size="large" />
      <p class="loading-text">加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="store.error && !currentComponent" class="error-container">
      <div class="error-icon">⚠️</div>
      <p class="error-message">{{ store.error }}</p>
      <n-button type="primary" @click="handleRetry">重试</n-button>
    </div>

    <!-- 阶段完成提示 -->
    <div v-else-if="phaseCompleted" class="phase-complete-container">
      <div class="phase-complete-icon">🎉</div>
      <h2 class="phase-complete-title">阶段完成！</h2>
      <p class="phase-complete-message">
        「{{ completedPhaseName }}」阶段的所有必修节点已完成
      </p>
      <n-button
        v-if="showBackButton"
        type="primary"
        size="large"
        @click="handleBackToPreview"
      >
        返回预览
      </n-button>
      <n-button
        v-if="hasNextPhase"
        type="info"
        size="large"
        @click="handleNextPhase"
        style="margin-left: 12px;"
      >
        进入下一阶段
      </n-button>
    </div>

    <!-- 实训已完成 -->
    <div v-else-if="trainingComplete" class="complete-container">
      <div class="complete-icon">✅</div>
      <h2 class="complete-title">实训完成</h2>
      <p class="complete-message">恭喜！你已完成本实训的所有必修节点</p>
      <div class="complete-actions">
        <n-button type="primary" @click="handleBackToPreview">查看总览</n-button>
        <n-button @click="router.push('/student/workbench')">返回工作台</n-button>
      </div>
    </div>

    <!-- 节点类型未注册错误 -->
    <div v-else-if="nodeTypeError" class="error-container">
      <div class="error-icon">❌</div>
      <p class="error-message">
        未注册的节点类型：「{{ nodeTypeError }}」，无法加载对应组件
      </p>
      <n-button type="primary" @click="handleBackToPreview">返回预览</n-button>
    </div>

    <!-- 正常执行：动态组件 -->
    <template v-else-if="currentComponent">
      <header class="top-bar">
        <div class="top-bar-left">
          <n-button text @click="handleBackToPreview" class="back-btn">
            ← 返回
          </n-button>
          <span class="task-name">{{ store.taskOverview?.task_name }}</span>
        </div>
        <div class="top-bar-right">
          <span class="phase-label">{{ currentPhaseName }}</span>
          <span class="step-hint">
            {{ currentNodeIndex + 1 }} / {{ currentPhaseNodeCount }}
          </span>
        </div>
      </header>

      <main class="center-area">
        <n-spin :show="nodeLoading" class="node-spin">
          <component
            :is="currentComponent"
            :node-instance-id="store.currentNodeId"
            :node-config="nodeConfig"
            @complete="handleNodeComplete"
          />
        </n-spin>
      </main>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <p class="empty-text">暂无可执行的节点</p>
      <n-button type="primary" @click="handleBackToPreview">返回预览</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 学生实训执行页面
 *
 * 集成 nodePageResolver 动态加载节点组件，实现：
 * - 根据 taskId 加载实训总览
 * - 进入当前节点（或第一个未完成节点）
 * - 渲染解析后的组件并传递 nodeConfig props
 * - 监听 complete 事件，完成节点后自动导航到下一节点
 * - 阶段完成时显示提示和返回按钮（3秒内显示）
 * - 加载状态指示和错误提示 + 重试按钮
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 6.5, 6.6
 *
 * @component TrainingExecute.vue
 */
import { ref, computed, onMounted, watch, type Component as VueComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, useMessage } from 'naive-ui'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import { resolveNodePage } from '@/views/training/nodePageResolver'
import { getNodeContent } from '@/services/modules/studentTraining.service'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const store = useStudentFlowStore()

/** 节点加载状态 */
const nodeLoading = ref(false)

/** 节点配置数据 */
const nodeConfig = ref<Record<string, unknown>>({})

/** 当前动态组件 */
const currentComponent = ref<VueComponent | null>(null)

/** 节点类型错误（未注册类型） */
const nodeTypeError = ref<string | null>(null)

/** 阶段完成标志 */
const phaseCompleted = ref(false)

/** 完成的阶段名称 */
const completedPhaseName = ref('')

/** 返回按钮显示标志（3秒内显示） */
const showBackButton = ref(false)

/** 实训整体完成标志 */
const trainingComplete = ref(false)

/** 从路由获取 taskId */
const taskId = computed(() => Number(route.params.taskId) || 0)

/** 当前阶段名称 */
const currentPhaseName = computed(() =>
  store.currentPhaseProgress?.phase_name ?? ''
)

/** 当前阶段节点总数 */
const currentPhaseNodeCount = computed(() =>
  store.currentPhaseProgress?.total_nodes ?? 0
)

/** 当前节点在阶段内的索引 */
const currentNodeIndex = computed(() => {
  if (!store.currentPhaseProgress || !store.currentNodeId) return 0
  const sorted = [...store.currentPhaseProgress.nodes].sort((a, b) => a.sort_num - b.sort_num)
  const idx = sorted.findIndex(n => n.node_instance_id === store.currentNodeId)
  return idx >= 0 ? idx : 0
})

/** 是否有下一个解锁的未完成阶段 */
const hasNextPhase = computed(() => {
  const sorted = [...store.phases].sort((a, b) => a.sort_num - b.sort_num)
  return sorted.some(p => p.is_unlocked && !p.is_complete)
})

/**
 * 加载并渲染当前节点
 */
async function loadCurrentNode(): Promise<void> {
  const nodeId = store.currentNodeId
  if (!nodeId) {
    currentComponent.value = null
    return
  }

  // 查找节点元信息
  const nodeInfo = store.currentNodeConfig
  if (!nodeInfo) {
    currentComponent.value = null
    return
  }

  // 解析组件
  const component = resolveNodePage(nodeInfo.node_type, 'student')
  if (!component) {
    nodeTypeError.value = nodeInfo.node_type
    currentComponent.value = null
    return
  }

  nodeTypeError.value = null

  // 获取节点内容
  nodeLoading.value = true
  try {
    const result = await getNodeContent(nodeId)
    nodeConfig.value = result.data.node_config ?? {}
    currentComponent.value = component
    // 进入节点
    await store.enterNode(nodeId)
  } catch (e: unknown) {
    store.error = e instanceof Error ? e.message : '加载节点内容失败'
    currentComponent.value = null
  } finally {
    nodeLoading.value = false
  }
}

/**
 * 节点完成事件处理
 */
async function handleNodeComplete(): Promise<void> {
  const nodeId = store.currentNodeId
  if (!nodeId) return

  nodeLoading.value = true
  try {
    const result = await store.completeNode(nodeId)

    // 检查是否阶段完成
    if (result?.phase_complete) {
      // 记录完成的阶段名称
      completedPhaseName.value = currentPhaseName.value
      phaseCompleted.value = true

      // 3秒内显示返回按钮
      showBackButton.value = false
      setTimeout(() => {
        showBackButton.value = true
      }, 300)

      // 检查是否整体完成
      if (result.training_complete) {
        trainingComplete.value = true
        phaseCompleted.value = false
      }
      return
    }

    // 自动导航到下一节点
    const nextNodeId = store.navigateToNextNode()
    if (nextNodeId) {
      await loadCurrentNode()
    } else {
      // 所有节点已完成
      trainingComplete.value = true
    }
  } catch {
    message.error('完成节点失败，请重试')
  } finally {
    nodeLoading.value = false
  }
}

/** 进入下一阶段 */
async function handleNextPhase(): Promise<void> {
  phaseCompleted.value = false
  showBackButton.value = false
  const nextNodeId = store.navigateToNextNode()
  if (nextNodeId) {
    await loadCurrentNode()
  } else {
    trainingComplete.value = true
  }
}

/** 返回预览页面 */
function handleBackToPreview(): void {
  router.push({
    path: `/student/training-preview`,
    query: { taskId: String(taskId.value) }
  })
}

/** 重试加载 */
async function handleRetry(): Promise<void> {
  store.error = null
  if (taskId.value) {
    await store.loadTaskOverview(taskId.value)
    if (!store.error) {
      await loadCurrentNode()
    }
  }
}

/** 监听 currentNodeId 变化 */
watch(() => store.currentNodeId, (newId) => {
  if (newId && !phaseCompleted.value && !trainingComplete.value) {
    loadCurrentNode()
  }
})

onMounted(async () => {
  if (!taskId.value) {
    router.push('/student/workbench')
    return
  }

  // 加载总览（如果尚未加载或 taskId 变化）
  if (!store.taskOverview || store.taskId !== taskId.value) {
    await store.loadTaskOverview(taskId.value)
  }

  if (store.error) return

  // 检查实训是否已完成
  if (store.taskOverview?.participation.status === 2) {
    trainingComplete.value = true
    return
  }

  // 如果路由中指定了 nodeId，使用它
  const queryNodeId = route.query.nodeId ? Number(route.query.nodeId) : null
  if (queryNodeId && store.isNodeAccessible(queryNodeId)) {
    store.currentNodeId = queryNodeId
  }

  // 加载当前节点
  if (store.currentNodeId) {
    await loadCurrentNode()
  }
})
</script>

<style scoped>
.execute-wrapper {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  background: var(--color-background);
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: var(--color-background-soft, #ffffff);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: var(--color-text);
  opacity: 0.7;
}

.task-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-heading);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.phase-label {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.7;
  background: var(--color-background-mute, #f2f2f2);
  padding: 4px 10px;
  border-radius: 8px;
}

.step-hint {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  font-weight: 600;
}

/* 中心区域 */
.center-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.node-spin {
  min-height: 200px;
}

/* 加载状态 */
.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-text {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 14px;
}

/* 错误状态 */
.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.error-icon {
  font-size: 48px;
}

.error-message {
  color: var(--color-text);
  font-size: 15px;
  text-align: center;
  max-width: 400px;
}

/* 阶段完成 */
.phase-complete-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.phase-complete-icon {
  font-size: 64px;
}

.phase-complete-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-heading);
  margin: 0;
}

.phase-complete-message {
  font-size: 15px;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0 0 16px 0;
}

/* 实训完成 */
.complete-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.complete-icon {
  font-size: 64px;
}

.complete-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-heading);
  margin: 0;
}

.complete-message {
  font-size: 15px;
  color: var(--color-text);
  opacity: 0.7;
  margin: 0 0 16px 0;
}

.complete-actions {
  display: flex;
  gap: 12px;
}

/* 空状态 */
.empty-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.empty-text {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 15px;
}
</style>
