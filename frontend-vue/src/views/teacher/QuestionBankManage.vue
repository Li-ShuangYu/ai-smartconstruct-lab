<template>
  <div class="question-bank-container">
    <aside class="bank-sidebar">
      <div class="sidebar-header">
        <h2 class="title">题库管理</h2>
        <button class="btn-icon" @click="openBankModal()" title="新建题库">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        </button>
      </div>
      
      <div class="bank-list">
        <div 
          v-for="bank in banks" 
          :key="bank.id" 
          class="bank-item" 
          :class="{ active: activeBankId === bank.id }"
          @click="selectBank(bank.id)"
        >
          <div class="bank-info">
            <h3 class="bank-name" :title="bank.bank_name">{{ bank.bank_name }}</h3>
            <span class="tag" :class="bank.is_public === 1 ? 'public-tag' : 'private-tag'">
              {{ bank.is_public === 1 ? '公开' : '私有' }}
            </span>
          </div>
          <div class="bank-actions" v-show="activeBankId === bank.id">
            <button class="btn-text-small" @click.stop="openBankModal(bank)">编辑</button>
            <button class="btn-text-small danger" @click.stop="deleteBank(bank.id)">删除</button>
          </div>
        </div>
      </div>
    </aside>

    <main class="question-main">
      <div v-if="!activeBankId" class="empty-state">
        请在左侧选择或创建一个题库
      </div>
      
      <div v-else class="main-content">
        <header class="main-header">
          <div class="header-left">
            <h2 class="main-title">{{ activeBank?.bank_name }}</h2>
            <span class="subtitle">共 {{ questions.length }} 道题目</span>
          </div>
          <div class="header-actions">
            <template v-if="selectedQuestionIds.length > 0">
              <span class="selected-count">已选 {{ selectedQuestionIds.length }} 项</span>
              <n-button secondary type="info" size="small" @click="openMoveCopyModal('move')">批量移动</n-button>
              <n-button secondary type="success" size="small" @click="openMoveCopyModal('copy')">批量复制</n-button>
            </template>
            <div class="divider" v-if="selectedQuestionIds.length > 0"></div>
            <n-button type="primary" size="small" @click="openAIModal">
              <template #icon>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              </template>
              AI 智能出题
            </n-button>
            <n-button type="primary" size="small" @click="openQuestionModal()">手动录入</n-button>
          </div>
        </header>

        <n-spin :show="loading">
          <div class="question-list">
            <div class="question-card" v-for="(q, index) in questions" :key="q.id">
              <div class="q-header">
                <div class="q-header-left">
                  <n-checkbox :value="q.id" v-model:checked="q._selected" @update:checked="handleSelectChange" />
                  <span class="q-num">{{ index + 1 }}.</span>
                  <span class="tag type-tag">{{ getQuestionTypeName(q.question_type) }}</span>
                  <span class="tag source-tag" :class="q.create_type === 1 ? 'ai-source' : 'manual-source'">
                    {{ q.create_type === 1 ? 'AI生成' : '手动录入' }}
                  </span>
                </div>
                <div class="q-header-right">
                  <span class="q-score">{{ q.default_score }} 分</span>
                  <button class="btn-text-small" @click="openQuestionModal(q)">编辑</button>
                  <button class="btn-text-small danger" @click="deleteQuestion(q.id)">删除</button>
                </div>
              </div>

              <div class="q-body">
                <div class="stem" v-html="q.content.rich_stem || q.content.stem"></div>
                <div v-if="q.question_type === 1 || q.question_type === 2" class="options-list">
                  <div class="option-item" v-for="opt in q.content.options" :key="opt.id">
                    <span class="opt-id">{{ opt.id }}.</span>
                    <span class="opt-text">{{ opt.text }}</span>
                  </div>
                </div>
              </div>

              <div class="q-footer">
                <div class="answer-box">
                  <span class="answer-label">标准答案：</span>
                  <span class="answer-content">{{ formatAnswer(q) }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="questions.length === 0" class="empty-state">当前题库暂无题目</div>
          </div>
        </n-spin>
      </div>
    </main>

    <n-modal v-model:show="showBankModal" preset="card" :title="bankForm.id ? '编辑题库' : '新建题库'" style="width: 500px">
      <n-form :model="bankForm" label-placement="left" label-width="80">
        <n-form-item label="题库名称" path="bank_name">
          <n-input v-model:value="bankForm.bank_name" placeholder="请输入题库名称" />
        </n-form-item>
        <n-form-item label="是否公开" path="is_public">
          <n-radio-group v-model:value="bankForm.is_public" name="is_public_group">
            <n-radio :value="1">公开 (其他教师可见)</n-radio>
            <n-radio :value="0">私有 (仅自己可见)</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showBankModal = false">取消</n-button>
          <n-button type="primary" @click="saveBank">保存</n-button>
        </div>
      </template>
    </n-modal>

    <n-modal v-model:show="showQuestionModal" preset="card" :title="questionForm.id ? '编辑题目' : '新增题目'" style="width: 800px">
       <div class="form-mock-placeholder">
          题目编辑区域（支持题干富文本、选项动态增删、答案设置等。此处为占位）
          <br><br>
          <n-input type="textarea" v-model:value="questionForm.contentStr" placeholder="JSON 数据格式调试录入..." rows="8"/>
       </div>
       <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showQuestionModal = false">取消</n-button>
          <n-button type="primary" @click="saveQuestion">保存题目</n-button>
        </div>
      </template>
    </n-modal>

    <n-modal v-model:show="showAIModal" preset="card" title="AI 智能出题" style="width: 600px">
      <n-form label-placement="top">
        <n-form-item label="出题知识点 / 材料要求">
          <n-input type="textarea" placeholder="例如：根据高中物理牛顿第二定律生成5道单选题，难度中等..." rows="5" />
        </n-form-item>
        <n-form-item label="生成题型及数量">
          <div style="display:flex; gap: 16px;">
            <n-select placeholder="选择题型" :options="[{label:'单选题',value:1},{label:'多选题',value:2}]" style="width: 150px" />
            <n-input-number placeholder="数量" :min="1" :max="20" style="width: 120px" />
          </div>
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showAIModal = false">取消</n-button>
          <n-button type="primary">开始生成</n-button>
        </div>
      </template>
    </n-modal>

    <n-modal v-model:show="showMoveCopyModal" preset="card" :title="moveCopyType === 'move' ? '批量移动至' : '批量复制至'" style="width: 400px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="目标题库">
          <n-select v-model:value="targetBankId" :options="targetBankOptions" placeholder="请选择目标题库" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showMoveCopyModal = false">取消</n-button>
          <n-button type="primary" @click="confirmMoveCopy">确认{{ moveCopyType === 'move' ? '移动' : '复制' }}</n-button>
        </div>
      </template>
    </n-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NSpin, NButton, NCheckbox, NModal, NForm, NFormItem, NInput, NRadioGroup, NRadio, NSelect, NInputNumber } from 'naive-ui'

