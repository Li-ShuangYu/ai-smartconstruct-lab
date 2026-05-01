<template>
  <div class="page-container custom-scrollbar">
    <div class="layout-grid-2x2">
      
      <div 
        v-for="(group, gIndex) in groups" 
        :key="gIndex"
        class="glass-card flex-col border-t-4"
        :style="{ borderTopColor: themes[gIndex].color }"
      >
        <div class="card-header flex-between border-b" :style="{ backgroundColor: themes[gIndex].color + '1a', borderBottomColor: themes[gIndex].color + '33' }">
          <h2 class="card-title flex-row gap-2" :style="{ color: themes[gIndex].color }">
            <span class="text-2xl drop-shadow-sm">{{ themes[gIndex].icon }}</span> 组 {{ gIndex + 1 }}
          </h2>
          <span class="badge" :style="{ backgroundColor: themes[gIndex].color + '20', color: themes[gIndex].color, borderColor: themes[gIndex].color + '40' }">
            {{ group.length }} 人
          </span>
        </div>

        <div class="card-body flex-row gap-4">
          <div 
            v-for="student in group" 
            :key="student.name"
            @click="goToStudent(student)"
            class="student-card flex-col items-center justify-center gap-3 transition-transform"
            :style="{ borderColor: themes[gIndex].color + '33', backgroundColor: 'white' }"
            @mouseenter="hoverCard = student.name"
            @mouseleave="hoverCard = null"
          >
            <div 
              class="avatar-circle"
              :style="{ backgroundColor: themes[gIndex].color + '20', color: themes[gIndex].color }"
            >
              {{ student.name.charAt(0) }}
            </div>
            
            <span class="student-name text-slate-800">
              {{ student.name }}
            </span>
            
            <div class="student-score font-mono" :style="{ color: themes[gIndex].color }">
              {{ student.score }}
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件19一致]
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const hoverCard = ref(null);

const goToStudent = (student) => {
  localStorage.setItem('currentStudentId', student.id.toString());
  router.push({ path: '/student-archive/competency/101' });
};

const allStudents = [
  { id: 101, name: '林浩轩', score: 95 }, { id: 102, name: '李若曦', score: 94 }, { id: 103, name: '刘承宇', score: 92 }, { id: 104, name: '黄雨桐', score: 90 },
  { id: 105, name: '陈宇泽', score: 98 }, { id: 106, name: '许诗涵', score: 95 }, { id: 107, name: '赵景辰', score: 94 }, { id: 108, name: '张语桐', score: 92 },
  { id: 109, name: '王嘉树', score: 97 }, { id: 110, name: '苏欣妍', score: 96 }, { id: 111, name: '周子恒', score: 94 }, { id: 112, name: '邓佳宁', score: 92 }
];

const groups = ref([[], [], [], []]);

const themes = [ { icon: '🔷', color: '#3b82f6' }, { icon: '🔴', color: '#ef4444' }, { icon: '🟡', color: '#f59e0b' }, { icon: '🟪', color: '#8b5cf6' } ];

onMounted(() => {
  localStorage.removeItem('currentStudentId');
  groups.value = [
    [allStudents[0], allStudents[1], allStudents[2]], [allStudents[3], allStudents[4], allStudents[5]],
    [allStudents[6], allStudents[7], allStudents[8]], [allStudents[9], allStudents[10], allStudents[11]]
  ];
});
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow-y: auto; font-family: sans-serif; padding: 24px; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 24px; min-height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); display: flex; flex-direction: column; overflow: hidden; }
.border-t-4 { border-top-width: 4px; border-top-style: solid; }
.flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 8px; } .gap-4 { gap: 16px; } .gap-3 { gap: 12px; }

.card-header { padding: 16px 24px; border-bottom-width: 1px; border-bottom-style: solid; }
.card-title { font-size: 20px; font-weight: 900; margin: 0; letter-spacing: 1px; }
.drop-shadow-sm { filter: drop-shadow(0 1px 1px rgba(0,0,0,0.1)); }
.badge { font-size: 13px; font-weight: bold; padding: 4px 12px; border-radius: 20px; border: 1px solid; }

.card-body { padding: 24px; flex: 1; }
.student-card { flex: 1; border: 1px solid; border-radius: 16px; padding: 20px; cursor: pointer; transition: all 0.3s; box-shadow: 0 2px 4px rgba(0,0,0,0.02); display: flex; align-items: center; justify-content: center; }
.student-card:hover { transform: translateY(-4px); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border-color: #cbd5e1 !important; }

.avatar-circle { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 900; transition: transform 0.3s; }
.student-card:hover .avatar-circle { transform: scale(1.1); }
.student-name { font-size: 16px; font-weight: bold; letter-spacing: 1px; margin-top: 4px; }
.student-score { font-size: 24px; font-weight: 900; }
.text-slate-800 { color: #1e293b; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
</style>