<template>
  <div class="app-container">
    <n-message-provider><router-view /></n-message-provider>
    <AIFloatingAssistant v-if="showAssistant" />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue"
import { useRoute } from "vue-router"
import { NMessageProvider } from "naive-ui"
import AIFloatingAssistant from "@/components/AIFloatingAssistant/AIFloatingAssistant.vue"

const route = useRoute()

const showAssistant = computed(() => !route.path.startsWith("/auth"))
</script>

<style>
@import "./assets/styles/reset.css";
@import "./assets/styles/variables.css";

html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}
</style>