// === 类型定义 ===
interface QuestionBank {
  id: number
  teacher_id: number
  bank_name: string
  is_public: number // 0-私有 1-公开
}

interface Question {
  id: number
  bank_id: number
  question_type: number // 1-单选 2-多选 3-填空 4-判断 5-简答
  create_type: number   // 1-AI生成 2-手动录入
  content: any          // 动态解析
  standard_answer: any  // 动态解析
  default_score: number
  _selected?: boolean   // 前端附加状态，用于多选
}

// === 状态数据 ===
const loading = ref(false)
const banks = ref<QuestionBank[]>([])
const activeBankId = ref<number | null>(null)
const questions = ref<Question[]>([])

// 计算属性
const activeBank = computed(() => banks.value.find(b => b.id === activeBankId.value))
const selectedQuestionIds = computed(() => questions.value.filter(q => q._selected).map(q => q.id))
const targetBankOptions = computed(() => {
  return banks.value
    .filter(b => b.id !== activeBankId.value)
    .map(b => ({ label: b.bank_name, value: b.id }))
})

// === 模拟数据初始化 ===
onMounted(() => {
  // 模拟题库数据
  banks.value = [
    { id: 1, teacher_id: 101, bank_name: 'Java核心技术基础', is_public: 1 },
    { id: 2, teacher_id: 101, bank_name: '前端进阶(Vue3+TS)', is_public: 0 },
    { id: 3, teacher_id: 101, bank_name: '数据结构与算法', is_public: 1 }
  ]
  if (banks.value.length > 0 && banks.value[0]) {
    const firstBank = banks.value[0]
    activeBankId.value = firstBank.id
    loadQuestions(activeBankId.value)
  }
})

