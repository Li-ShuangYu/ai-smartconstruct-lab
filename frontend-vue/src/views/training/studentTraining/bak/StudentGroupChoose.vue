<template>
  <div class="student-home-container">
    <transition name="fade-in">
      <div v-if="showContent" class="home-header">
        <div class="header-text">
          <h1 class="panel-main-title">课题攻坚方向选择</h1>
          <p class="section-desc">请根据你的专业背景选择一个密码学研究方向，加入对应的协同小队。</p>
        </div>
        <div class="header-action">
          <button 
            @click="confirmSelection"
            :disabled="selectedGroupId === null || isLoading"
            :class="['action-btn', { 'btn-active': selectedGroupId !== null && !isLoading }]"
          >
            <template v-if="isLoading">
              <i class="fas fa-spinner fa-spin mr-2"></i>处理中...
            </template>
            <template v-else>
              {{ selectedGroupId !== null ? '确认加入小队' : '请先选择方向' }}
              <i v-if="selectedGroupId !== null" class="fas fa-arrow-right ml-2"></i>
            </template>
          </button>
        </div>
      </div>
    </transition>
    
    <div class="groups-grid">
      <transition-group name="fade-up">
        <div 
          v-for="(group, index) in groups" 
          :key="index"
          v-if="showContent"
          @click="selectGroup(index)"
          :class="[
            'group-card glass-card',
            { 'is-selected': selectedGroupId === index }
          ]"
        >
          <div v-if="selectedGroupId === index" class="select-badge">
            <i class="fas fa-check-circle"></i>
          </div>

          <div class="card-header">
            <div class="role-badge" :style="{ backgroundColor: group.themeColor }">
              组{{ index + 1 }}
            </div>
            <div class="title-meta">
              <h2 class="group-title">{{ group.title }}</h2>
              <span class="algorithm-tag" :style="{ color: group.themeColor, borderColor: group.themeColor + '40' }">
                {{ group.algorithm }}
              </span>
            </div>
          </div>
          
          <div class="card-body">
            <div class="info-section">
              <div class="label-row">
                <i class="fas fa-fingerprint"></i>
                <span>人物画像：{{ group.persona }}</span>
              </div>
              <div class="traits-tags">
                <span v-for="(trait, tIndex) in group.traits" :key="tIndex" class="trait-tag">
                  {{ trait }}
                </span>
              </div>
            </div>

            <div class="style-box">
              <h3 class="mini-label">🎯 做事风格</h3>
              <p class="style-desc">{{ group.style }}</p>
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <transition name="fade">
      <div v-if="showTeamSuccessModal" class="modal-overlay">
        <div class="modal-content glass-card">
          <div class="modal-icon-wrapper" :style="{ backgroundColor: groups[selectedGroupId].themeColor }">
            <i class="fas fa-users"></i>
          </div>
          <h3 class="modal-title">组队成功！</h3>
          <p class="modal-desc">你已加入 <span :style="{ color: groups[selectedGroupId].themeColor }">{{ groups[selectedGroupId].title }}</span></p>
          
          <div class="team-list">
            <div v-for="(member, index) in teamMembers" :key="index" class="member-item">
              <div class="member-avatar" :style="{ backgroundColor: groups[selectedGroupId].themeColor }">
                {{ member.name.charAt(0) }}
              </div>
              <div class="member-info">
                <span class="m-name">{{ member.name }}</span>
                <span class="m-role">{{ member.role }} · {{ member.specialty }}</span>
              </div>
            </div>
          </div>
          
          <button @click="goToTaskSelect" class="primary-btn full-width">
            进入接取任务界面 <i class="fas fa-chevron-right ml-2"></i>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const selectedGroupId = ref(null);
const isLoading = ref(false);
const showContent = ref(false);
const showTeamSuccessModal = ref(false);
const teamMembers = ref([
  { name: '张三', role: '组长', specialty: '密码学算法' },
  { name: '李四', role: '组员', specialty: '硬件实现' },
  { name: '王五', role: '组员', specialty: '安全分析' }
]);

