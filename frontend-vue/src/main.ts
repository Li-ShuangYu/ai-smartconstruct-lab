import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

import './assets/main.css'
import './assets/styles/layout.css'
import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';

// Font Awesome 配置
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'

// 注册所有 solid 图标
library.add(fas)

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
// 注册 FontAwesomeIcon 组件
app.component('FontAwesomeIcon', FontAwesomeIcon)
app.mount('#app')
