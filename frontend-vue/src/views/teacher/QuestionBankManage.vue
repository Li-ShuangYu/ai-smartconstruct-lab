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
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
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
      <n-form label-placement="top" class="question-form">
        <n-form-item label="题目类型">
          <n-radio-group v-model:value="questionForm.question_type" name="question_type">
            <n-radio-button :value="1">单选题</n-radio-button>
            <n-radio-button :value="2">多选题</n-radio-button>
            <n-radio-button :value="3">填空题</n-radio-button>
            <n-radio-button :value="4">判断题</n-radio-button>
            <n-radio-button :value="5">简答题</n-radio-button>
          </n-radio-group>
        </n-form-item>
        
        <n-form-item label="题目分值">
          <n-input-number v-model:value="questionForm.default_score" :min="1" :max="100" style="width: 120px" />
        </n-form-item>
        
        <n-form-item label="题目排序序号">
          <n-input-number v-model:value="questionForm.sort_order" :min="0" :max="999" style="width: 120px" />
          <span class="sort-hint">用于题目在题库中的排序，数字越小越靠前</span>
        </n-form-item>
        
        <n-form-item label="题目内容（题干）">
          <n-input 
            type="textarea" 
            v-model:value="questionForm.stem" 
            placeholder="请输入题目内容..."
            :rows="3"
            show-count
          />
          <div class="format-hint" v-if="questionForm.question_type === 3">提示：支持使用 <b>__</b> 表示填空位置，如：Java中声明常量的关键字是<b>__</b></div>
        </n-form-item>
        
        <n-form-item label="上传图片">
          <div class="upload-area">
            <input type="file" accept="image/*" class="file-input" @change="handleImageUpload" />
            <label class="upload-btn" for="image-upload">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              {{ questionForm.image_url ? '更换图片' : '上传图片' }}
            </label>
            <div v-if="questionForm.image_url" class="upload-preview">
              <img :src="questionForm.image_url" alt="题目图片" class="preview-img" />
              <button class="remove-media-btn" @click="removeImage">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          </div>
        </n-form-item>
        
        <n-form-item label="上传音频">
          <div class="upload-area">
            <input type="file" accept="audio/*" class="file-input" @change="handleAudioUpload" />
            <label class="upload-btn" for="audio-upload">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
              </svg>
              {{ questionForm.audio_url ? '更换音频' : '上传音频' }}
            </label>
            <div v-if="questionForm.audio_url" class="upload-preview audio-preview">
              <button class="audio-play-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="5 3 19 12 5 21 5 3"></polygon>
                </svg>
              </button>
              <span class="audio-name">{{ questionForm.audio_name || '音频文件' }}</span>
              <button class="remove-media-btn" @click="removeAudio">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          </div>
        </n-form-item>
        
        <!-- 选择题/多选题选项 -->
        <template v-if="questionForm.question_type === 1 || questionForm.question_type === 2">
          <n-form-item label="选项设置">
            <div class="options-editor">
              <div v-for="(opt, idx) in questionForm.options" :key="idx" class="option-row">
                <span class="opt-label">{{ opt.id }}.</span>
                <n-input 
                  v-model:value="opt.text" 
                  placeholder="选项内容"
                  style="flex: 1"
                />
                <n-checkbox 
                  v-if="questionForm.question_type === 1" 
                  :value="opt.id"
                  :checked="questionForm.answer === opt.id"
                  @update:checked="(v) => setSingleAnswer(opt.id, v)"
                  class="opt-check"
                >正确答案</n-checkbox>
                <n-checkbox 
                  v-else 
                  :value="opt.id"
                  :checked="isMultiAnswer(opt.id)"
                  @update:checked="(v) => toggleMultiAnswer(opt.id, v)"
                  class="opt-check"
                >正确答案</n-checkbox>
                <button class="btn-remove-opt" @click="removeOption(idx)" v-if="questionForm.options.length > 2">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                </button>
              </div>
              <n-button text type="primary" @click="addOption" class="add-option-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                添加选项
              </n-button>
            </div>
          </n-form-item>
        </template>
        
        <!-- 判断题答案 -->
        <n-form-item v-if="questionForm.question_type === 4" label="判断答案">
          <n-radio-group v-model:value="questionForm.answer">
            <n-radio :value="true">正确</n-radio>
            <n-radio :value="false">错误</n-radio>
          </n-radio-group>
        </n-form-item>
        
        <!-- 填空题答案 -->
        <n-form-item v-if="questionForm.question_type === 3" label="填空答案（按顺序填写）">
          <div class="fill-blanks-editor">
            <div v-for="(ans, idx) in questionForm.fillAnswers" :key="idx" class="fill-answer-row">
              <span class="fill-num">填空{{ idx + 1 }}</span>
              <n-input v-model:value="questionForm.fillAnswers[idx]" placeholder="答案" style="flex: 1" />
              <button class="btn-remove-opt" @click="removeFillAnswer(idx)" v-if="questionForm.fillAnswers.length > 1">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <n-button text type="primary" @click="addFillAnswer" class="add-option-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              添加填空
            </n-button>
          </div>
        </n-form-item>
        
        <!-- 简答题答案 -->
        <n-form-item v-if="questionForm.question_type === 5" label="参考答案">
          <n-input 
            type="textarea" 
            :value="String(questionForm.answer ?? '')" 
            @update:value="(v: string) => questionForm.answer = v" 
            placeholder="请输入参考答案..."
            :rows="4"
          />
        </n-form-item>
      </n-form>
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
          <n-input type="textarea" v-model:value="aiPrompt" placeholder="例如：根据高中物理牛顿第二定律生成题目，难度中等..." rows="5" />
        </n-form-item>
        <n-form-item label="生成题型及数量">
          <div class="ai-question-types">
            <div v-for="type in aiQuestionTypes" :key="type.value" class="ai-type-item">
              <span class="type-label">{{ type.label }}</span>
              <n-input-number v-model:value="aiQuestionCounts[type.value]" :min="0" :max="20" :style="{width: '80px'}" />
              <span class="type-hint">题</span>
            </div>
          </div>
        </n-form-item>
        <n-form-item label="示例说明">
          <div class="example-section">
            <div class="example-title">各种题型示例：</div>
            <div class="example-list">
              <div class="example-item">
                <strong>单选题：</strong>Java中，以下哪个关键字用于定义常量？
                <div class="example-options">A. const B. final C. static D. volatile</div>
                <div class="example-answer">答案：B</div>
              </div>
              <div class="example-item">
                <strong>多选题：</strong>以下哪些是Java的基本数据类型？
                <div class="example-options">A. int B. String C. boolean D. Object</div>
                <div class="example-answer">答案：A、C</div>
              </div>
              <div class="example-item">
                <strong>填空题：</strong>Java中，声明常量的关键字是______。
                <div class="example-answer">答案：final</div>
              </div>
              <div class="example-item">
                <strong>判断题：</strong>Java中的数组长度是可变的。
                <div class="example-answer">答案：错误</div>
              </div>
              <div class="example-item">
                <strong>简答题：</strong>请简述Java中equals()和==的区别。
                <div class="example-answer">答案：equals()比较内容，==比较引用地址...</div>
              </div>
            </div>
            <div class="media-example">
              <div class="example-title">多媒体题目示例：</div>
              <div class="example-item">
                <strong>图片题：</strong>观察下图，回答问题：<img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='40' viewBox='0 0 60 40'%3E%3Crect fill='%23E2E8F0' width='60' height='40'/%3E%3Ctext x='30' y='25' text-anchor='middle' font-size='12' fill='%2394A3B8'%3E图片占位%3C/text%3E%3C/svg%3E" alt="图片示例" />
              </div>
              <div class="example-item">
                <strong>音频题：</strong>听音频回答问题：
                <button class="audio-btn" disabled>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                    <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                    <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
                  </svg>
                  播放音频
                </button>
              </div>
            </div>
          </div>
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showAIModal = false">取消</n-button>
          <n-button type="primary" @click="generateAIQuestions">开始生成</n-button>
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
import { NSpin, NButton, NCheckbox, NModal, NForm, NFormItem, NInput, NRadioGroup, NRadio, NSelect, NInputNumber, NRadioButton } from 'naive-ui'

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
interface QuestionOption {
  id: string
  text: string
}

