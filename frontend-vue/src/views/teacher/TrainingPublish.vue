<template>
  <div class="publish-wrapper">
    <div class="publish-card">
      <header class="publish-header">
        <h1 class="panel-main-title">实例化实训副本</h1>
        <p class="subtitle">配置实训基础信息并下发底层计算资源</p>
      </header>
      
      <main class="form-layout">
        <div class="form-column">
          <h2 class="column-title">1. 基础与排期</h2>
          
          <section class="form-group">
            <label>副本名称 <span class="required">*</span></label>
            <input type="text" v-model="formData.name" placeholder="例如：2026春季学期-密码工程01班实测" class="base-input" />
          </section>

          <section class="form-group">
            <label>实训时间范围</label>
            <div class="time-range-picker">
              <input type="datetime-local" v-model="formData.startTime" class="base-input time-input" />
              <span class="separator">至</span>
              <input type="datetime-local" v-model="formData.endTime" class="base-input time-input" />
            </div>
          </section>

          <section class="form-group">
            <label>指派参训学员</label>
            <div class="student-selector">
              <div v-for="cls in formData.selectedClasses" :key="cls.id" class="group-chip">
                {{ cls.course }} - {{ cls.name }} ({{ cls.count }}人)
                <span class="close-icon" @click="removeClass(cls.id)">×</span>
              </div>
              <button class="add-btn" @click="openClassModal">+ 选取班级/课程</button>
            </div>
          </section>
        </div>

        <div class="form-column">
          <h2 class="column-title">2. 资源调度与环境</h2>

          <section class="form-group">
            <label>计算资源下发方案 <span class="required">*</span></label>
            <div class="resource-options">
              <div 
                class="res-card" 
                :class="{ active: formData.resourceType === 'k8s' }"
                @click="formData.resourceType = 'k8s'"
              >
                <div class="res-title">标准 K8s 容器环境</div>
                <p>为每位学员独立下发全隔离的靶机环境。</p>
                <div class="check-mark" v-if="formData.resourceType === 'k8s'">✓</div>
              </div>
              <div 
                class="res-card" 
                :class="{ active: formData.resourceType === 'web' }"
                @click="formData.resourceType = 'web'"
              >
                <div class="res-title">轻量级 Web 模拟环境</div>
                <p>仅支持算法逻辑验证，无底层通信仿真。</p>
                <div class="check-mark" v-if="formData.resourceType === 'web'">✓</div>
              </div>
            </div>
          </section>

          <transition name="fade-slide">
            <div class="advanced-options" v-if="formData.resourceType === 'k8s'">
              <div class="form-row">
                <section class="form-group flex-1">
                  <label>目标集群</label>
                  <select v-model="formData.cluster" class="base-input select-input">
                    <option value="cluster-155">155 业务服务器组</option>
                    <option value="cluster-158">158 备用服务器组</option>
                  </select>
                </section>
                <section class="form-group flex-1">
                  <label>预装算法镜像</label>
                  <select v-model="formData.baseImage" class="base-input select-input">
                    <option value="sm-suite">SM3/SM4 国密套件 v2.1</option>
                    <option value="pqc-suite">PQC 抗量子密码测试包</option>
                    <option value="anti-replay">UAV 抗重放通信靶机</option>
                  </select>
                </section>
              </div>

              <section class="switch-group">
                <div class="switch-info">
                  <span class="switch-label">实训结束后自动回收容器</span>
                  <span class="switch-desc">到达结束时间后自动清理 K8s Pod 释放资源</span>
                </div>
                <label class="toggle-switch">
                  <input type="checkbox" v-model="formData.autoDestroy">
                  <span class="slider"></span>
                </label>
              </section>
            </div>
          </transition>
        </div>
      </main>

      <footer class="footer-actions">
        <button class="cancel-btn">取消并返回</button>
        <button class="confirm-btn">立即下发并启动 (预计消耗 {{ totalSelectedStudents }} Pods)</button>
      </footer>
    </div>

    <transition name="modal-bounce">
      <div v-if="showClassModal" class="class-modal-overlay" @click.self="closeClassModal">
        <div class="class-modal-card">
          <header class="modal-header">
            <h3>指派参训班级与课程</h3>
            <span class="close-btn" @click="closeClassModal">×</span>
          </header>

          <div class="modal-tabs">
            <div 
              class="tab-item" 
              :class="{ active: modalTab === 'select' }" 
              @click="modalTab = 'select'"
            >选取现有班级</div>
            <div 
              class="tab-item" 
              :class="{ active: modalTab === 'create' }" 
              @click="modalTab = 'create'"
            >新建课程与班级</div>
          </div>

          <div class="modal-body">
            <div v-if="modalTab === 'select'" class="select-mode">
              <div class="class-grid">
                <div 
                  v-for="cls in mockClasses" 
                  :key="cls.id" 
                  class="class-item-card"
                  :class="{ selected: tempSelectedIds.includes(cls.id) }"
                  @click="toggleSelectClass(cls.id)"
                >
                  <div class="class-course">{{ cls.course }}</div>
                  <div class="class-name">{{ cls.name }}</div>
                  <div class="class-count">👥 {{ cls.count }} 人</div>
                  <div class="item-check" v-if="tempSelectedIds.includes(cls.id)">✓</div>
                </div>
              </div>
            </div>

            <div v-if="modalTab === 'create'" class="create-mode">
              <div class="form-group">
                <label>所属课程名称</label>
                <input type="text" v-model="newClassForm.course" placeholder="如：后量子密码学实务" class="base-input" />
              </div>
              <div class="form-row">
                <div class="form-group flex-1">
                  <label>班级名称</label>
                  <input type="text" v-model="newClassForm.name" placeholder="如：26级信安02班" class="base-input" />
                </div>
                <div class="form-group" style="width: 120px;">
                  <label>学员人数</label>
                  <input type="number" v-model="newClassForm.count" placeholder="0" class="base-input" min="1" />
                </div>
              </div>
            </div>
          </div>

          <footer class="modal-footer">
            <button class="cancel-btn" @click="closeClassModal">取消</button>
            <button class="confirm-btn" @click="confirmClassSelection">
              {{ modalTab === 'select' ? `确认指派 (${tempSelectedIds.length})` : '创建并指派' }}
            </button>
          </footer>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'