const selectGroup = (index) => {
  selectedGroupId.value = index;
};

// 【修改点：补充数据提取和写入 localStorage 的逻辑】
const confirmSelection = () => {
  if (selectedGroupId.value !== null) {
    isLoading.value = true;
    
    // 1. 提取当前选中的组别数据
    const selectedGroup = groups[selectedGroupId.value];
    
    // 2. 补全下游页面需要的类名映射主题（适配下游通过 class 获取颜色的逻辑）
    const themeMap = [
      { borderClass: 'border-blue-900/40', activeBorderClass: 'border-blue-500', bgHighlightClass: 'bg-blue-50', badgeBgClass: 'bg-blue-600', textClass: 'text-blue-600', tagClass: 'bg-blue-100 text-blue-600 border-blue-200', shadowClass: 'shadow-[0_4px_15px_rgba(59,130,246,0.15)]' },
      { borderClass: 'border-red-900/40', activeBorderClass: 'border-red-500', bgHighlightClass: 'bg-red-50', badgeBgClass: 'bg-red-600', textClass: 'text-red-600', tagClass: 'bg-red-100 text-red-600 border-red-200', shadowClass: 'shadow-[0_4px_15px_rgba(239,68,68,0.15)]' },
      { borderClass: 'border-amber-900/40', activeBorderClass: 'border-amber-500', bgHighlightClass: 'bg-amber-50', badgeBgClass: 'bg-amber-500', textClass: 'text-amber-600', tagClass: 'bg-amber-100 text-amber-600 border-amber-200', shadowClass: 'shadow-[0_4px_15px_rgba(245,158,11,0.15)]' },
      { borderClass: 'border-purple-900/40', activeBorderClass: 'border-purple-500', bgHighlightClass: 'bg-purple-50', badgeBgClass: 'bg-purple-600', textClass: 'text-purple-600', tagClass: 'bg-purple-100 text-purple-600 border-purple-200', shadowClass: 'shadow-[0_4px_15px_rgba(168,85,247,0.15)]' }
    ];

    // 3. 打包数据
    const groupInfo = {
      groupId: selectedGroupId.value + 1, // 编号 1-4
      groupName: selectedGroup.title,
      algorithm: selectedGroup.algorithm,
      persona: selectedGroup.persona,
      traits: selectedGroup.traits,
      style: selectedGroup.style,
      colorTheme: themeMap[selectedGroupId.value],
      selectTime: new Date().toISOString()
    };

    // 4. 塞进 localStorage
    localStorage.setItem('selectedGroupInfo', JSON.stringify(groupInfo));
    console.log('队伍数据已打包存入本地缓存:', groupInfo);

    // 5. 继续原有的UI交互（等待 -> 弹窗）
    setTimeout(() => {
      isLoading.value = false;
      showTeamSuccessModal.value = true;
    }, 1500);
  }
};

const goToTaskSelect = () => {
  showTeamSuccessModal.value = false;
  router.push('/training/student-training/student-task-select');
};

onMounted(() => {
  setTimeout(() => showContent.value = true, 100);
});

const groups = [
  {
    title: '低功耗优化方向',
    algorithm: 'PRESENT 轻量级密码算法',
    persona: '极致的“精算师”与“极简主义者”',
    traits: ['极其自律', '务实导向', '算力断舍离'],
    style: '目标导向极强，懂得Trade-off。别人追求花哨，你追求“刚刚好”。',
    themeColor: '#4F46E5'
  },
  {
    title: '侧信道防护方向',
    algorithm: 'AES + 掩码防护防线',
    persona: '敏锐的“心理弈者”与“细节控”',
    traits: ['心思缜密', '极度敏锐', '防御型思维'],
    style: '习惯性保持防御姿态，不仅关注正面逻辑，更关注物理暗处泄露的把柄。',
    themeColor: '#EF4444'
  },
  {
    title: '抗重放攻击方向',
    algorithm: '动态随机数校验协议',
    persona: '严谨的“秩序守卫者”与“史官”',
    traits: ['精神洁癖', '原则性强', '活在当下'],
    style: '对“炒冷饭”深恶痛绝，做事一码归一码，有着严格的校验机制和底线。',
    themeColor: '#F59E0B'
  },
  {
    title: '后量子算法适配方向',
    algorithm: 'Kyber 密钥封装防降维打击',
    persona: '前卫的“未来先锋”与“破壁人”',
    traits: ['危机意识', '高瞻远瞩', '拥抱变化'],
    style: '绝对的长期主义者，对颠覆性新技术充满好奇。敢于做第一个吃螃蟹的人。',
    themeColor: '#8B5CF6'
  }
];
</script>

