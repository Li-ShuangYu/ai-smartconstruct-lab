<template>
  <aside class="admin-sidebar">
    <div class="sidebar-scroll-area">
      <nav class="nav-groups">
        
        <div class="menu-group" v-for="group in adminMenus" :key="group.id">
          
          <div class="group-header" @click="toggleGroup(group)">
            <span class="group-title">{{ group.title }}</span>
            <FontAwesomeIcon 
              icon="fa-solid fa-chevron-down" 
              class="toggle-icon" 
              :class="{ 'is-collapsed': !group.expanded }"
            />
          </div>

          <div class="group-items" v-show="group.expanded">
            <router-link 
              v-for="item in group.children" 
              :key="item.path"
              :to="item.path" 
              class="nav-item" 
              active-class="active"
            >
              <FontAwesomeIcon :icon="item.icon" /> {{ item.name }}
            </router-link>
          </div>
          
        </div>

      </nav>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 侧边栏菜单数据配置
const adminMenus = ref([
  {
    id: 'user',
    title: '用户中心',
    expanded: true, // 默认展开
    children: [
      { name: '临时测试', path: '/admin/training-test', icon: 'fa-solid fa-user-tie' },
      { name: '教师管理', path: '/admin/teacher', icon: 'fa-solid fa-user-tie' },
      { name: '学生统管', path: '/admin/student', icon: 'fa-solid fa-user-graduate' },
      { name: '菜单权限', path: '/admin/menu', icon: 'fa-solid fa-list' }
    ]
  },
  {
    id: 'edu',
    title: '教务中心',
    expanded: true,
    children: [
      { name: '教学组织维护', path: '/admin/org', icon: 'fa-solid fa-sitemap' },
      { name: '课程统管', path: '/admin/course', icon: 'fa-solid fa-book-open' }
    ]
  },
  {
    id: 'resource',
    title: '资源中心',
    expanded: true,
    children: [
      { name: '编排节点管理', path: '/admin/node', icon: 'fa-solid fa-server' },
      { name: '公共模板库', path: '/admin/template', icon: 'fa-solid fa-layer-group' },
      { name: '公共题库大盘', path: '/admin/question', icon: 'fa-solid fa-database' }
    ]
  },
  {
    id: 'sys',
    title: '系统中心',
    expanded: true,
    children: [
      { name: '工单/反馈处理', path: '/admin/ticket', icon: 'fa-solid fa-headset' },
      { name: '服务运行监控', path: '/admin/monitor', icon: 'fa-solid fa-microchip' },
      { name: '操作日志审计', path: '/admin/audit', icon: 'fa-solid fa-history' }
    ]
  }
])

// 切换折叠状态的方法
const toggleGroup = (group: any) => {
  group.expanded = !group.expanded
}
</script>

<style scoped>
.admin-sidebar {
  width: 240px;
  height: calc(100vh - 64px);
  background-color: #fff;
  border-right: 1px solid #E5E7EB;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.sidebar-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
}

.nav-groups {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 控制分类之间的紧凑间距 */
}

/* 分类标题区交互设计 */
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: background-color 0.2s;
  user-select: none; /* 防止双击选中文本 */
}

.group-header:hover {
  background-color: #F9FAFB;
}

.group-title {
  font-size: 12px;
  font-weight: 700;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 折叠箭头动画 */
.toggle-icon {
  font-size: 10px;
  color: #D1D5DB;
  transition: transform 0.2s ease;
}

.toggle-icon.is-collapsed {
  transform: rotate(-90deg); /* 折叠时向左指 */
}

.group-items {
  display: flex;
  flex-direction: column;
  gap: 2px; /* 子菜单项的微小间距，避免留白 */
}

/* 子菜单项样式保持不变 */
.nav-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 12px;
  border-radius: 6px;
  color: #4B5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  gap: 12px;
}

.nav-item i {
  width: 16px;
  text-align: center;
  font-size: 14px;
  color: #9CA3AF;
  transition: color 0.2s;
}

.nav-item:hover {
  background-color: #F3F4F6;
  color: #111827;
}

.nav-item.active {
  background-color: #EEF2FF;
  color: #4F46E5;
}

.nav-item.active i {
  color: #4F46E5;
}

/* 自定义纤细滚动条 */
.sidebar-scroll-area::-webkit-scrollbar {
  width: 4px;
}
.sidebar-scroll-area::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 4px;
}
.sidebar-scroll-area:hover::-webkit-scrollbar-thumb {
  background: #E5E7EB;
}
</style>