// 模拟加载题目数据
function loadQuestions(bankId: number) {
  loading.value = true
  setTimeout(() => {
    // 根据题型填充完整测试数据
    questions.value = [
      {
        id: 1001, bank_id: bankId, question_type: 1, create_type: 2, default_score: 5, _selected: false,
        content: {
          stem: "以下哪个是Java的关键字？",
          rich_stem: "<p>以下哪个是<strong>Java</strong>的关键字？</p>",
          options: [{ id: "A", text: "main" }, { id: "B", text: "String" }, { id: "C", text: "static" }, { id: "D", text: "System" }]
        },
        standard_answer: { answer: "C" }
      },
      {
        id: 1002, bank_id: bankId, question_type: 2, create_type: 1, default_score: 10, _selected: false,
        content: {
          stem: "以下哪些是Java的基本数据类型？",
          options: [{ id: "A", text: "int" }, { id: "B", text: "String" }, { id: "C", text: "boolean" }, { id: "D", text: "double" }, { id: "E", text: "ArrayList" }]
        },
        standard_answer: { answers: ["A", "C", "D"] }
      },
      {
        id: 1003, bank_id: bankId, question_type: 4, create_type: 1, default_score: 2, _selected: false,
        content: { stem: "Java是一种纯面向对象的编程语言。" },
        standard_answer: { answer: false }
      },
      {
        id: 1004, bank_id: bankId, question_type: 3, create_type: 2, default_score: 8, _selected: false,
        content: {
          stem: "Java中声明常量的关键字是______，声明接口的关键字是______。",
          rich_stem: "<p>Java中声明常量的关键字是<span style=\"color:red\">______</span>，声明接口的关键字是<span style=\"color:red\">______</span>。</p>"
        },
        standard_answer: { answers: ["final", "interface"] }
      },
      {
        id: 1005, bank_id: bankId, question_type: 5, create_type: 2, default_score: 15, _selected: false,
        content: { stem: "简述Java中接口和抽象类的区别。" },
        standard_answer: { answer: "1. 抽象类可以有构造方法，接口不能有。\n2. 抽象类是对类的抽象，接口是对行为的抽象。" }
      }
    ]
    loading.value = false
  }, 400)
}

// === 辅助方法 ===
function getQuestionTypeName(type: number) {
  const map: Record<number, string> = { 1: '单选', 2: '多选', 3: '填空', 4: '判断', 5: '简答' }
  return map[type] || '未知'
}

function formatAnswer(q: Question) {
  if (q.question_type === 1 || q.question_type === 5) {
    return q.standard_answer.answer;
  }
  if (q.question_type === 4) {
    return q.standard_answer.answer ? '正确' : '错误';
  }
  if (q.question_type === 2 || q.question_type === 3) {
    return q.standard_answer.answers.join(' , ');
  }
  return '';
}

function handleSelectChange() {
  // 触发 computed 更新选中状态
}

// === 事件交互 ===