interface QuestionFormData {
  id?: number
  question_type: number
  default_score: number
  sort_order: number
  stem: string
  options: QuestionOption[]
  answer: string | boolean | null
  multiAnswers: string[]
  fillAnswers: string[]
  image_url: string
  audio_url: string
  audio_name: string
}

const defaultQuestionForm = (): QuestionFormData => ({
  question_type: 1,
  default_score: 5,
  sort_order: 0,
  stem: '',
  options: [
    { id: 'A', text: '' },
    { id: 'B', text: '' },
    { id: 'C', text: '' },
    { id: 'D', text: '' }
  ],
  answer: null,
  multiAnswers: [],
  fillAnswers: [''],
  image_url: '',
  audio_url: '',
  audio_name: ''
})

// === AI 出题相关 ===
const aiPrompt = ref('')
const aiQuestionTypes = [
  { label: '单选题', value: 1 },
  { label: '多选题', value: 2 },
  { label: '填空题', value: 3 },
  { label: '判断题', value: 4 },
  { label: '简答题', value: 5 }
]
const aiQuestionCounts = ref<Record<number, number>>({
  1: 0,
  2: 0,
  3: 0,
  4: 0,
  5: 0
})

function generateAIQuestions() {
  const total = Object.values(aiQuestionCounts.value).reduce((sum, val) => sum + val, 0)
  if (total === 0) {
    alert('请至少选择一种题型并设置数量')
    return
  }
  
  // 模拟AI生成题目
  let generatedCount = 0
  const newQuestions: Question[] = []
  
  // 根据选择的题型和数量生成题目
  Object.entries(aiQuestionCounts.value).forEach(([type, count]) => {
    const questionType = parseInt(type)
    for (let i = 0; i < count; i++) {
      const newQuestion = generateMockQuestion(questionType, generatedCount + 1)
      if (newQuestion) {
        newQuestions.push(newQuestion)
        generatedCount++
      }
    }
  })
  
  questions.value.push(...newQuestions)
  showAIModal.value = false
  alert(`成功生成 ${generatedCount} 道题目！`)
}

