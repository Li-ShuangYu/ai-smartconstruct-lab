---
name: "smartconstruct-frontend"
description: "AI SmartConstruct 前端开发规范。开发或修改前端页面、服务层、类型定义、Store状态管理时必须遵循此规范。"
---

# AI SmartConstruct 前端技能规范

## 项目概览

AI SmartConstruct 是一个前后端分离的实训管理平台，采用 Vue 3 + TypeScript + Vite 技术栈。

### 角色模块划分

| 角色 | 路由前缀 | 说明 |
|------|---------|------|
| Auth | `/auth` | 登录/注册模块 |
| Admin | `/admin` | 超级管理员后台 |
| Teacher | `/teacher` | 教师工作台 |
| Student | `/student` | 学生学习空间 |

## 目录结构规范

```
src/
├── assets/styles/          # 全局样式（reset.css, variables.css, layout.css, components.css）
├── components/              # 公共组件
│   ├── icons/              # 图标组件
│   └── layout/             # 布局组件（AuthLayout, WorkbenchLayout, 各角色Header/Sidebar）
├── hooks/                  # 组合式函数（useAuth, useTraining, useStepProgress等）
├── router/
│   └── modules/            # 路由模块划分（auth.ts, admin.ts, teacher.ts, student.ts, training.ts, homework.ts）
├── rules/                  # 开发规范文档
├── services/
│   ├── modules/            # 服务层（auth.service.ts, user.service.ts, training.service.ts等）
│   └── types/              # 类型定义（auth.types.ts, user.types.ts, training.types.ts等）
├── stores/
│   └── modules/            # 状态管理（auth.store.ts, training.store.ts, homework.store.ts等）
├── utils/                  # 工具函数（format.ts, storage.ts, validate.ts等）
├── views/                  # 页面组件
│   ├── admin/              # 管理员页面
│   ├── auth/               # 认证页面
│   ├── common/             # 公共组件（Pagination, AdminStandardPage, 404, 500）
│   ├── homework/           # 作业页面
│   ├── student/            # 学生页面
│   ├── teacher/            # 教师页面
│   └── training/           # 实训页面
└── App.vue, main.ts        # 应用入口
```

## 1. 类型定义层 (`services/types/*.types.ts`)

### 规范要求

- **必须使用 `interface` 关键字**定义对象结构，禁止使用 `type`。
- 将相关的 Request（请求）和 Response（响应）接口按业务分组建在一起。
- 涉及列表查询的接口，必须包含标准的 Pagination（分页）结构。
- 所有接口响应必须包含标准的响应包装结构。

### 标准分页结构

```typescript
interface Pagination {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

interface PageResult<T> {
  list: T[]
  pagination: Pagination
}
```

### 标准响应结构

```typescript
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
```

### 类型定义示例

```typescript
// services/types/training.types.ts
export interface Training {
  id: number
  name: string
  description?: string
  teacherId?: number
  teacherName?: string
  status: 'draft' | 'published' | 'archived'
  createdAt?: string
  updatedAt?: string
}

export interface CreateTrainingReq {
  name: string
  description?: string
  nodes?: TrainingNode[]
}

export interface CreateTrainingRes extends ApiResponse<Training> {}

export interface TrainingListReq {
  name?: string
  status?: string
  page: number
  pageSize: number
}

export interface TrainingListRes extends ApiResponse<PageResult<Training>> {}
```

## 2. 服务层 (`services/modules/*.service.ts`)

### 规范要求

- **必须导入项目中已封装好的单例 Axios 实例**（从 `@/services/api` 导入）。
- 每个方法都必须返回一个带有明确类型的 `Promise`。
- 禁止硬编码 URL，必须使用配置好的 API 前缀。
- 所有服务必须以 `Service` 后缀结尾（如 `TrainingService`）。

### 服务层示例

