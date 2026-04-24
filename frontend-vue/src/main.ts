import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 【关键修复点】：在此处按层级引入你的全局 CSS
// 注意：确保这些文件在你本地确实存在，如果还没建 reset 和 variables，可以先注释掉前两行
// import './assets/styles/reset.css' 
// import './assets/styles/variables.css' 
import './assets/styles/layout.css' // 将你刚才创建的通用样式挂载到全局

const app = createApp(App)

app.use(router)
app.mount('#app')