function generateMockQuestion(type: number, seq: number): Question | null {
  const bankId = activeBankId.value!
  const templates: Record<number, () => Question> = {
    1: () => ({
      id: Date.now() + seq,
      bank_id: bankId,
      question_type: 1,
      create_type: 1,
      default_score: 5,
      sort_order: questions.value.length + seq,
      _selected: false,
      content: {
        stem: `单选题${seq}：以下哪个选项是正确的？`,
        options: [
          { id: 'A', text: `选项A-${seq}` },
          { id: 'B', text: `选项B-${seq}` },
          { id: 'C', text: `选项C-${seq}` },
          { id: 'D', text: `选项D-${seq}` }
        ]
      },
      standard_answer: { answer: 'B' }
    }),
    2: () => ({
      id: Date.now() + seq,
      bank_id: bankId,
      question_type: 2,
      create_type: 1,
      default_score: 10,
      sort_order: questions.value.length + seq,
      _selected: false,
      content: {
        stem: `多选题${seq}：以下哪些选项是正确的？`,
        options: [
          { id: 'A', text: `选项A-${seq}` },
          { id: 'B', text: `选项B-${seq}` },
          { id: 'C', text: `选项C-${seq}` },
          { id: 'D', text: `选项D-${seq}` }
        ]
      },
      standard_answer: { answers: ['A', 'C'] }
    }),
    3: () => ({
      id: Date.now() + seq,
      bank_id: bankId,
      question_type: 3,
      create_type: 1,
      default_score: 8,
      sort_order: questions.value.length + seq,
      _selected: false,
      content: {
        stem: `填空题${seq}：______是一种重要的编程概念。`
      },
      standard_answer: { answers: ['抽象'] }
    }),
    4: () => ({
      id: Date.now() + seq,
      bank_id: bankId,
      question_type: 4,
      create_type: 1,
      default_score: 2,
      sort_order: questions.value.length + seq,
      _selected: false,
      content: {
        stem: `判断题${seq}：Java是面向对象的编程语言。`
      },
      standard_answer: { answer: true }
    }),
    5: () => ({
      id: Date.now() + seq,
      bank_id: bankId,
      question_type: 5,
      create_type: 1,
      default_score: 15,
      sort_order: questions.value.length + seq,
      _selected: false,
      content: {
        stem: `简答题${seq}：请简述面向对象的三大特性。`
      },
      standard_answer: { answer: '封装、继承、多态。' }
    })
  }
  
  return templates[type]?.() || null
}

