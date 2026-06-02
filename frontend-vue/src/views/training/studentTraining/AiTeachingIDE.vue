<template>
  <div class="ide-root">
    <!-- ── 启动弹窗 ── -->
    <div v-if="showStartModal" class="modal-overlay" @click.self="showStartModal = false">
      <div class="modal-box">
        <div class="modal-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#6366f1" stroke-width="2"><path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/></svg>
        </div>
        <h2>AI 编程实训助手</h2>
        <p>你想编写什么类型的代码？请告诉我你的需求：</p>
        <textarea v-model="userRequest" placeholder="例如：我想学习如何实现一个排序算法、写一个简单的计算器..." />
        <div class="modal-lang-row">
          <span class="modal-lang-label">运行环境：</span>
          <div class="lang-pills">
            <button v-for="l in langOptions" :key="l.value"
              class="lang-pill" :class="{ active: startLang === l.value }"
              @click="startLang = l.value">
              {{ l.label }}
            </button>
          </div>
        </div>
        <div class="modal-btns">
          <button class="btn-cancel" @click="router.push('/admin/training-test')">返回</button>
          <button class="btn-confirm" :disabled="!userRequest.trim()" @click="startSession">
            开始实训
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- ── 顶部栏 ── -->
    <header class="hdr">
      <div class="hdr-left">
        <button class="hdr-back" @click="router.push('/admin/training-test')" title="返回">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5m7-7-7 7 7 7"/></svg>
        </button>
        <div class="hdr-brand">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="#6366f1"><path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/></svg>
          <span class="hdr-title">AI 编程助手</span>
        </div>
        <div class="hdr-lang-badge" v-if="sessionStarted">
          <span class="lang-dot" :style="{ background: langColor }"></span>
          {{ langLabel }}
        </div>
      </div>
      <div class="hdr-center">
        <button class="hdr-btn run" @click="runCode" :disabled="running || !sessionStarted">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          {{ running ? '运行中…' : '运行' }}
        </button>
        <button class="hdr-btn" @click="clearCode" :disabled="!sessionStarted">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
          清空
        </button>
        <button class="hdr-btn" @click="resetSession">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
          重新开始
        </button>
      </div>
      <div class="hdr-right">
        <div class="ai-status-pill" :class="aiStatus">
          <span class="status-dot"></span>
          {{ aiStatus === 'online' ? 'AI 在线' : aiStatus === 'offline' ? 'AI 离线' : '检测中…' }}
        </div>
        <button class="hdr-btn" :class="{ active: showAiPanel }" @click="showAiPanel = !showAiPanel">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          AI 辅导
        </button>
      </div>
    </header>

    <!-- ── 主体 ── -->
    <div class="body" ref="bodyRef">
      <!-- 左侧：编辑器 + 终端 -->
      <div class="editor-pane" :style="{ width: showAiPanel ? leftPct + '%' : '100%' }">
        <!-- 编辑器头部 -->
        <div class="code-header">
          <div class="code-header-left">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#6366f1" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            <span class="code-title">代码编辑器</span>
          </div>
          <div class="code-header-right">
            <button class="code-action-btn" @click="copyCode" title="复制代码">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
            </button>
          </div>
        </div>

        <!-- CodeMirror 编辑器 -->
        <div class="code-area" ref="editorContainer"></div>

        <!-- Diff 对比面板 -->
        <div class="diff-panel" :class="{ visible: diffVisible }">
          <div class="diff-header">
            <span class="diff-title">AI 建议的修改</span>
            <div class="diff-actions">
              <button class="diff-btn revert" @click="revertDiff">撤销</button>
              <button class="diff-btn apply" @click="acceptDiff">应用</button>
            </div>
          </div>
          <div class="diff-body">
            <div class="diff-col">
              <div class="diff-col-hdr">改前</div>
              <pre class="diff-pre">{{ diffOrig }}</pre>
            </div>
            <div class="diff-col">
              <div class="diff-col-hdr diff-col-hdr--new">改后</div>
              <pre class="diff-pre diff-pre--new">{{ diffMod }}</pre>
            </div>
          </div>
        </div>

        <!-- 终端 -->
        <div class="terminal-panel">
          <div class="terminal-header">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="#6366f1" stroke-width="2"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>
            <span class="terminal-title">终端输出</span>
            <div class="terminal-actions">
              <span v-if="lastExitCode !== null" class="exit-badge" :class="lastExitCode === 0 ? 'ok' : 'err'">
                退出码 {{ lastExitCode }}
              </span>
              <button class="term-btn" @click="clearTerminal">清空</button>
            </div>
          </div>
          <div class="terminal-body" ref="termRef">
            <div v-for="(l, i) in terminalLines" :key="i" class="term-line" :class="l.type">
              <span class="term-prefix" v-if="l.type === 'info'">ℹ</span>
              <span class="term-prefix err-prefix" v-else-if="l.type === 'error'">✕</span>
              <span class="term-text">{{ l.text }}</span>
              <button v-if="l.text.trim() && l.type !== 'info'" class="term-add-btn"
                @click="addTerminalToAI(l.text)" title="发给 AI">↗</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分割条 -->
      <div class="resize-bar" v-if="showAiPanel" @mousedown="resizeStart"></div>

      <!-- 右侧：AI 面板 -->
      <div class="ai-pane" :style="{ width: (100 - leftPct) + '%' }" v-show="showAiPanel">
        <div class="ai-header">
          <span class="ai-header-title">AI 编程导师</span>
          <button class="ai-clear-btn" @click="clearChat" title="清空对话">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/></svg>
          </button>
        </div>

        <div class="ai-messages" ref="aiMsgRef">
          <div v-if="!messages.length" class="ai-empty">
            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#c7d2fe" stroke-width="1.5"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            <p>有编程问题？AI 导师帮你思考</p>
            <div class="ai-chips">
              <button class="ai-chip" @click="aiAsk('解释当前代码的原理')">解释代码</button>
              <button class="ai-chip" @click="aiAsk('帮我优化这段代码')">优化代码</button>
              <button class="ai-chip" @click="aiAsk('检查代码中的问题')">检查问题</button>
              <button class="ai-chip" @click="aiAsk('这段代码如何测试？')">测试建议</button>
            </div>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="ai-msg" :class="m.role">
            <div class="ai-av">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="ai-bd">
              <div class="ai-txt" v-html="renderMd(m.text)"></div>
              <div v-for="(b, bi) in m.blocks" :key="bi" class="ai-code-block">
                <div class="ai-code-block-hdr">
                  <span class="ai-code-lang">{{ b.lang || 'code' }}</span>
                  <div class="ai-code-actions">
                    <button class="ai-code-btn" @click="copyText(b.code)">复制</button>
                    <button class="ai-code-btn primary" @click="applyAICode(b.code)">应用到编辑器</button>
                  </div>
                </div>
                <pre class="ai-code-pre"><code>{{ b.code }}</code></pre>
              </div>
              <span v-if="m.streaming" class="ai-cursor">▊</span>
            </div>
          </div>

          <div v-if="aiStreaming && messages.length && messages[messages.length-1].role === 'assistant' && !messages[messages.length-1].text" class="ai-msg assistant">
            <div class="ai-av">🤖</div>
            <div class="ai-bd"><div class="ai-thinking"><span></span><span></span><span></span></div></div>
          </div>
        </div>

        <div class="ai-input-area">
          <div class="ai-input-row" :class="{ focused: aiInputFocused }">
            <textarea ref="aiInputRef" v-model="aiInput" class="ai-input"
              placeholder="输入问题，或选中代码后点击 ↗ 发给 AI…"
              rows="3" @keydown="aiKeydown"
              @focus="aiInputFocused = true" @blur="aiInputFocused = false" />
            <button class="ai-send-btn" :disabled="!aiInput.trim() || aiStreaming" @click="aiAsk()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送 · Shift+Enter 换行</div>
        </div>
      </div>
    </div>
  </div>
</template>
