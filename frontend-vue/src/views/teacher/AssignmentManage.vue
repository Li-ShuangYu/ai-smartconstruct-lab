<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2>作业与题库中心</h2>
        <p>管理思维导图题、编程题库及 AI 自动批改规则矩阵。</p>
      </div>
      <div class="action-group">
        <button class="secondary-btn">导入题库</button>
        <button class="primary-btn">+ 发布作业</button>
      </div>
    </div>

    <div class="content-tabs">
      <span class="tab active">已发布作业</span>
      <span class="tab">题库资源 (知识图谱)</span>
      <span class="tab">批改规则配置</span>
    </div>
    
    <div class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>暂无作业数据</h3>
      <p>AI Auto-Grader 正在待命，发布您的第一份智能批改作业。</p>
    </div>
  </div>
</template>
<script setup lang="ts"></script>