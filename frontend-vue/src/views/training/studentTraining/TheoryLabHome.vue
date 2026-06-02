<template>
  <div class="home-root" :class="{ dark: darkMode }">
    <div class="home-bg">
      <div class="home-blob blue"></div>
      <div class="home-blob purple"></div>
    </div>

    <!-- 返回按钮 -->
    <button class="home-back-btn" @click="goBack" title="返回">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5m7-7-7 7 7 7"/></svg>
    </button>

    <!-- 右上角工具栏 -->
    <div class="home-topbar">
      <div class="home-pill">
        <button class="home-pb" @click="toggleTheme" :title="darkMode ? '切换浅色' : '切换深色'">
          <svg v-if="darkMode" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72 1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
        </button>
        <div class="home-psep"></div>
        <button class="home-pb" @click="triggerImport" title="导入课堂">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4m4-5 5 5 5-5m-5 5V3"/></svg>
        </button>
        <input ref="importInput" type="file" accept=".json,.zip" style="display:none" @change="onImport" />
        <div class="home-psep"></div>
        <button class="home-pb" @click="showSettings=true" title="设置">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72 1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
        </button>
      </div>
    </div>

    <!-- Hero -->
    <div class="home-hero" :class="{ centered: !classrooms.length }">
      <div class="home-logo">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#818cf8" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-2z"/><path d="M2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
        <span>AI 学苑 · 理论实训</span>
      </div>
      <p class="home-slogan">AI 智能生成实训课件 · 多智能体协同教学 · 随堂测验与代码实战</p>

      <!-- 输入卡片 -->
      <div class="home-card">
        <div class="home-card-top">
          <div class="home-greeting" @click="showProfile=true">
            <span class="home-avatar">{{ avatar }}</span>
            <span class="home-gname">{{ nickname || '学员' }}
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </span>
          </div>
          <div class="home-agents-bar">
            <span v-for="a in agentIcons" :key="a" class="home-ai">{{ a }}</span>
            <button class="home-agent-add" @click="showSettings=true" title="配置智能体">+</button>
          </div>
        </div>

        <textarea v-model="requirement" class="home-textarea" rows="4"
          placeholder="输入实训需求，例如：数组越界异常的原理与防护实践..."
          @keydown.meta.enter="generate"
          @keydown.ctrl.enter="generate">
        </textarea>

        <div class="home-card-bottom">
          <div class="home-tools">
            <button class="home-tb" :class="{ active: pdfFile }" @click="triggerPdfUpload" :title="pdfFile ? (pdfFile as File).name : '上传PDF教材'">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.5 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>
              <span class="home-tb-label">{{ pdfFile ? (pdfFile as File).name.substring(0, 10) + '...' : 'PDF' }}</span>
            </button>
            <input ref="pdfInput" type="file" accept=".pdf" style="display:none" @change="onPdfChange" />
            <button class="home-tb" :class="{ active: webSearch }" @click="webSearch=!webSearch" title="联网搜索补充资料">🔍 <span class="home-tb-label">搜索</span></button>
            <button class="home-tb interactive" :class="{ active: interactiveMode }" @click="interactiveMode=!interactiveMode" title="开启后支持多轮问答和实时讨论">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              <span class="home-tb-label">互动</span>
            </button>
          </div>
          <div class="home-actions">
            <button class="home-vb" :class="{ recording: isRecording }" @click="toggleRecording" title="语音输入">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="2" width="6" height="12" rx="3"/><path d="M5 10a7 7 0 0014 0"/><line x1="12" y1="19" x2="12" y2="22"/></svg>
            </button>
            <button class="home-send" :disabled="!requirement.trim()" @click="generate">
              <span>进入课堂</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </button>
          </div>
        </div>
      </div>

      <div v-if="error" class="home-error">{{ error }}</div>

      <!-- 历史课堂 -->
      <div class="home-recent" v-if="classrooms.length">
        <div class="home-divider">
          <span></span>
          <div class="home-dt" @click="recentOpen=!recentOpen">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>历史实训 ({{ classrooms.length }})</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :style="{ transform: recentOpen ? 'rotate(180deg)' : '' }"><polyline points="6 9 12 15 18 9"/></svg>
          </div>
          <button class="home-import-link" @click="triggerImport">📂 导入</button>
          <span></span>
        </div>
        <div v-if="recentOpen" class="home-grid">
          <div v-for="r in classrooms" :key="r.id" class="home-card-item" @click="openRoom(r.id)">
            <div class="home-ci-thumb">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2L2 7l10 5 10-5-10-2zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
              <span v-if="r.slides?.length" class="home-ci-count">{{ r.slides.length }}页</span>
              <div class="home-ci-actions">
                <button class="home-cia" @click.stop="startRename(r)">✎</button>
                <button class="home-cia del" @click.stop="pendingDel=r.id">✕</button>
              </div>
              <div v-if="pendingDel===r.id" class="home-ci-overlay" @click.stop>
                <span>确定删除？</span>
                <div><button @click.stop="doDelete(r.id)">确定</button><button @click.stop="pendingDel=null">取消</button></div>
              </div>
              <div v-if="renamingId===r.id" class="home-ci-overlay" @click.stop>
                <input v-model="renameDraft" @keydown.enter="doRename(r.id)" @keydown.escape="renamingId=null" />
              </div>
            </div>
            <div class="home-ci-footer">
              <span class="home-ci-name">{{ r.name }}</span>
              <span class="home-ci-date">{{ r.date }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="home-empty-import" @click="triggerImport">📂 导入已有实训</div>

      <div class="home-footer">AI 学苑 · 智能实训平台</div>
    </div>

    <!-- 设置弹窗 -->
    <teleport to="body">
      <div v-if="showSettings" class="home-overlay" @click.self="showSettings=false">
        <div class="home-dialog wide">
          <div class="home-dh">
            <span>⚙️ 设置</span>
            <button @click="showSettings=false">✕</button>
          </div>
          <div class="home-db settings-layout">
            <div class="settings-sidebar">
              <button v-for="tab in settingsTabs" :key="tab.key" :class="{ active: settingsTab === tab.key }" @click="settingsTab = tab.key">{{ tab.label }}</button>
            </div>
            <div class="settings-content">
              <div v-if="settingsTab === 'llm'" class="settings-section">
                <div class="settings-hdr">
                  <span>LLM 提供商</span>
                  <select v-model="selectedProvider" class="home-sel" @change="addProvider">
                    <option value="">+ 添加</option>
                    <option v-for="(p, k) in providerPresets" :key="k" :value="k">{{ p.name }}</option>
                  </select>
                </div>
                <div v-for="(cfg, pid) in providerConfigs" :key="pid" class="settings-card">
                  <div class="settings-card-hdr"><b>{{ providerPresets[pid as string]?.name || pid }}</b><button class="home-del" @click="removeProvider(pid as string)">✕</button></div>
                  <div class="settings-row"><label>API Key</label><input v-model="(cfg as any).apiKey" type="password" class="home-inp" @input="saveProviders" /></div>
                  <div class="settings-row"><label>Base URL</label><input v-model="(cfg as any).baseUrl" class="home-inp" @input="saveProviders" /></div>
                  <div class="settings-row">
                    <label>模型</label>
                    <div class="settings-models">
                      <span v-for="(m, mi) in (cfg as any).models" :key="mi" class="settings-tag">{{ m }}<button @click="removeModel(pid as string, mi as number)">×</button></span>
                      <input v-model="newModel[pid as string]" class="home-inp-sm" placeholder="+ 添加" @keydown.enter="addModel(pid as string)" />
                    </div>
                  </div>
                  <button class="home-use" :class="{ active: activeProvider === pid }" @click="setActive(pid as string)">设为当前{{ activeProvider === pid ? ' ✓' : '' }}</button>
                </div>
              </div>
              <div v-if="settingsTab === 'general'" class="settings-section">
                <div class="settings-hdr"><span>通用设置</span></div>
                <p class="settings-hint">清除所有本地存储的课堂数据，此操作不可撤销。</p>
                <button class="home-danger" @click="clearAll">清除所有数据</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>

    <!-- 个人资料弹窗 -->
    <teleport to="body">
      <div v-if="showProfile" class="home-overlay" @click.self="showProfile=false">
        <div class="home-dialog">
          <div class="home-dh"><span>👤 个人资料</span><button @click="showProfile=false">✕</button></div>
          <div class="home-db">
            <div class="home-avatar-section">
              <span class="home-big-avatar">{{ avatar }}</span>
              <div class="home-avatar-opts">
                <button v-for="a in avatarOpts" :key="a" :class="{ active: avatar === a }" @click="avatar=a; saveProfile()">{{ a }}</button>
              </div>
            </div>
            <div class="home-sg"><label>昵称</label><input v-model="nickname" @input="saveProfile" /></div>
            <div class="home-sg"><label>简介</label><textarea v-model="bio" rows="2" maxlength="200" @input="saveProfile"></textarea></div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const darkMode = ref(true)

// ─── Form state ───
const requirement = ref(localStorage.getItem('theorylab_requirement') || '')
const webSearch = ref(localStorage.getItem('theorylab_webSearch') === 'true')
const interactiveMode = ref(localStorage.getItem('theorylab_interactive') === 'true')
const pdfFile = ref<File | null>(null)
const error = ref('')

// ─── Voice ───
const isRecording = ref(false)
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []

// ─── Classrooms ───
interface Classroom { id: string; name: string; date: string; slides?: unknown[] }
const classrooms = ref<Classroom[]>([])
const recentOpen = ref(true)
const pendingDel = ref<string | null>(null)
const renamingId = ref<string | null>(null)
const renameDraft = ref('')

// ─── Profile ───
const showProfile = ref(false)
const nickname = ref(localStorage.getItem('theorylab_nickname') || '学员')
const avatar = ref(localStorage.getItem('theorylab_avatar') || '🧑‍🎓')
const bio = ref(localStorage.getItem('theorylab_bio') || '')
const avatarOpts = ['🧑‍🎓','👨‍💻','👩‍🔬','🧑‍🏫','👨‍🚀','🧑‍✈️','👩‍🎨','🧑‍🔧']
const agentIcons = ref(['🎓'])

// ─── Settings ───
const showSettings = ref(false)
const settingsTab = ref('llm')
const settingsTabs = [
  { key: 'llm', label: 'LLM' },
  { key: 'general', label: '通用' },
]
const providerPresets: Record<string, { name: string; baseUrl: string }> = {
  openai: { name: 'OpenAI', baseUrl: 'https://api.openai.com/v1' },
  deepseek: { name: 'DeepSeek', baseUrl: 'https://api.deepseek.com/v1' },
  qwen: { name: '通义千问', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1' },
}
const providerConfigs = ref<Record<string, unknown>>(loadJson('theorylab_providers', {}))
const selectedProvider = ref('')
const newModel = ref<Record<string, string>>({})
const activeProvider = ref(localStorage.getItem('theorylab_activeProvider') || '')
const importInput = ref<HTMLInputElement | null>(null)
const pdfInput = ref<HTMLInputElement | null>(null)

function loadJson(key: string, def: unknown) {
  try { return JSON.parse(localStorage.getItem(key) || 'null') || def } catch { return def }
}
function saveJson(key: string, val: unknown) { localStorage.setItem(key, JSON.stringify(val)) }
function saveProviders() { saveJson('theorylab_providers', providerConfigs.value) }
function saveProfile() {
  localStorage.setItem('theorylab_nickname', nickname.value)
  localStorage.setItem('theorylab_avatar', avatar.value)
  localStorage.setItem('theorylab_bio', bio.value)
}
function goBack() { router.push('/admin/training-test') }
function toggleTheme() {
  darkMode.value = !darkMode.value
  document.documentElement.classList.toggle('dark', darkMode.value)
}
function triggerPdfUpload() { pdfInput.value?.click() }
function onPdfChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (file.size > 50 * 1024 * 1024) { alert('PDF文件大小不能超过50MB'); return }
  pdfFile.value = file
}
async function toggleRecording() {
  if (isRecording.value) { mediaRecorder?.stop(); isRecording.value = false; return }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []
    mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) audioChunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      try {
        const fd = new FormData()
        fd.append('audio', blob, 'recording.webm')
        const res = await fetch('/ai/transcription', { method: 'POST', body: fd })
        const data = await res.json()
        if (data.text) requirement.value += (requirement.value ? ' ' : '') + data.text
      } catch { /* ignore */ }
      stream.getTracks().forEach(t => t.stop())
    }
    mediaRecorder.start()
    isRecording.value = true
  } catch { /* ignore */ }
}
function addProvider() {
  const key = selectedProvider.value
  if (!key || (providerConfigs.value as Record<string, unknown>)[key]) { selectedProvider.value = ''; return }
  ;(providerConfigs.value as Record<string, unknown>)[key] = { apiKey: '', baseUrl: providerPresets[key]?.baseUrl || '', models: [] }
  saveProviders(); selectedProvider.value = ''
}
function removeProvider(pid: string) { delete (providerConfigs.value as Record<string, unknown>)[pid]; saveProviders() }
function addModel(pid: string) {
  const m = newModel.value[pid]
  if (!m?.trim()) return
  const cfg = (providerConfigs.value as Record<string, { models: string[] }>)[pid]
  if (!cfg.models) cfg.models = []
  cfg.models.push(m.trim()); newModel.value[pid] = ''; saveProviders()
}
function removeModel(pid: string, mi: number) {
  const cfg = (providerConfigs.value as Record<string, { models: string[] }>)[pid]
  cfg.models.splice(mi, 1); saveProviders()
}
function setActive(pid: string) { activeProvider.value = pid; localStorage.setItem('theorylab_activeProvider', pid) }
function loadRooms() {
  const idx: string[] = JSON.parse(localStorage.getItem('cl_index') || '[]')
  classrooms.value = idx.map(id => {
    try { return { id, ...JSON.parse(localStorage.getItem(id) || 'null') } } catch { return null }
  }).filter(Boolean) as Classroom[]
}
function doDelete(id: string) {
  localStorage.removeItem(id)
  const idx: string[] = JSON.parse(localStorage.getItem('cl_index') || '[]').filter((x: string) => x !== id)
  localStorage.setItem('cl_index', JSON.stringify(idx)); pendingDel.value = null; loadRooms()
}
function startRename(r: Classroom) { renamingId.value = r.id; renameDraft.value = r.name }
function doRename(id: string) {
  const data = JSON.parse(localStorage.getItem(id) || 'null')
  if (data && renameDraft.value.trim()) { data.name = renameDraft.value.trim(); localStorage.setItem(id, JSON.stringify(data)) }
  renamingId.value = null; loadRooms()
}
function openRoom(id: string) {
  try {
    const data = JSON.parse(localStorage.getItem(id) || 'null')
    if (data?.slides?.length) {
      sessionStorage.setItem('th_slides', JSON.stringify(data.slides))
      sessionStorage.setItem('th_agents', JSON.stringify(data.agents || []))
      sessionStorage.setItem('th_title', data.name)
      alert('课堂数据已加载，请在实训执行页面查看。')
    }
  } catch { /* ignore */ }
}
function triggerImport() { importInput.value?.click() }
function onImport(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const data = JSON.parse(ev.target?.result as string)
      const sl = data.slides || data.outlines || []
      if (sl.length) {
        const id = 'cl_' + Date.now()
        localStorage.setItem(id, JSON.stringify({ name: data.title || '导入课堂', slides: sl, agents: data.agents || [], date: new Date().toLocaleDateString() }))
        const idx: string[] = JSON.parse(localStorage.getItem('cl_index') || '[]')
        idx.unshift(id); localStorage.setItem('cl_index', JSON.stringify(idx)); loadRooms()
      }
    } catch { /* ignore */ }
  }
  reader.readAsText(file);
  (e.target as HTMLInputElement).value = ''
}
function generate() {
  if (!requirement.value.trim()) return
  localStorage.setItem('theorylab_requirement', requirement.value)
  localStorage.setItem('theorylab_webSearch', String(webSearch.value))
  localStorage.setItem('theorylab_interactive', String(interactiveMode.value))
  const session = {
    sessionId: Date.now().toString(36),
    requirements: { requirement: requirement.value, webSearch: webSearch.value, interactiveMode: interactiveMode.value },
    pdfFile: pdfFile.value ? { name: pdfFile.value.name, size: pdfFile.value.size } : null,
  }
  sessionStorage.setItem('genSession', JSON.stringify(session))
  alert('课堂生成请求已提交！（生成预览页面待接入）')
}
function clearAll() {
  if (!confirm('确定清除所有数据？')) return
  const idx: string[] = JSON.parse(localStorage.getItem('cl_index') || '[]')
  idx.forEach(id => localStorage.removeItem(id))
  localStorage.removeItem('cl_index')
  classrooms.value = []
  alert('所有数据已清除')
}
onMounted(() => { loadRooms(); document.documentElement.classList.toggle('dark', darkMode.value) })
</script>

