<template>
  <div class="cabin-wrapper">
    <aside class="ai-assistant">
      <header class="ai-header">🤖 理论指导伙伴 (LangChain 驱动)</header>
      <div class="chat-flow">
        <div class="msg ai">你好，我是你的实训助手。在进行无人机抗重放代码实操前，你需要先配置好 SM4 密钥，有什么疑问可以问我。</div>
        <div class="msg user">如何生成符合国密标准的 128 位密钥？</div>
        <div class="msg ai thinking">AI 正在调取密码学知识库...</div>
      </div>
      <div class="input-area">
        <input type="text" placeholder="向 AI 导师提问..." />
        <button>发送</button>
      </div>
    </aside>

    <main class="ide-container">
      <div class="ide-header">
        <span class="file-name">main_crypto.py</span>
        <button class="run-btn">编译并验证</button>
      </div>
      <div class="code-editor">
        <pre><code>
import sm_crypto

# 任务：在此处实现抗重放攻击的时间戳校验逻辑
def verify_timestamp(ts):
    current_time = get_now()
    # TODO: 编写校验代码
    pass
        </code></pre>
      </div>
      <div class="terminal">
        > [系统] 环境已就绪，等待代码提交...
      </div>
    </main>
  </div>
</template>

<style scoped>
.cabin-wrapper { display: flex; height: 100vh; background: #0F172A; }
.ai-assistant { width: 400px; background: #FFFFFF; display: flex; flex-direction: column; border-right: 1px solid #E2E8F0; }
.ai-header { padding: 20px; font-weight: 800; border-bottom: 1px solid #F1F5F9; color: #1E293B; }
.chat-flow { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.msg { padding: 12px; border-radius: 12px; max-width: 85%; font-size: 14px; line-height: 1.6; }
.msg.ai { background: #F1F5F9; color: #334155; }
.msg.user { background: #4F46E5; color: white; align-self: flex-end; }
.msg.thinking { color: #94A3B8; font-style: italic; }
.input-area { padding: 20px; border-top: 1px solid #F1F5F9; display: flex; gap: 10px; }
.input-area input { flex: 1; padding: 10px; border: 1px solid #E2E8F0; border-radius: 8px; }

.ide-container { flex: 1; display: flex; flex-direction: column; color: #E2E8F0; }
.ide-header { padding: 12px 24px; background: #1E293B; display: flex; justify-content: space-between; align-items: center; }
.run-btn { background: #10B981; color: white; border: none; padding: 6px 16px; border-radius: 6px; font-weight: 600; cursor: pointer; }
.code-editor { flex: 1; padding: 24px; font-family: 'Courier New', monospace; background: #0F172A; }
.terminal { height: 120px; background: #020617; padding: 16px; border-top: 1px solid #334155; font-family: monospace; color: #10B981; }
</style>