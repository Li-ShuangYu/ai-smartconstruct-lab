<template>
  <div class="result-wrapper">
    <header class="result-header">
      <h1 class="panel-main-title">实训成效评估报告：胡凯亮</h1>
      <div class="score-badge">综合评分: 92</div>
    </header>

    <div class="result-grid">
      <section class="result-card">
        <h3 class="card-title">五维能力画像</h3>
        <div class="radar-placeholder">
          <div class="mock-radar">
            <div class="radar-line" v-for="i in 5" :key="i" :style="{ transform: `rotate(${i * 72}deg)` }"></div>
            <div class="radar-area"></div>
          </div>
          <div class="radar-labels">
            <span>理论基础</span><span>算法选型</span><span>代码实操</span><span>AI协作</span><span>防御意识</span>
          </div>
        </div>
      </section>

      <section class="result-card">
        <h3 class="card-title">AI 智能辅助诊断</h3>
        <div class="ai-report">
          <p><strong>优势：</strong> 学员在“SM4 算法对称加密”环节表现极其出色，代码逻辑紧凑，一次性通过了抗重放攻击测试。</p>
          <p><strong>待改进：</strong> 在“后量子密码理论”问答环节，多次向 AI 助手发起基础性重复提问，建议加强前置理论课的预习。</p>
          <div class="ai-footer">—— 由 AI学苑-辅助导师 自动生成</div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.result-wrapper { padding: 32px; background: #F1F5F9; min-height: 100vh; font-size: 15px; box-sizing: border-box; }
.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
.score-badge { background: #4F46E5; color: white; padding: 8px 24px; border-radius: 30px; font-weight: 800; font-size: 20px; }
.result-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.result-card { background: white; padding: 24px; border-radius: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }
.card-title { font-size: 18px; margin-bottom: 24px; font-weight: 800; border-left: 4px solid #4F46E5; padding-left: 12px; }
.mock-radar { width: 200px; height: 200px; border: 1px solid #E2E8F0; border-radius: 50%; margin: 40px auto; position: relative; }
.radar-line { position: absolute; width: 100px; height: 1px; background: #E2E8F0; left: 100px; top: 100px; transform-origin: left; }
.radar-area { position: absolute; width: 120px; height: 120px; background: rgba(79, 70, 229, 0.2); border: 2px solid #4F46E5; left: 40px; top: 40px; clip-path: polygon(50% 0%, 100% 38%, 81% 91%, 19% 91%, 0% 38%); }
.radar-labels { display: flex; justify-content: space-between; font-size: 13px; font-weight: 600; color: #64748B; }
.ai-report { line-height: 1.8; color: #334155; }
.ai-footer { margin-top: 24px; font-style: italic; color: #94A3B8; font-size: 13px; }
</style>