// --- 基础表单数据 ---
const formData = reactive({
  name: '',
  startTime: '',
  endTime: '',
  resourceType: 'k8s',
  cluster: 'cluster-155',
  baseImage: 'sm-suite',
  autoDestroy: true,
  selectedClasses: [] as any[] // 存储最终确认的班级
})

// 计算预计消耗的 Pod 数量 (按人数计算)
const totalSelectedStudents = computed(() => {
  if (formData.resourceType === 'web') return 0
  return formData.selectedClasses.reduce((sum, cls) => sum + cls.count, 0)
})

// 删除已选班级
const removeClass = (id: number) => {
  formData.selectedClasses = formData.selectedClasses.filter(c => c.id !== id)
}

// --- 弹窗逻辑与状态 ---
const showClassModal = ref(false)
const modalTab = ref('select') // 'select' | 'create'
const tempSelectedIds = ref<number[]>([]) // 暂存弹窗中选中的 ID

// 模拟数据库里现有的课程/班级数据
const mockClasses = ref([
  { id: 1, course: '密码工程实务', name: '26春季-密码01班', count: 32 },
  { id: 2, course: '密码工程实务', name: '26春季-密码02班', count: 28 },
  { id: 3, course: '后量子密码学', name: '26春季-信安01班', count: 45 },
  { id: 4, course: '网络安全基础', name: '25秋季-计科03班', count: 38 },
])

// 新建班级表单
const newClassForm = reactive({
  course: '',
  name: '',
  count: 30
})

const openClassModal = () => {
  // 每次打开弹窗时，将当前已选的 ID 赋值给临时数组
  tempSelectedIds.value = formData.selectedClasses.map(c => c.id)
  modalTab.value = 'select'
  showClassModal.value = true
}

const closeClassModal = () => {
  showClassModal.value = false
}

// 弹窗内的卡片点击切换选中状态
const toggleSelectClass = (id: number) => {
  const index = tempSelectedIds.value.indexOf(id)
  if (index === -1) {
    tempSelectedIds.value.push(id)
  } else {
    tempSelectedIds.value.splice(index, 1)
  }
}

