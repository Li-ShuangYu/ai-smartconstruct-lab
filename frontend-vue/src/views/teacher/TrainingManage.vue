<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2>实训项目库</h2>
        <p>管理您的全周期实训课程、实验舱环境与历史快照。</p>
      </div>
      <button class="primary-btn" @click="$router.push('/teacher/training/create')">
        + 新建实训编排
      </button>
    </div>

    <div class="empty-state">
      <div class="empty-icon">📂</div>
      <h3>暂无实训项目</h3>
      <p>点击右上角新建一个基于 DAG 的实训流程吧。</p>
    </div>
  </div>
</template>
<script setup lang="ts"></script>