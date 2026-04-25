import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import './assets/styles/layout.css'
// 【新增】：必须引入 Vue Flow 底层样式，否则画布会崩溃导致路由假死
import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';

const app = createApp(App)
app.use(router)
app.mount('#app')