// 点击确认按钮
const confirmClassSelection = () => {
  if (modalTab.value === 'select') {
    // 根据暂存的 ID 找出真实的班级对象，赋值给 formData
    formData.selectedClasses = mockClasses.value.filter(c => tempSelectedIds.value.includes(c.id))
  } else if (modalTab.value === 'create') {
    // 校验简单逻辑
    if (!newClassForm.course || !newClassForm.name) {
      alert('请填写完整课程与班级名称')
      return
    }
    // 创建新班级对象并推入现有列表和已选列表
    const newClass = {
      id: Date.now(),
      course: newClassForm.course,
      name: newClassForm.name,
      count: Number(newClassForm.count)
    }
    mockClasses.value.push(newClass)
    formData.selectedClasses.push(newClass)
    
    // 清空新建表单
    newClassForm.course = ''
    newClassForm.name = ''
    newClassForm.count = 30
  }
  closeClassModal()
}
</script>

<style scoped>
/* ====== 页面基础布局 (维持不变) ====== */
.publish-wrapper {
  height: calc(100vh - 64px); 
  background: #F1F5F9;
  padding: 24px 32px; 
  font-family: Inter, system-ui, sans-serif;
  font-size: 15px;
  color: #334155;
  box-sizing: border-box;
  overflow: hidden; 
}

.publish-card {
  /* width: 100%;
  max-width: 1400px; */
  height: 100%; 
  margin: 0 auto;
  background: #FFFFFF;
  border-radius: 16px;
  padding: 32px 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column; 
  box-sizing: border-box;
}

.publish-header {
  flex-shrink: 0;

  border-bottom: 1px solid #E2E8F0;
  padding-bottom: 20px;
}

.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #64748B; margin: 0; }

.form-layout {
    padding-top: 24px;
  flex: 1; 
  min-height: 0; 
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px; 
  /* margin-bottom: 24px; */
}

.form-layout {
  overflow-y: auto; 
  padding-right: 16px; 
}