<style scoped>
.home-back-btn{position:fixed;top:16px;left:16px;z-index:51;width:36px;height:36px;border-radius:10px;border:1px solid #e2e8f0;background:rgba(255,255,255,.7);backdrop-filter:blur(12px);color:#64748b;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:.12s}
.dark .home-back-btn{background:rgba(30,41,59,.6);border-color:#334155;color:#94a3b8}
.home-back-btn:hover{background:#fff;color:#475569}
.dark .home-back-btn:hover{background:#1e293b;color:#cbd5e1}
.home-root{min-height:100vh;width:100%;background:linear-gradient(180deg,#f8fafc,#f1f5f9);color:#1e293b;display:flex;flex-direction:column;align-items:center;padding:16px 16px 0;position:relative;overflow-x:hidden}
.home-root.dark{background:linear-gradient(180deg,#0f172a,#020617);color:#e2e8f0}
.home-bg{position:absolute;inset:0;overflow:hidden;pointer-events:none}
.home-blob{position:absolute;width:384px;height:384px;border-radius:50%;filter:blur(80px);opacity:.06;animation:pulse 4s ease-in-out infinite}
.home-blob.blue{top:0;left:25%;background:#6366f1}
.home-blob.purple{bottom:0;right:25%;background:#a855f7;animation-delay:2s}
@keyframes pulse{0%,100%{opacity:.06}50%{opacity:.1}}
.home-topbar{position:fixed;top:16px;right:16px;z-index:50}
.home-pill{display:flex;align-items:center;gap:2px;padding:6px 8px;background:rgba(255,255,255,0.6);backdrop-filter:blur(12px);border-radius:999px;border:1px solid rgba(255,255,255,0.5)}
.dark .home-pill{background:rgba(30,41,59,0.6);border-color:#334155}
.home-pb{width:28px;height:28px;border-radius:999px;border:none;background:transparent;color:#94a3b8;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:.12s}
.home-pb:hover{background:#fff;color:#475569}
.dark .home-pb:hover{background:#1e293b;color:#cbd5e1}
.home-psep{width:1px;height:14px;background:#e2e8f0}
.dark .home-psep{background:#334155}
.home-hero{position:relative;z-index:10;width:100%;max-width:800px;display:flex;flex-direction:column;align-items:center;flex:1}
.home-hero.centered{justify-content:center;min-height:calc(100vh - 80px)}
.home-logo{display:flex;align-items:center;gap:8px;margin-bottom:2px;margin-top:6vh}
.home-hero.centered .home-logo{margin-top:0}
.home-logo span{font-size:20px;font-weight:700;background:linear-gradient(135deg,#818cf8,#6366f1);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
.home-slogan{font-size:13px;color:#94a3b8;margin-bottom:28px;text-align:center}
.home-card{width:100%;background:rgba(255,255,255,0.88);backdrop-filter:blur(16px);border-radius:16px;border:1px solid rgba(255,255,255,0.6);box-shadow:0 4px 24px rgba(0,0,0,0.04)}
.dark .home-card{background:rgba(15,23,42,0.88);border-color:rgba(30,41,59,0.6)}
.home-card-top{display:flex;justify-content:space-between;align-items:flex-start;padding:14px 16px 4px}
.home-greeting{display:flex;align-items:center;gap:8px;cursor:pointer;padding:4px 10px 4px 6px;border-radius:999px;border:1px solid rgba(0,0,0,0.04);user-select:none;transition:.12s}
.home-greeting:hover{background:rgba(0,0,0,0.02)}
.dark .home-greeting{border-color:rgba(255,255,255,0.04)}
.home-avatar{font-size:18px}
.home-gname{display:flex;align-items:center;gap:4px;font-size:13px;font-weight:600;color:#475569}
.dark .home-gname{color:#94a3b8}
.home-agents-bar{display:flex;gap:2px;padding:2px 4px;align-items:center}
.home-ai{font-size:16px;opacity:.7}
.home-agent-add{width:18px;height:18px;border-radius:50%;border:1px dashed #94a3b8;background:transparent;color:#94a3b8;font-size:11px;cursor:pointer;display:flex;align-items:center;justify-content:center;padding:0}
.home-agent-add:hover{border-color:#818cf8;color:#818cf8}
.home-textarea{width:100%;border:none;background:transparent;padding:6px 16px 10px;font-size:13px;line-height:1.7;color:#1e293b;resize:none;outline:none;font-family:inherit;min-height:96px}
.dark .home-textarea{color:#e2e8f0}
.home-textarea::placeholder{color:#94a3b8}
.home-card-bottom{display:flex;align-items:center;justify-content:space-between;padding:6px 12px 12px;gap:8px;flex-wrap:wrap}
.home-tools{display:flex;gap:4px;flex-wrap:wrap}
.home-tb{display:flex;align-items:center;gap:4px;height:30px;padding:0 10px;border-radius:8px;border:1px solid #e2e8f0;background:transparent;color:#64748b;font-size:11px;font-weight:500;cursor:pointer;transition:.12s}
.dark .home-tb{border-color:#334155;color:#94a3b8}
.home-tb:hover{background:#f8fafc;color:#475569}
.dark .home-tb:hover{background:#1e293b;color:#cbd5e1}
.home-tb.active{border-color:#818cf8;color:#818cf8;background:rgba(99,102,241,0.04)}
.home-tb.interactive.active{border-color:#06b6d4;color:#06b6d4;background:rgba(6,182,212,0.04)}
.home-actions{display:flex;gap:6px;align-items:center}
.home-vb{width:30px;height:30px;border-radius:8px;border:1px solid #e2e8f0;background:transparent;color:#64748b;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:.12s}
.dark .home-vb{border-color:#334155;color:#94a3b8}
.home-vb:hover{background:#f8fafc;color:#475569}
.home-vb.recording{background:rgba(239,68,68,0.1);border-color:#ef4444;color:#ef4444;animation:pulse 1.5s infinite}
.home-send{display:flex;align-items:center;gap:6px;height:32px;padding:0 16px;border-radius:8px;border:none;background:#818cf8;color:#fff;font-size:12px;font-weight:600;cursor:pointer;transition:.12s}
.home-send:hover{background:#6366f1}
.home-send:disabled{opacity:.4;cursor:not-allowed}
.home-error{margin-top:8px;padding:8px 12px;background:rgba(239,68,68,0.06);border:1px solid rgba(239,68,68,0.15);border-radius:8px;color:#ef4444;font-size:12px;width:100%}
.home-recent{width:100%;max-width:900px;margin-top:36px}
.home-divider{display:flex;align-items:center;gap:12px;width:100%;padding:6px 0}
.home-divider span{flex:1;height:1px;background:#e2e8f0}
.dark .home-divider span{background:#334155}
.home-dt{display:flex;align-items:center;gap:6px;font-size:13px;color:#64748b;cursor:pointer;user-select:none;white-space:nowrap}
.home-import-link{font-size:12px;color:#94a3b8;cursor:pointer;background:none;border:none;padding:4px 8px;border-radius:4px}
.home-import-link:hover{color:#64748b}
.home-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px;padding-top:20px}
.home-card-item{cursor:pointer;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0;background:#fff;transition:.2s}
.dark .home-card-item{border-color:#334155;background:#1e293b}
.home-card-item:hover{transform:translateY(-3px);box-shadow:0 8px 24px rgba(0,0,0,0.06)}
.home-ci-thumb{position:relative;aspect-ratio:16/9;background:#f8fafc;display:flex;align-items:center;justify-content:center;color:#d1d5db;overflow:hidden}
.dark .home-ci-thumb{background:#0f172a;color:#334155}
.home-ci-count{position:absolute;bottom:4px;right:4px;font-size:9px;padding:1px 5px;border-radius:3px;background:rgba(99,102,241,0.15);color:#818cf8}
.home-ci-actions{position:absolute;top:4px;right:4px;display:flex;gap:2px;opacity:0;transition:.15s;z-index:2}
.home-card-item:hover .home-ci-actions{opacity:1}
.home-cia{width:22px;height:22px;border-radius:5px;border:none;background:rgba(255,255,255,0.9);color:#64748b;font-size:11px;cursor:pointer;display:flex;align-items:center;justify-content:center}
.dark .home-cia{background:rgba(15,23,42,0.9);color:#94a3b8}
.home-cia.del:hover{color:#ef4444}
.home-ci-overlay{position:absolute;inset:0;background:rgba(0,0,0,0.5);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;font-size:11px;color:#fff;z-index:5}
.home-ci-overlay div{display:flex;gap:4px}
.home-ci-overlay button{padding:3px 10px;border-radius:4px;border:none;font-size:10px;cursor:pointer}
.home-ci-overlay button:first-child{background:#ef4444;color:#fff}
.home-ci-overlay button:last-child{background:rgba(255,255,255,0.9);color:#475569}
.home-ci-overlay input{width:80%;padding:4px 8px;border-radius:4px;border:none;font-size:11px}
.home-ci-footer{padding:8px 10px;display:flex;justify-content:space-between;align-items:center;gap:4px}
.home-ci-name{font-size:12px;font-weight:600;color:#475569;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:70%}
.dark .home-ci-name{color:#cbd5e1}
.home-ci-date{font-size:10px;color:#94a3b8;white-space:nowrap}
.home-empty-import{margin-top:20px;font-size:12px;color:#94a3b8;cursor:pointer;padding:4px 12px;border-radius:8px}
.home-empty-import:hover{color:#64748b}
.home-footer{margin-top:auto;padding:24px 0 16px;font-size:11px;color:#94a3b8;text-align:center}
.home-overlay{position:fixed;inset:0;background:rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center;z-index:1000;backdrop-filter:blur(2px)}
.home-dialog{background:#fff;border-radius:16px;width:420px;max-width:90vw;max-height:80vh;overflow:hidden;box-shadow:0 24px 80px rgba(0,0,0,0.1)}
.dark .home-dialog{background:#1e293b}
.home-dialog.wide{width:680px}
.home-dh{display:flex;justify-content:space-between;align-items:center;padding:16px 20px;border-bottom:1px solid #e2e8f0;font-size:15px;font-weight:600}
.dark .home-dh{border-color:#334155}
.home-dh button{background:none;border:none;color:#94a3b8;cursor:pointer;font-size:16px;padding:4px}
.home-dh button:hover{color:#475569}
.home-db{padding:20px;display:flex;flex-direction:column;gap:14px}
.settings-layout{flex-direction:row;gap:0;padding:0}
.settings-sidebar{width:120px;border-right:1px solid #e2e8f0;padding:12px 0;flex-shrink:0}
.dark .settings-sidebar{border-color:#334155}
.settings-sidebar button{display:block;width:100%;padding:8px 14px;border:none;background:transparent;color:#64748b;font-size:12px;text-align:left;cursor:pointer;transition:.1s}
.settings-sidebar button:hover{background:#f8fafc;color:#475569}
.dark .settings-sidebar button:hover{background:rgba(255,255,255,0.02);color:#cbd5e1}
.settings-sidebar button.active{color:#818cf8;background:rgba(99,102,241,0.04);font-weight:600}
.settings-content{flex:1;padding:16px 20px;overflow-y:auto;max-height:50vh}
.settings-section{display:flex;flex-direction:column;gap:12px}
.settings-hdr{display:flex;justify-content:space-between;align-items:center;font-size:13px;font-weight:600}
.settings-card{padding:12px;border:1px solid #e2e8f0;border-radius:10px;background:#f8fafc}
.dark .settings-card{border-color:#334155;background:#0f172a}
.settings-card-hdr{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;font-size:13px}
.settings-row{display:flex;flex-direction:column;gap:3px;margin-bottom:8px}
.settings-row label{font-size:10px;font-weight:600;color:#64748b;text-transform:uppercase;letter-spacing:.5px}
.dark .settings-row label{color:#94a3b8}
.settings-models{display:flex;flex-wrap:wrap;gap:4px;align-items:center}
.settings-tag{display:inline-flex;align-items:center;gap:3px;background:#e2e8f0;padding:2px 8px;border-radius:999px;font-size:10px;color:#475569}
.dark .settings-tag{background:#334155;color:#cbd5e1}
.settings-tag button{background:none;border:none;color:#94a3b8;cursor:pointer;padding:0;font-size:12px;line-height:1}
.settings-tag button:hover{color:#ef4444}
.settings-hint{font-size:11px;color:#94a3b8}
.home-inp{width:100%;padding:6px 10px;border-radius:6px;border:1px solid #e2e8f0;font-size:12px;outline:none;background:#fff;color:#1e293b;box-sizing:border-box}
.dark .home-inp{background:#0f172a;border-color:#334155;color:#e2e8f0}
.home-inp-sm{padding:4px 8px;border-radius:999px;border:1px solid #e2e8f0;font-size:11px;outline:none;background:transparent;color:#475569;width:80px}
.dark .home-inp-sm{border-color:#334155;color:#e2e8f0}
.home-sel{padding:5px 8px;border-radius:6px;border:1px solid #e2e8f0;font-size:11px;outline:none;background:#fff;color:#475569}
.dark .home-sel{background:#0f172a;border-color:#334155;color:#cbd5e1}
.home-del{background:none;border:none;color:#94a3b8;cursor:pointer;font-size:13px;padding:2px}
.home-del:hover{color:#ef4444}
.home-use{margin-top:4px;padding:3px 12px;border-radius:6px;border:1px solid #e2e8f0;background:transparent;color:#64748b;font-size:10px;cursor:pointer}
.home-use.active{border-color:#818cf8;color:#818cf8;background:rgba(99,102,241,0.04)}
.home-danger{padding:8px 16px;border-radius:8px;border:none;background:rgba(239,68,68,0.1);color:#ef4444;font-size:12px;cursor:pointer}
.home-danger:hover{background:rgba(239,68,68,0.2)}
.home-sg{display:flex;flex-direction:column;gap:4px}
.home-sg label{font-size:12px;font-weight:600;color:#64748b}
.dark .home-sg label{color:#94a3b8}
.home-sg input,.home-sg textarea{padding:8px 10px;border-radius:8px;border:1px solid #e2e8f0;font-size:13px;outline:none;background:#f8fafc;color:#1e293b}
.dark .home-sg input,.dark .home-sg textarea{background:#0f172a;border-color:#334155;color:#e2e8f0}
.home-avatar-section{display:flex;align-items:center;gap:16px}
.home-big-avatar{width:48px;height:48px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:24px;background:rgba(99,102,241,0.06)}
.home-avatar-opts{display:flex;flex-wrap:wrap;gap:4px}
.home-avatar-opts button{width:32px;height:32px;border-radius:50%;border:1px solid #e2e8f0;background:transparent;font-size:16px;cursor:pointer;transition:.1s}
.dark .home-avatar-opts button{border-color:#334155}
.home-avatar-opts button:hover{border-color:#94a3b8}
.home-avatar-opts button.active{border-color:#818cf8;background:rgba(99,102,241,0.06)}
</style>