// 切换题库
function selectBank(id: number) {
  if (activeBankId.value === id) return
  activeBankId.value = id
  loadQuestions(id)
}

// --- 题库操作 ---
const showBankModal = ref(false)
const bankForm = ref<Partial<QuestionBank>>({ bank_name: '', is_public: 0 })

function openBankModal(bank?: QuestionBank) {
  if (bank) {
    bankForm.value = { ...bank }
  } else {
    bankForm.value = { bank_name: '', is_public: 0 }
  }
  showBankModal.value = true
}

function saveBank() {
  if (bankForm.value.id) {
    const idx = banks.value.findIndex(b => b.id === bankForm.value.id)
    if (idx > -1) {
      const existing = banks.value[idx]
      if (existing) {
        banks.value[idx] = {
          id: existing.id,
          teacher_id: existing.teacher_id,
          bank_name: bankForm.value.bank_name ?? existing.bank_name,
          is_public: bankForm.value.is_public ?? existing.is_public
        }
      }
    }
  } else {
    banks.value.push({ 
      id: Date.now(), 
      teacher_id: 101, 
      bank_name: bankForm.value.bank_name || '未命名', 
      is_public: bankForm.value.is_public || 0 
    })
  }
  showBankModal.value = false
}

function deleteBank(id: number) {
  if (confirm('确定要删除该题库吗？')) {
    banks.value = banks.value.filter(b => b.id !== id)
    if (activeBankId.value === id) {
      activeBankId.value = banks.value[0]?.id || null
      if (activeBankId.value) loadQuestions(activeBankId.value)
      else questions.value = []
    }
  }
}

// --- 题目操作 ---
const showQuestionModal = ref(false)
const questionForm = ref<any>({})

function openQuestionModal(q?: Question) {
  if (q) {
    questionForm.value = { ...q, contentStr: JSON.stringify(q.content, null, 2) }
  } else {
    questionForm.value = { contentStr: '' }
  }
  showQuestionModal.value = true
}

function saveQuestion() {
  showQuestionModal.value = false
  // 伪实现
}

function deleteQuestion(id: number) {
  if (confirm('确定要删除这道题吗？')) {
    questions.value = questions.value.filter(q => q.id !== id)
  }
}

// --- AI 生成 ---
const showAIModal = ref(false)
function openAIModal() { showAIModal.value = true }

// --- 批量移动/复制 ---
const showMoveCopyModal = ref(false)
const moveCopyType = ref<'move' | 'copy'>('move')
const targetBankId = ref<number | null>(null)

function openMoveCopyModal(type: 'move' | 'copy') {
  moveCopyType.value = type
  targetBankId.value = null
  showMoveCopyModal.value = true
}

function confirmMoveCopy() {
  if (!targetBankId.value) return alert('请选择目标题库')
  const actionName = moveCopyType.value === 'move' ? '移动' : '复制'
  alert(`成功将 ${selectedQuestionIds.value.length} 道题目${actionName}至所选题库`)
  
  if (moveCopyType.value === 'move') {
    // 从当前列表移除已移动的数据
    questions.value = questions.value.filter(q => !selectedQuestionIds.value.includes(q.id))
  }
  showMoveCopyModal.value = false
}
</script>

<style scoped>
/* 全局与布局 */
.question-bank-container { 
  display: flex; 
  height: calc(100vh - 60px); /* 视实际布局调整高度 */
  gap: 24px; 
  max-width: 1400px; 
  margin: 0 auto; 
  background-color: #F8FAFC; 
}