/* 品牌色极细滚动条 */
.form-layout::-webkit-scrollbar { width: 4px; }
.form-layout::-webkit-scrollbar-track { background: transparent; }
.form-layout::-webkit-scrollbar-thumb { background: rgba(79, 70, 229, 0.4); border-radius: 4px; transition: 0.3s ease; }
.form-layout::-webkit-scrollbar-thumb:hover { background: #4F46E5; }

.column-title { font-size: 16px; font-weight: 700; color: #1E293B; margin: 0 0 24px 0; padding-bottom: 12px; border-bottom: 2px solid #F1F5F9; }

/* 基础表单元素 */
.form-group { margin-bottom: 28px; }
.form-row { display: flex; gap: 20px; }
.flex-1 { flex: 1; }
.form-group label { display: block; font-size: 14px; font-weight: 600; color: #475569; margin-bottom: 10px; }
.required { color: #EF4444; margin-left: 4px; }

.base-input {
  width: 100%; padding: 12px 16px; border: 1px solid #CBD5E1; border-radius: 10px; font-size: 15px; color: #0F172A; background: #F8FAFC; transition: all 0.2s ease; box-sizing: border-box; font-family: inherit;
}
.base-input:hover { border-color: #94A3B8; }
.base-input:focus { outline: none; background: #FFFFFF; border-color: #818CF8; box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1); }
.select-input { cursor: pointer; appearance: auto; }

.time-range-picker { display: flex; align-items: center; gap: 12px; }
.time-input { flex: 1; }
.separator { color: #94A3B8; font-size: 14px; font-weight: 500; }

.student-selector {
  display: flex; flex-wrap: wrap; gap: 12px; padding: 20px; background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 12px;
}
.group-chip {
  background: #EEF2FF; color: #4F46E5; padding: 8px 12px 8px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 8px; box-shadow: 0 2px 6px rgba(79,70,229,0.05);
}
.close-icon {
  display: inline-block; width: 20px; height: 20px; line-height: 18px; text-align: center; border-radius: 50%; background: rgba(79, 70, 229, 0.15); cursor: pointer; transition: 0.2s;
}
.close-icon:hover { background: #4F46E5; color: #FFF; }

.add-btn {
  background: #FFFFFF; border: 1px dashed #94A3B8; color: #475569; padding: 8px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; cursor: pointer; transition: 0.2s;
}
.add-btn:hover { border-color: #4F46E5; color: #4F46E5; background: #F5F3FF; }

.resource-options { display: flex; flex-direction: column; gap: 16px; }
.res-card { position: relative; padding: 20px 24px; border: 2px solid #E2E8F0; border-radius: 12px; cursor: pointer; background: #FFFFFF; transition: all 0.2s ease; overflow: hidden; }
.res-card:hover { border-color: #C7D2FE; background: #FAFAFF; }
.res-card.active { border-color: #4F46E5; background: #EEF2FF; }
.res-title { font-size: 16px; font-weight: 700; color: #1E293B; margin-bottom: 6px; }
.res-card p { font-size: 14px; color: #64748B; margin: 0; }
.check-mark { position: absolute; top: 0; right: 0; width: 36px; height: 36px; background: #4F46E5; color: #FFF; font-weight: bold; display: flex; align-items: center; justify-content: center; border-bottom-left-radius: 12px; font-size: 15px; }

.advanced-options { margin-top: 28px; padding-top: 28px; border-top: 1px dashed #E2E8F0; }
.switch-group { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #F8FAFC; border-radius: 12px; }
.switch-info { display: flex; flex-direction: column; gap: 6px; }
.switch-label { font-size: 15px; font-weight: 600; color: #1E293B; }
.switch-desc { font-size: 13px; color: #64748B; }

.toggle-switch { position: relative; display: inline-block; width: 48px; height: 26px; }
.toggle-switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #CBD5E1; transition: .4s; border-radius: 26px; }
.slider:before { position: absolute; content: ""; height: 20px; width: 20px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
input:checked + .slider { background-color: #10B981; }
input:checked + .slider:before { transform: translateX(22px); }

.footer-actions { flex-shrink: 0; display: flex; justify-content: flex-end; align-items: center; gap: 20px; padding-top: 24px; border-top: 1px solid #E2E8F0; }

.confirm-btn { background: #4F46E5; color: white; border: none; padding: 14px 32px; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2); }
.confirm-btn:hover { background: #4338CA; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(79, 70, 229, 0.3); }

.cancel-btn { background: transparent; color: #64748B; border: 1px solid #CBD5E1; padding: 13px 28px; border-radius: 10px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.cancel-btn:hover { background: #F8FAFC; border-color: #94A3B8; color: #0F172A; }


/* ====== 新增：班级/课程选择弹窗 ====== */
.class-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  /* 极其轻微的透明模糊，与之前实训创建页的要求保持统一 */
  background: transparent; 
  backdrop-filter: blur(4px); 
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.class-modal-card {
  background: #FFFFFF;
  border-radius: 16px;
  width: 640px;
  max-height: 80vh;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #F1F5F9;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #0F172A; }
.close-btn { font-size: 24px; color: #94A3B8; cursor: pointer; line-height: 1; transition: 0.2s; }
.close-btn:hover { color: #EF4444; }

.modal-tabs {
  display: flex;
  background: #F8FAFC;
  padding: 12px 24px 0;
  gap: 24px;
  border-bottom: 1px solid #E2E8F0;
}
.tab-item {
  padding: 10px 4px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #64748B;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.tab-item:hover:not(.active) { color: #0F172A; }

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.select-mode .class-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.class-item-card {
  position: relative;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.class-item-card:hover { border-color: #818CF8; background: #FAFAFF; }
.class-item-card.selected { border-color: #4F46E5; background: #EEF2FF; }

.class-course { font-size: 12px; color: #64748B; margin-bottom: 6px; }
.class-name { font-size: 16px; font-weight: 700; color: #0F172A; margin-bottom: 12px; }
.class-count { font-size: 13px; color: #475569; font-weight: 500; background: #FFFFFF; display: inline-block; padding: 4px 8px; border-radius: 6px; border: 1px solid #E2E8F0; }

.item-check {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 22px;
  height: 22px;
  background: #4F46E5;
  color: #FFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.create-mode {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #E2E8F0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #F8FAFC;
}

/* 弹窗动画 */
.modal-bounce-enter-active { animation: bounce-in 0.3s; }
.modal-bounce-leave-active { animation: bounce-in 0.2s reverse; }
@keyframes bounce-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>