```typescript
// services/modules/training.service.ts
import http from '@/services/api'
import type {
  Training,
  CreateTrainingReq,
  CreateTrainingRes,
  TrainingListReq,
  TrainingListRes
} from '@/services/types/training.types'

export const TrainingService = {
  createTraining: (data: CreateTrainingReq): Promise<CreateTrainingRes> => {
    return http.post('/api/v1/training/create', data)
  },

  getTrainingList: (params: TrainingListReq): Promise<TrainingListRes> => {
    return http.get('/api/v1/training/list', { params })
  },

  getTrainingById: (id: number): Promise<ApiResponse<Training>> => {
    return http.get(`/api/v1/training/${id}`)
  },

  updateTraining: (id: number, data: Partial<Training>): Promise<ApiResponse<Training>> => {
    return http.put(`/api/v1/training/${id}`, data)
  },

  deleteTraining: (id: number): Promise<ApiResponse<null>> => {
    return http.delete(`/api/v1/training/${id}`)
  }
}
```

## 3. Store 状态管理层 (`stores/modules/*.store.ts`)

### 规范要求

- 使用 Pinia 进行状态管理。
- 每个 Store 必须有明确的类型定义。
- 状态变更必须通过 Actions 进行。
- 异步操作必须在 Actions 中处理。

### Store 示例

```typescript
// stores/modules/training.store.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Training, CreateTrainingReq } from '@/services/types/training.types'
import { TrainingService } from '@/services/modules/training.service'

export const useTrainingStore = defineStore('training', () => {
  const trainings = ref<Training[]>([])
  const currentTraining = ref<Training | null>(null)
  const loading = ref(false)

  const createTraining = async (data: CreateTrainingReq): Promise<Training> => {
    loading.value = true
    try {
      const res = await TrainingService.createTraining(data)
      if (res.code === 200) {
        currentTraining.value = res.data
        return res.data
      }
      throw new Error(res.message)
    } finally {
      loading.value = false
    }
  }

  return {
    trainings,
    currentTraining,
    loading,
    createTraining
  }
})
```

## 4. Hooks 层 (`hooks/*.ts`)

### 规范要求

- Hooks 必须使用 `use` 前缀命名（如 `useAuth`, `useTraining`）。
- Hooks 必须使用 Vue 3 Composition API 风格（`setup` 函数或 `<script setup>`）。
- Hooks 应该处理单一的职责。

### Hook 示例

```typescript
// hooks/useAuth.ts
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth.store'
import type { LoginReq, LoginRes } from '@/services/types/auth.types'

export const useAuth = () => {
  const router = useRouter()
  const authStore = useAuthStore()
  const loading = ref(false)

  const login = async (data: LoginReq): Promise<LoginRes | null> => {
    loading.value = true
    try {
      const result = await authStore.login(data)
      if (result) {
        router.push('/teacher/workbench')
      }
      return result
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    login
  }
}
```

## 5. 路由模块 (`router/modules/*.ts`)

### 规范要求

- 按角色划分路由模块文件。
- 路由必须包含完整的 meta 信息。
- 页面组件使用动态导入 `() => import()`。
- 沉浸式页面（如实训编排、直播监控）必须设置 `hideSidebar: true`。

### 路由配置示例

```typescript
// router/modules/teacher.ts
const teacherRoutes = [
  {
    path: '/teacher',
    name: 'TeacherRoot',
    component: () => import('@/components/layout/WorkbenchLayout/index.vue'),
    redirect: '/teacher/workbench',
    children: [
      {
        path: 'workbench',
        name: 'TeacherWorkbench',
        component: () => import('@/views/teacher/Workbench.vue'),
        meta: { title: '工作台概览' }
      },
      {
        path: 'training-create',
        name: 'TrainingCreate',
        component: () => import('@/views/teacher/TrainingCreate.vue'),
        meta: { title: '实训编排', hideSidebar: true }
      }
    ]
  }
]

export default teacherRoutes
```

## 6. 布局组件规范

### 布局组件目录