/* 按钮通用样式 */
.btn-icon { background: transparent; border: none; cursor: pointer; color: #64748B; padding: 4px; border-radius: 4px; transition: 0.2s; display: flex; align-items: center;}
.btn-icon:hover { background: #E2E8F0; color: #0F172A; }
.btn-text-small { background: none; border: none; color: #4F46E5; font-size: 13px; cursor: pointer; padding: 0 4px; }
.btn-text-small:hover { text-decoration: underline; }
.btn-text-small.danger { color: #E11D48; }

/* 标签样式 */
.tag { font-size: 12px; padding: 2px 8px; border-radius: 4px; font-weight: 500; display: inline-block;}
.public-tag { background-color: #DCFCE7; color: #166534; }
.private-tag { background-color: #F1F5F9; color: #475569; }
.type-tag { background-color: #EEF2FF; color: #4338CA; border: 1px solid #C7D2FE; }
.source-tag { border: 1px solid transparent; }
.ai-source { background-color: #FDF4FF; color: #C026D3; border-color: #F5D0FE; }
.manual-source { background-color: #F0FDF4; color: #059669; border-color: #BBF7D0; }

/* ======== 左侧：题库列表 ======== */
.bank-sidebar { 
  width: 280px; 
  background: #FFF; 
  border-right: 1px solid #E2E8F0; 
  border-radius: 12px;
  display: flex; 
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sidebar-header .title { font-size: 16px; font-weight: 700; color: #0F172A; margin: 0; }
.bank-list { flex: 1; overflow-y: auto; padding: 12px; }
.bank-item {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.bank-item:hover { background-color: #F8FAFC; }
.bank-item.active { background-color: #EEF2FF; border-color: #C7D2FE; }
.bank-info { display: flex; justify-content: space-between; align-items: center; }
.bank-name { font-size: 14px; font-weight: 600; color: #1E293B; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 160px; }
.bank-actions { display: flex; justify-content: flex-end; gap: 8px; border-top: 1px dashed #CBD5E1; padding-top: 8px; }

/* ======== 右侧：题目列表 ======== */
.question-main { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  background: transparent;
  overflow: hidden;
}
.empty-state { flex: 1; display: flex; justify-content: center; align-items: center; color: #94A3B8; font-size: 14px; }

.main-content { display: flex; flex-direction: column; height: 100%; }
.main-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; margin-bottom: 16px; border-bottom: 1px solid #E2E8F0; }
.header-left .main-title { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 4px 0; }
.header-left .subtitle { font-size: 13px; color: #64748B; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.selected-count { font-size: 13px; color: #4F46E5; font-weight: 600; }
.divider { width: 1px; height: 16px; background-color: #CBD5E1; margin: 0 4px; }

.question-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; padding-bottom: 24px; padding-right: 8px;}
.question-card {
  background: #FFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  transition: box-shadow 0.2s ease;
}
.question-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-color: #CBD5E1; }

.q-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.q-header-left { display: flex; align-items: center; gap: 12px; }
.q-num { font-size: 16px; font-weight: 700; color: #0F172A; }
.q-header-right { display: flex; align-items: center; gap: 16px; }
.q-score { font-size: 13px; color: #F59E0B; font-weight: 600; background: #FEF3C7; padding: 2px 8px; border-radius: 12px; }

.q-body { font-size: 14px; color: #1E293B; line-height: 1.6; margin-bottom: 16px; }
.stem { margin-bottom: 12px; }
.options-list { display: flex; flex-direction: column; gap: 8px; margin-left: 8px; }
.option-item { display: flex; gap: 8px; align-items: flex-start; }
.opt-id { font-weight: 600; color: #475569; }
.opt-text { color: #334155; }

.q-footer { border-top: 1px solid #F1F5F9; padding-top: 12px; }
.answer-box { background: #F8FAFC; padding: 10px 14px; border-radius: 6px; border-left: 3px solid #10B981; }
.answer-label { font-size: 13px; font-weight: 600; color: #059669; }
.answer-content { font-size: 14px; color: #0F172A; font-weight: 500; white-space: pre-wrap;}

/* 占位符样式 */
.form-mock-placeholder {
  background: #F8FAFC;
  border: 1px dashed #CBD5E1;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  color: #64748B;
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #94A3B8; }
</style>