<style scoped>
.student-home-container {
  padding: 0px 16px;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.panel-main-title {
  font-size: 24px;
  font-weight: 800;
  color: #0F172A;
  margin-bottom: 4px;
}

.section-desc {
  font-size: 14px;
  color: #64748B;
}

/* 按钮样式统一 */
.action-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  border: none;
  background: #E2E8F0;
  color: #94A3B8;
  cursor: not-allowed;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-active {
  background: #4F46E5;
  color: white;
  cursor: pointer;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.3);
}

.btn-active:hover {
  background: #4338CA;
  transform: translateY(-2px);
}

/* 紧凑网格布局 */
.groups-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.group-card {
  padding: 20px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.group-card:hover {
  transform: translateY(-4px);
  border-color: #4F46E5;
  background: rgba(255, 255, 255, 0.9);
}

.group-card.is-selected {
  border-color: #4F46E5;
  background: #F5F7FF;
  box-shadow: 0 10px 25px -5px rgba(79, 70, 229, 0.1);
}

.select-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #4F46E5;
  font-size: 20px;
}

/* 卡片内部紧凑排版 */
.card-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.role-badge {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
  flex-shrink: 0;
}

.group-title {
  font-size: 18px;
  font-weight: 800;
  color: #1E293B;
  margin-bottom: 4px;
}

.algorithm-tag {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid;
  background: rgba(255, 255, 255, 0.5);
}

.info-section {
  margin-bottom: 16px;
}

.label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 8px;
}

.traits-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.trait-tag {
  font-size: 12px;
  background: #F1F5F9;
  color: #475569;
  padding: 3px 10px;
  border-radius: 6px;
  font-weight: 600;
}

.style-box {
  background: #F8FAFC;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #F1F5F9;
}

.mini-label {
  font-size: 12px;
  font-weight: 800;
  color: #94A3B8;
  margin-bottom: 6px;
  text-transform: uppercase;
}

.style-desc {
  font-size: 13px;
  line-height: 1.5;
  color: #475569;
}

/* 弹窗样式重构 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  text-align: center;
}

.modal-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
}

.modal-title { font-size: 20px; font-weight: 800; color: #1E293B; margin-bottom: 4px; }
.modal-desc { font-size: 14px; color: #64748B; margin-bottom: 24px; }

.team-list {
  background: #F8FAFC;
  border-radius: 12px;
  padding: 8px;
  margin-bottom: 24px;
  text-align: left;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-bottom: 1px solid #F1F5F9;
}

.member-item:last-child { border: none; }

.member-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}

.m-name { display: block; font-size: 14px; font-weight: 700; color: #1E293B; }
.m-role { font-size: 12px; color: #94A3B8; }

.primary-btn {
  background: #0F172A;
  color: white;
  border: none;
  padding: 14px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn:hover { background: #1E293B; }
.full-width { width: 100%; }

/* 动画效果 */
.fade-up-enter-active { transition: all 0.4s ease-out; }
.fade-up-enter-from { opacity: 0; transform: translateY(20px); }

.fade-in-enter-active { transition: opacity 0.5s ease; }
.fade-in-enter-from { opacity: 0; }
</style>