```
components/layout/
├── AuthLayout/              # 认证布局（登录/注册）
│   └── index.vue
├── WorkbenchLayout/         # 工作台布局
│   ├── AdminHeader.vue      # 管理员顶栏
│   ├── AdminSidebar.vue     # 管理员侧边栏
│   ├── SuperHeader.vue      # 超级管理员顶栏
│   ├── TeacherHeader.vue    # 教师顶栏
│   ├── TeacherSidebar.vue   # 教师侧边栏
│   ├── StudentHeader.vue    # 学生顶栏
│   ├── Sidebar.vue          # 通用侧边栏
│   └── index.vue
```

### 布局组件使用

- `AuthLayout` 用于登录/注册页面
- `WorkbenchLayout` 用于工作台类页面（教师/学生/管理员）
- 沉浸式页面需要 `hideSidebar: true` 的路由 meta

## 7. 视图页面规范

### 页面目录划分

| 目录 | 角色 | 说明 |
|------|------|------|
| `views/auth/` | 通用 | 登录(Login)、注册(Register) |
| `views/admin/` | Admin | 机构管理、用户管理、课程管理、服务监控等 |
| `views/teacher/` | Teacher | 工作台、实训管理、班级课程管理、直播监控、能力分析等 |
| `views/student/` | Student | 工作台、我的课程、我的作业、我的实训、通知等 |
| `views/homework/` | 通用 | 作业页面 |
| `views/training/` | 通用 | 实训页面 |
| `views/common/` | 通用 | Pagination、AdminStandardPage、404、500 |

### 页面组件规范

- 使用 `<script setup lang="ts">` 语法。
- 页面级组件放在 `views/` 目录下。
- 可复用组件放在 `components/` 目录下。
- 教师页面特有组件放在 `views/teacher/components/` 目录下。

## 8. 工具函数规范 (`utils/`)

### 常用工具

```typescript
// utils/storage.ts - 存储封装
// utils/format.ts - 格式化函数
// utils/validate.ts - 校验函数
```

### 命名规范

- 工具函数使用 PascalCase 或 camelCase 命名。
- 导出函数必须有无副作用的纯函数风格。

## 9. API 封装规范 (`services/api.ts`)

### Axios 实例配置

- 使用单例模式导出 Axios 实例。
- 配置请求拦截器和响应拦截器。
- 统一处理错误和 Token 刷新。

### 请求拦截器

- 自动添加 Authorization Header。
- 处理请求ID生成。

### 响应拦截器

- 统一处理 HTTP 错误码。
- 处理 Token 过期自动刷新。
- 统一错误提示。

## 10. 样式规范

### 样式文件

| 文件 | 说明 |
|------|------|
| `assets/styles/reset.css` | CSS 重置样式 |
| `assets/styles/variables.css` | CSS 变量定义 |
| `assets/styles/layout.css` | 布局样式 |
| `assets/styles/components.css` | 组件样式 |
| `assets/styles/base.css` | 基础样式 |

### 样式规范

- 使用 CSS 变量管理主题色。
- 组件样式使用 Scoped CSS 或 CSS Modules。
- 禁止使用行内样式（除动态计算值外）。

## 11. 开发流程

### 新增页面步骤

1. 在对应角色目录下创建 Vue 组件
2. 在 `router/modules/` 中配置路由
3. 在 `services/types/` 中定义类型
4. 在 `services/modules/` 中实现服务
5. 在 `stores/modules/` 中管理状态（如需要）
6. 使用 `useXxx` Hook 封装逻辑（如需要）

### 新增服务步骤

1. 在 `services/types/` 中定义 Request/Response 类型
2. 在 `services/modules/` 中实现服务方法
3. 在需要的地方导入使用

## 12. 组件命名规范

### 组件文件命名

- 使用 PascalCase 命名（如 `ClassCourseManage.vue`）
- 页面组件使用功能命名（如 `TrainingCreate.vue`）
- 通用组件使用语义命名（如 `Pagination.vue`）

### 组件引用命名

```vue
<script setup lang="ts">
import Pagination from '@/views/common/Pagination.vue'
import TrainingCreate from '@/views/teacher/TrainingCreate.vue'
</script>
```