// === 媒体上传相关 ===
function handleImageUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      questionForm.value.image_url = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

function handleAudioUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    questionForm.value.audio_url = `audio://${file.name}`
    questionForm.value.audio_name = file.name
  }
}

function removeImage() {
  questionForm.value.image_url = ''
}

function removeAudio() {
  questionForm.value.audio_url = ''
  questionForm.value.audio_name = ''
}

// --- 题目表单原有逻辑 ---
const showQuestionModal = ref(false)
const questionForm = ref<QuestionFormData>(defaultQuestionForm())

function openQuestionModal(q?: Question) {
  if (q) {
    // 编辑模式
    const options: QuestionOption[] = q.content.options ? q.content.options.map((o: any) => ({ id: String(o.id), text: o.text })) : [
      { id: 'A', text: '' },
      { id: 'B', text: '' },
      { id: 'C', text: '' },
      { id: 'D', text: '' }
    ]
    
    let answer: string | boolean | null = null
    let multiAnswers: string[] = []
    let fillAnswers: string[] = ['']
    
    if (q.question_type === 1) {
      answer = q.standard_answer?.answer ? String(q.standard_answer.answer) : null
    } else if (q.question_type === 2) {
      multiAnswers = q.standard_answer?.answers?.map(String) || []
    } else if (q.question_type === 3) {
      fillAnswers = q.standard_answer?.answers?.length ? q.standard_answer.answers : ['']
    } else if (q.question_type === 4) {
      answer = q.standard_answer?.answer ?? null
    } else if (q.question_type === 5) {
      answer = q.standard_answer?.answer || ''
    }
    
    questionForm.value = {
      id: q.id,
      question_type: q.question_type,
      default_score: q.default_score,
      sort_order: 0,
      stem: q.content.stem || '',
      options,
      answer,
      multiAnswers,
      fillAnswers,
      image_url: '',
      audio_url: '',
      audio_name: ''
    }
  } else {
    // 新增模式
    questionForm.value = defaultQuestionForm()
  }
  showQuestionModal.value = true
}

function saveQuestion() {
  const form = questionForm.value
  if (!form.stem) {
    alert('请输入题目内容')
    return
  }
  
  let content: any = { stem: form.stem }
  let standard_answer: any = {}
  
  if (form.question_type === 1 || form.question_type === 2) {
    content.options = form.options.filter((o) => o.text)
    if (form.question_type === 1) {
      standard_answer = { answer: form.answer }
    } else {
      standard_answer = { answers: form.multiAnswers }
    }
  } else if (form.question_type === 3) {
    standard_answer = { answers: form.fillAnswers.filter((a) => a) }
  } else if (form.question_type === 4) {
    standard_answer = { answer: form.answer }
  } else if (form.question_type === 5) {
    standard_answer = { answer: form.answer }
  }
  
  const newQuestion: Question = {
    id: form.id ?? Date.now(),
    bank_id: activeBankId.value!,
    question_type: form.question_type,
    create_type: 2,
    content,
    standard_answer,
    default_score: form.default_score,
    _selected: false
  }
  
  if (form.id) {
    // 更新
    const idx = questions.value.findIndex(q => q.id === form.id)
    if (idx > -1) questions.value[idx] = newQuestion
  } else {
    // 新增
    questions.value.push(newQuestion)
  }
  
  showQuestionModal.value = false
}

// === 选项操作 ===
function addOption() {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const nextId = letters[questionForm.value.options.length] as string
  if (nextId) {
    questionForm.value.options.push({ id: nextId, text: '' })
  }
}

function removeOption(idx: number) {
  if (questionForm.value.options.length > 2) {
    questionForm.value.options.splice(idx, 1)
  }
}

function setSingleAnswer(optId: string, checked: boolean) {
  questionForm.value.answer = checked ? optId : null
}

function isMultiAnswer(optId: string) {
  return questionForm.value.multiAnswers?.includes(optId)
}

function toggleMultiAnswer(optId: string, checked: boolean) {
  if (checked) {
    if (!questionForm.value.multiAnswers) questionForm.value.multiAnswers = []
    questionForm.value.multiAnswers.push(optId)
  } else {
    questionForm.value.multiAnswers = questionForm.value.multiAnswers.filter((a: string) => a !== optId)
  }
}

function addFillAnswer() {
  questionForm.value.fillAnswers.push('')
}

function removeFillAnswer(idx: number) {
  if (questionForm.value.fillAnswers.length > 1) {
    questionForm.value.fillAnswers.splice(idx, 1)
  }
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
.main-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; margin-bottom: 16px; border-bottom: 1px solid #E2E8F0; }
.header-left { padding-top: 8px; }
.header-left .main-title { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 6px 0; }
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

/* 题目编辑表单样式 */
.question-form {
  padding: 0 8px;
}

.format-hint {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 8px;
  line-height: 1.5;
}

.options-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.opt-label {
  font-weight: 600;
  color: #475569;
  min-width: 24px;
}

.opt-check {
  min-width: 80px;
  margin-left: 8px;
}

.btn-remove-opt {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #94A3B8;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  transition: 0.2s;
}

.btn-remove-opt:hover {
  background: #FEE2E2;
  color: #EF4444;
}

.add-option-btn {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.fill-blanks-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fill-answer-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fill-num {
  font-weight: 600;
  color: #475569;
  min-width: 60px;
}

/* 滚动条美化 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

/* === AI 出题弹窗样式 === */
.ai-question-types {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.ai-type-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-label {
  font-size: 14px;
  color: #475569;
  min-width: 60px;
}

.type-hint {
  font-size: 13px;
  color: #94A3B8;
}

.example-section {
  background: #FAFAFA;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.example-title {
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
  font-size: 14px;
}

.example-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-item {
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
}

.example-options {
  margin: 4px 0 4px 20px;
  color: #64748B;
}

.example-answer {
  margin-left: 20px;
  color: #10B981;
  font-weight: 500;
}

.media-example {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #E2E8F0;
}

.audio-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #EEF2FF;
  border: 1px solid #C7D2FE;
  border-radius: 6px;
  color: #4338CA;
  font-size: 12px;
  cursor: not-allowed;
}

/* === 上传区域样式 === */
.upload-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-input {
  display: none;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #F8FAFC;
  border: 1px dashed #CBD5E1;
  border-radius: 8px;
  color: #64748B;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  background: #F1F5F9;
  border-color: #94A3B8;
  color: #334155;
}

.upload-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #F8FAFC;
  border-radius: 8px;
}

.preview-img {
  max-width: 120px;
  max-height: 80px;
  border-radius: 6px;
  object-fit: cover;
}

.audio-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.audio-play-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #EEF2FF;
  border: none;
  border-radius: 50%;
  color: #4338CA;
  cursor: pointer;
  transition: all 0.2s;
}

.audio-play-btn:hover {
  background: #E0E7FF;
}

.audio-name {
  flex: 1;
  font-size: 14px;
  color: #475569;
}

.remove-media-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FEE2E2;
  border: none;
  border-radius: 6px;
  color: #EF4444;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-media-btn:hover {
  background: #FECACA;
}

.sort-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #94A3B8;
}
</style>