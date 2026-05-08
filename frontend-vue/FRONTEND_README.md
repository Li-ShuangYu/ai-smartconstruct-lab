# AI SmartConstruct Lab - 前端项目

基于 Vue 3 + TypeScript + Vite 的智能教学实训平台前端应用。

---

## 📋 项目概述

本项目是 AI SmartConstruct Lab 的前端部分，采用 Vue 3 Composition API 和 TypeScript 开发，为教师和学生提供沉浸式的实训体验。

### 技术栈

| 技术 | 说明 | 版本 |
|------|------|------|
| Vue 3 | 渐进式JavaScript框架 | 3.5+ |
| TypeScript | JavaScript超集 | 5.x |
| Vite | 新一代前端构建工具 | 8.x |
| Vue Router | Vue官方路由管理 | 5.x |
| Pinia | Vue状态管理 | 3.x |
| Naive UI | Vue 3组件库 | 2.x |
| Vue Flow | 流程图/节点编排组件 | 1.x |
| ECharts | 数据可视化图表库 | 6.x |
| NProgress | 路由进度条 | - |
| Axios | HTTP客户端 | 1.x |

---

## 📁 目录结构

```
frontend-vue/
├── public/                     # 静态资源（直接拷贝至dist）
│   ├── favicon.ico
│   └── 1.ico
├── src/                        # 源代码目录
│   ├── assets/                 # 静态资源
│   │   ├── styles/            # 全局样式
│   │   │   ├── reset.css      # 浏览器样式重置
│   │   │   ├── variables.css  # CSS变量（配色/间距/字号）
│   │   │   ├── layout.css    # 全局布局规范
│   │   │   ├── components.css # 通用组件样式
│   │   │   └── base.css       # 基础样式
│   │   ├── audio/             # 音频资源
│   │   │   ├── 侧信道.mp3
│   │   │   ├── 后量子算法.mp3
│   │   │   ├── 抗重放.mp3
│   │   │   └── 轻量级.mp3
│   │   ├── AIZG-Logo.png      # 品牌Logo
│   │   ├── auth-illustration.png # 登录页插图
│   │   ├── logo.svg           # SVG图标
│   │   └── main.css           # 主样式文件
│   │
│   ├── components/             # 组件目录
│   │   ├── icons/            # 图标组件
│   │   │   ├── IconCommunity.vue
│   │   │   ├── IconDocumentation.vue
│   │   │   ├── IconEcosystem.vue
│   │   │   ├── IconSupport.vue
│   │   │   └── IconTooling.vue
│   │   └── layout/           # 布局组件
│   │       ├── AuthLayout/   # 认证布局（登录/注册）
│   │       │   └── index.vue
│   │       ├── TrainingLayout/ # 实训布局
│   │       │   ├── index.vue
│   │       │   ├── StudentHeader.vue
│   │       │   └── TeacherHeader.vue
│   │       └── WorkbenchLayout/ # 工作台布局
│   │           ├── index.vue         # 主布局入口
│   │           ├── Sidebar.vue       # 通用侧边栏
│   │           ├── TeacherHeader.vue # 教师顶部导航
│   │           ├── StudentHeader.vue # 学生顶部导航
│   │           ├── AdminHeader.vue   # 管理员顶部导航
│   │           ├── SuperHeader.vue   # 超级管理员顶部导航
│   │           └── AdminSidebar.vue  # 管理员侧边栏
│   │
│   ├── hooks/                  # 组合式函数
│   │   ├── useAuth.ts         # 权限认证相关
│   │   ├── useTraining.ts     # 实训相关
│   │   ├── useStepProgress.ts # 步进流程
│   │   ├── useTelemetry.ts    # 埋点上报
│   │   └── useAssistant.ts    # 智能助手
│   │
│   ├── router/                 # 路由配置
│   │   ├── index.ts           # 路由入口
│   │   ├── guard.ts          # 路由守卫
│   │   └── modules/          # 路由模块
│   │       ├── auth.ts        # 认证路由
│   │       ├── teacher.ts     # 教师路由
│   │       ├── student.ts     # 学生路由
│   │       ├── admin.ts       # 管理员路由
│   │       ├── training.ts    # 实训路由
│   │       └── homework.ts    # 作业路由
│   │
│   ├── services/              # 服务层
│   │   ├── api.ts            # Axios封装
│   │   ├── types/            # 类型定义
│   │   │   ├── auth.types.ts
│   │   │   ├── user.types.ts
│   │   │   ├── training.types.ts
│   │   │   ├── homework.types.ts
│   │   │   └── org.types.ts
│   │   └── modules/          # API服务
│   │       ├── auth.service.ts
│   │       ├── user.service.ts
│   │       ├── training.service.ts
│   │       ├── homework.service.ts
│   │       ├── assistant.service.ts
│   │       └── org.service.ts
│   │
│   ├── stores/                # 状态管理
│   │   ├── index.ts          # Store入口
│   │   └── modules/          # Store模块
│   │       ├── auth.store.ts
│   │       ├── app.store.ts
│   │       ├── training.store.ts
│   │       ├── homework.store.ts
│   │       └── assistant.store.ts
│   │
│   ├── utils/                # 工具函数
│   │   ├── format.ts        # 格式化工具
│   │   ├── storage.ts       # 本地存储
│   │   ├── validate.ts      # 校验函数
│   │   └── telemetry.ts     # 埋点工具
│   │
│   ├── views/                # 页面组件
│   │   ├── auth/            # 认证页面
│   │   │   ├── Login.vue    # 登录页
│   │   │   └── Register.vue # 注册页
│   │   ├── teacher/         # 教师端页面
│   │   ├── student/         # 学生端页面
│   │   ├── admin/           # 管理员端页面
│   │   ├── homework/        # 作业页面
│   │   ├── training/        # 实训页面
│   │   └── common/          # 公共页面
│   │
│   ├── rules/               # 开发规范
│   │   ├── .cursorrules    # Cursor AI规则
│   │   ├── SKILL.md        # 代码生成技能规范
│   │   └── PROMPT.md       # 组件生成提示词
│   │
│   ├── App.vue             # 根组件
│   ├── main.ts             # 应用入口
│   └── env.d.ts            # TypeScript环境声明
│
├── package.json
├── tsconfig.json           # TypeScript配置
├── tsconfig.app.json       # 应用TypeScript配置
├── tsconfig.node.json      # 节点TypeScript配置
├── vite.config.ts         # Vite配置
├── index.html              # HTML入口
├── env.d.ts               # 环境声明
└── README.md              # 项目说明
```

---

## 🏗️ 核心类与模块架构

### 1. 认证模块 (Auth)

**核心类/组件**:
- `Login.vue` - 登录页面组件
- `Register.vue` - 注册页面组件
- `AuthLayout/index.vue` - 认证页面布局组件
- `useAuth.ts` - 认证相关组合式函数
- `auth.store.ts` - 认证状态管理Store
- `auth.service.ts` - 认证API服务

**核心类型定义**:
```typescript
// services/types/auth.types.ts
interface LoginRequest {
  username: string;      // 用户名/学号/工号
  password: string;      // 密码
  roleType: 'student' | 'teacher' | 'admin';  // 角色类型
}

interface LoginResponse {
  token: string;         // JWT Token
  user: UserInfo;        // 用户信息
}

interface UserInfo {
  id: number;
  username: string;
  realName: string;
  role: string;
  avatar?: string;
}
```

**认证流程**:
```
1. 用户访问登录页 (/auth/login)
   ↓
2. 选择角色类型 (学生/教师/管理员)
   ↓
3. 输入账号密码，点击登录
   ↓
4. auth.service.login() 发送登录请求
   ↓
5. 后端验证成功返回 JWT Token
   ↓
6. auth.store.setToken() 存储Token到localStorage
   ↓
7. auth.store.setUserInfo() 存储用户信息
   ↓
8. 根据角色跳转到对应工作台
   - 学生 → /student/workbench
   - 教师 → /teacher/workbench
   - 管理员 → /admin/dashboard
```

**Token管理流程**:
```
1. 登录成功获取 Token
   ↓
2. 存储到 localStorage (key: 'token')
   ↓
3. 后续请求自动携带 (api.ts 拦截器)
   ↓
4. 路由守卫检查 Token 有效性
   ↓
5. Token过期/无效 → 清除存储 → 跳转登录页
```

---

### 2. 实训编排模块 (Training Orchestration)

**核心类/组件**:
- `TrainingCreate.vue` - 实训编排页面
- `training.store.ts` - 实训状态管理
- `useTraining.ts` - 实训相关组合式函数
- `training.service.ts` - 实训API服务
- `training.types.ts` - 实训类型定义

**核心类型定义**:
```typescript
// services/types/training.types.ts
interface TrainingNode {
  id: string;                    // 节点唯一ID
  type: NodeType;                // 节点类型
  position: { x: number; y: number };  // 画布位置
  data: {
    label: string;               // 节点标签
    description?: string;        // 节点描述
    config?: Record<string, any>; // 节点配置
  };
}

type NodeType = 
  | 'llm'           // LLM调用节点
  | 'code'          // 代码执行节点
  | 'condition'     // 条件分支节点
  | 'start'         // 开始节点
  | 'end'           // 结束节点
  | 'knowledge';    // 知识库节点

interface TrainingEdge {
  id: string;
  source: string;                // 源节点ID
  target: string;                // 目标节点ID
  type?: 'default' | 'condition-true' | 'condition-false';
}

interface TrainingFlow {
  id?: number;
  name: string;                  // 实训名称
  description?: string;          // 实训描述
  nodes: TrainingNode[];         // 节点列表
  edges: TrainingEdge[];         // 连线列表
  status: 'draft' | 'published' | 'archived';
}
```

**实训编排流程**:
```
1. 教师进入实训编排页 (/teacher/training-create)
   ↓
2. 从左侧组件库拖拽节点到画布
   - Vue Flow 处理拖拽事件
   - screenToFlowCoordinate 坐标转换
   ↓
3. 连接节点形成流程
   - checkValidConnection 验证连接合法性
   - 防止自环和类型不匹配连接
   ↓
4. 配置节点属性
   - 双击节点打开配置面板
   - 设置节点参数（LLM提示词、代码等）
   ↓
5. 保存草稿或发布实训
   - 保存草稿: training.store.saveDraft()
   - 发布: training.service.publishTraining()
   ↓
6. 发布后跳转到实训管理页
```

**撤销/重做机制**:
```
数据结构:
- historyStack: 历史状态栈 (最大30步)
- redoStack: 重做栈
- currentState: 当前状态

操作流程:
1. 每次节点/连线变更
   ↓
2. 当前状态压入 historyStack
   ↓
3. 清空 redoStack
   ↓
4. 执行撤销: 
   - historyStack.pop() → currentState
   - 原状态压入 redoStack
   ↓
5. 执行重做:
   - redoStack.pop() → currentState
   - 原状态压入 historyStack
```

---

### 3. 实训执行模块 (Training Execution)

**核心类/组件**:
- `TrainingExecute.vue` - 学生实训执行页面
- `StudentCabin.vue` - 学生舱位页面
- `TrainingDetail.vue` - 实训详情页面
- `training.store.ts` - 实训状态管理
- `useWebSocket.ts` - WebSocket连接管理

**核心类型定义**:
```typescript
interface TrainingSession {
  id: number;                    // 会话ID
  trainingId: number;            // 实训ID
  studentId: number;             // 学生ID
  status: 'not_started' | 'in_progress' | 'completed' | 'abandoned';
  currentNodeId?: string;        // 当前执行节点
  progress: number;              // 进度百分比
  startTime?: Date;
  endTime?: Date;
}

interface NodeExecutionResult {
  nodeId: string;
  status: 'success' | 'error' | 'pending';
  output?: any;
  errorMessage?: string;
  executionTime: number;
}

interface WebSocketMessage {
  type: 'node_status' | 'progress_update' | 'completion' | 'error';
  payload: any;
  timestamp: number;
}
```

**实训执行流程**:
```
1. 学生进入实训执行页 (/student/training-execute/:id)
   ↓
2. 初始化 WebSocket 连接
   - 建立 /ws/training 连接
   - 发送身份验证信息
   ↓
3. 加载实训流程图
   - 获取节点和连线数据
   - 渲染 Vue Flow 画布
   ↓
4. 从起始节点开始执行
   ↓
5. 节点执行循环:
   ┌─────────────────────────────────────┐
   │  a. 高亮当前节点                     │
   │  b. 根据节点类型执行不同逻辑          │
   │     - LLM节点: 调用AI接口           │
   │     - 代码节点: 执行代码并返回结果    │
   │     - 条件节点: 根据条件选择分支      │
   │  c. 发送执行结果到WebSocket          │
   │  d. 更新进度和状态                   │
   │  e. 自动跳转到下一节点               │
   └─────────────────────────────────────┘
   ↓
6. 到达结束节点，标记完成
   ↓
7. 生成实训报告
```

**WebSocket实时通信流程**:
```
连接建立:
1. 学生进入实训页面
   ↓
2. useWebSocket.connect(trainingSessionId)
   ↓
3. 发送连接请求到 ws://localhost:8080/ws/training
   ↓
4. 后端 TrainingHandshakeInterceptor 验证身份
   ↓
5. TrainingWebSocketHandler 处理连接
   ↓
6. 连接成功，开始监听消息

消息收发:
- 学生端发送: { type: 'node_complete', nodeId: 'xxx', result: {...} }
- 后端广播: { type: 'progress_update', studentId: 1, progress: 50 }
- 教师监控端实时接收并展示
```

---

### 4. 状态管理模块 (Pinia Stores)

**核心Store类**:

#### auth.store.ts - 认证状态管理
```typescript
interface AuthState {
  token: string | null;          // JWT Token
  userInfo: UserInfo | null;     // 用户信息
  isLoggedIn: boolean;           // 登录状态
}

// Actions:
- login(credentials): Promise<void>    // 登录
- logout(): void                       // 登出
- setToken(token): void                // 设置Token
- setUserInfo(info): void              // 设置用户信息
- clearAuth(): void                    // 清除认证信息
```

#### training.store.ts - 实训状态管理
```typescript
interface TrainingState {
  currentTraining: TrainingFlow | null;  // 当前实训
  nodes: TrainingNode[];                 // 节点列表
  edges: TrainingEdge[];                 // 连线列表
  historyStack: HistoryState[];          // 历史栈
  redoStack: HistoryState[];             // 重做栈
  selectedNode: TrainingNode | null;     // 选中节点
}

// Actions:
- setNodes(nodes): void                // 设置节点
- setEdges(edges): void                // 设置连线
- addNode(node): void                  // 添加节点
- updateNode(id, data): void           // 更新节点
- removeNode(id): void                 // 删除节点
- addEdge(edge): void                  // 添加连线
- removeEdge(id): void                 // 删除连线
- saveToHistory(): void                // 保存到历史
- undo(): void                         // 撤销
- redo(): void                         // 重做
- clearCanvas(): void                  // 清空画布
```

#### app.store.ts - 应用状态管理
```typescript
interface AppState {
  sidebarCollapsed: boolean;     // 侧边栏折叠状态
  theme: 'light' | 'dark';       // 主题
  language: string;              // 语言
  loading: boolean;              // 全局加载状态
}

// Actions:
- toggleSidebar(): void          // 切换侧边栏
- setTheme(theme): void          // 设置主题
- setLanguage(lang): void        // 设置语言
- setLoading(status): void       // 设置加载状态
```

#### homework.store.ts - 作业状态管理
```typescript
interface HomeworkState {
  homeworkList: Homework[];      // 作业列表
  currentHomework: Homework | null;
  submissions: Submission[];     // 提交记录
}

// Actions:
- fetchHomeworkList(): Promise<void>
- submitHomework(data): Promise<void>
- fetchSubmissions(homeworkId): Promise<void>
```

---

### 5. API服务模块 (Services)

**核心服务类**:

#### api.ts - Axios封装
```typescript
// 核心配置
const http: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  }
);

// 响应拦截器
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，清除并跳转登录
      authStore.logout();
      router.push('/auth/login');
    }
    return Promise.reject(error);
  }
);
```

#### auth.service.ts - 认证服务
```typescript
class AuthService {
  // 用户登录
  async login(data: LoginRequest): Promise<LoginResponse>
  
  // 用户注册
  async register(data: RegisterRequest): Promise<void>
  
  // 获取当前用户信息
  async getCurrentUser(): Promise<UserInfo>
  
  // 修改密码
  async changePassword(data: ChangePasswordRequest): Promise<void>
  
  // 登出
  async logout(): Promise<void>
}
```

#### training.service.ts - 实训服务
```typescript
class TrainingService {
  // 获取实训列表
  async getTrainingList(params: QueryParams): Promise<PageResult<TrainingFlow>>
  
  // 获取实训详情
  async getTrainingDetail(id: number): Promise<TrainingFlow>
  
  // 创建实训
  async createTraining(data: TrainingFlow): Promise<TrainingFlow>
  
  // 更新实训
  async updateTraining(id: number, data: TrainingFlow): Promise<TrainingFlow>
  
  // 发布实训
  async publishTraining(id: number): Promise<void>
  
  // 删除实训
  async deleteTraining(id: number): Promise<void>
  
  // 开始实训会话
  async startSession(trainingId: number): Promise<TrainingSession>
  
  // 提交节点执行结果
  async submitNodeResult(sessionId: number, result: NodeExecutionResult): Promise<void>
}
```

#### user.service.ts - 用户服务
```typescript
class UserService {
  // 获取用户列表
  async getUserList(params: QueryParams): Promise<PageResult<UserInfo>>
  
  // 更新用户信息
  async updateUser(id: number, data: UpdateUserRequest): Promise<UserInfo>
  
  // 上传头像
  async uploadAvatar(file: File): Promise<string>
}
```

---

### 6. 路由模块 (Router)

**核心文件**:
- `router/index.ts` - 路由入口配置
- `router/guard.ts` - 路由守卫
- `router/modules/*.ts` - 路由模块

**路由守卫流程**:
```typescript
// guard.ts
router.beforeEach((to, from, next) => {
  // 1. 启动进度条
  NProgress.start();
  
  // 2. 设置页面标题
  document.title = `${to.meta.title} - AI学苑`;
  
  // 3. 注入角色信息
  const role = to.path.split('/')[1];
  to.meta.role = role;
  
  // 4. 权限校验
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token');
    if (!token) {
      next('/auth/login');
      return;
    }
    
    // 校验角色权限
    const userRole = authStore.userInfo?.role;
    if (to.meta.allowedRoles && !to.meta.allowedRoles.includes(userRole)) {
      next('/403');
      return;
    }
  }
  
  next();
});

router.afterEach(() => {
  // 结束进度条
  NProgress.done();
});
```

**路由模块结构**:
```typescript
// router/modules/teacher.ts
const teacherRoutes = [
  {
    path: '/teacher',
    component: WorkbenchLayout,
    meta: { requiresAuth: true, allowedRoles: ['teacher'] },
    children: [
      { path: 'workbench', component: TeacherWorkbench },
      { path: 'training-manage', component: TrainingManage },
      { path: 'training-create', component: TrainingCreate, meta: { hideSidebar: true } },
      { path: 'evaluation', component: EvaluationDashboard },
    ]
  }
];
```

---

### 7. 布局组件模块 (Layout Components)

**核心布局组件**:

#### AuthLayout - 认证布局
```typescript
// components/layout/AuthLayout/index.vue
// 用途: 登录/注册页面布局
// 结构:
┌─────────────────────────────────────────────┐
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │              │  │                      │ │
│  │   品牌展示    │  │     表单区域          │ │
│  │   (左侧)      │  │     (右侧)            │ │
│  │              │  │                      │ │
│  └──────────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────┘

// Props: 无
// Slots: default (表单内容)
```

#### WorkbenchLayout - 工作台布局
```typescript
// components/layout/WorkbenchLayout/index.vue
// 用途: 教师/学生/管理员工作台
// 结构:
┌─────────────────────────────────────────────┐
│  Header (根据角色显示不同导航)              │
├─────────┬───────────────────────────────────┤
│         │                                   │
│ Sidebar │         Main Content              │
│         │         (router-view)             │
│         │                                   │
└─────────┴───────────────────────────────────┘

// Props:
interface Props {
  role: 'teacher' | 'student' | 'admin';
  hideSidebar?: boolean;  // 沉浸式模式
}
```

#### TrainingLayout - 实训布局
```typescript
// components/layout/TrainingLayout/index.vue
// 用途: 实训执行页面
// 特点: 全屏布局，无侧边栏，专注于实训内容
```

---

### 8. 组合式函数模块 (Hooks)

**核心Hooks**:

#### useAuth.ts - 认证Hook
```typescript
export function useAuth() {
  const store = useAuthStore();
  const router = useRouter();
  
  // 登录
  const login = async (credentials: LoginRequest) => {
    await store.login(credentials);
    redirectByRole(store.userInfo.role);
  };
  
  // 登出
  const logout = () => {
    store.logout();
    router.push('/auth/login');
  };
  
  // 检查权限
  const hasPermission = (permission: string) => {
    return store.userInfo?.permissions?.includes(permission);
  };
  
  return { login, logout, hasPermission, isLoggedIn: store.isLoggedIn };
}
```

#### useTraining.ts - 实训Hook
```typescript
export function useTraining() {
  const store = useTrainingStore();
  
  // 加载实训
  const loadTraining = async (id: number) => {
    const training = await trainingService.getTrainingDetail(id);
    store.setNodes(training.nodes);
    store.setEdges(training.edges);
  };
  
  // 保存实训
  const saveTraining = async (data: TrainingFlow) => {
    if (data.id) {
      await trainingService.updateTraining(data.id, data);
    } else {
      await trainingService.createTraining(data);
    }
  };
  
  // 节点操作
  const addNode = (type: NodeType, position: Position) => {
    const node = createNode(type, position);
    store.addNode(node);
    store.saveToHistory();
  };
  
  return { loadTraining, saveTraining, addNode, ...store };
}
```

#### useWebSocket.ts - WebSocket Hook
```typescript
export function useWebSocket() {
  const ws = ref<WebSocket | null>(null);
  const isConnected = ref(false);
  const messages = ref<WebSocketMessage[]>([]);
  
  // 建立连接
  const connect = (sessionId: number) => {
    ws.value = new WebSocket(`ws://localhost:8080/ws/training?sessionId=${sessionId}`);
    
    ws.value.onopen = () => { isConnected.value = true; };
    ws.value.onmessage = (event) => {
      messages.value.push(JSON.parse(event.data));
    };
    ws.value.onclose = () => { isConnected.value = false; };
  };
  
  // 发送消息
  const send = (message: WebSocketMessage) => {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify(message));
    }
  };
  
  // 断开连接
  const disconnect = () => {
    ws.value?.close();
  };
  
  return { connect, send, disconnect, isConnected, messages };
}
```

---

## 🎯 路由系统

### 路由架构

```
/                           # 根路径，重定向到登录页
├── /auth                   # 认证模块
│   ├── /auth/login        # 登录页
│   └── /auth/register     # 注册页
│
├── /teacher               # 教师工作台（带侧边栏）
│   ├── /teacher/workbench           # 工作台概览
│   ├── /teacher/training-manage    # 实训管理
│   ├── /teacher/class-course-manage # 班级课程管理
│   ├── /teacher/evaluation         # 评估分析
│   └── /teacher/profile           # 用户中心
│
├── /teacher/* (沉浸式，无侧边栏)   # 教师沉浸式页面
│   ├── /teacher/training-create    # 实训编排
│   ├── /teacher/teacher-live-monitor # 直播监控
│   ├── /teacher/training-publish   # 发布实训
│   ├── /teacher/class-competency/:studentId  # 班级能力分析
│   └── /teacher/student-competency/:studentId # 学生能力分析
│
├── /student               # 学生学习空间（带侧边栏）
│   ├── /student/workbench          # 工作台
│   ├── /student/student-cabin/:id # 学生舱位
│   ├── /student/training-detail/:id # 实训详情
│   ├── /student/my-class          # 我的班级
│   ├── /student/courselist        # 课程列表
│   ├── /student/notifications     # 消息通知
│   ├── /student/my-homework       # 我的作业
│   ├── /student/my-submission     # 我的提交
│   ├── /student/my-training       # 我的实训
│   └── /student/profile           # 个人中心
│
├── /training              # 实训模块
│   ├── /training/theory           # 理论实训
│   └── /training/code             # 代码实训
│
├── /admin                 # 管理员后台
│   ├── /admin/teacher     # 教师管理
│   ├── /admin/student     # 学生管理
│   ├── /admin/org         # 机构管理
│   ├── /admin/course      # 课程管理
│   ├── /admin/menu        # 菜单管理
│   ├── /admin/node        # 节点管理
│   ├── /admin/template    # 模板管理
│   ├── /admin/question    # 题目管理
│   ├── /admin/ticket      # 工单管理
│   ├── /admin/monitor     # 服务监控
│   └── /admin/audit       # 审计日志
│
└── /:pathMatch(.*)*       # 404页面
```

### 路由守卫

路由守卫在 `router/guard.ts` 中实现，主要功能：

1. **进度条**：使用 NProgress 显示页面加载进度
2. **角色注入**：根据路径自动注入 `meta.role`（student/teacher/admin/auth）
3. **标题设置**：自动设置页面标题，格式为 `{页面标题} - AI学苑`
4. **权限校验**：检查登录状态，未登录用户重定向到登录页

---

## 🎨 布局系统

### AuthLayout - 认证布局

用于登录/注册页面，采用左右分栏设计：

- **左侧**：品牌展示区，包含Logo、标题、副标题和背景插图
- **右侧**：登录/注册表单区域，毛玻璃效果背景
- **底部**：版权信息

### WorkbenchLayout - 工作台布局

用于教师/学生/管理员的工作台页面，结构如下：

```
┌─────────────────────────────────────────────┐
│  Header (根据角色显示不同导航)              │
├─────────┬───────────────────────────────────┤
│         │                                   │
│ Sidebar │         Main Content              │
│         │                                   │
│         │                                   │
└─────────┴───────────────────────────────────┘
```

#### Header 组件

| 组件 | 用途 |
|------|------|
| TeacherHeader | 教师顶部导航 |
| StudentHeader | 学生顶部导航 |
| AdminHeader | 管理员顶部导航 |
| SuperHeader | 超级管理员顶部导航 |

#### Sidebar 组件

| 组件 | 用途 |
|------|------|
| Sidebar | 教师/通用侧边栏 |
| AdminSidebar | 管理员侧边栏 |

#### 沉浸式模式

当路由 meta 设置 `hideSidebar: true` 时：
- 不显示 Header 和 Sidebar
- 全屏展示内容
- 适用于：实训编排、直播监控、发布实训等沉浸式场景

---

## 📄 页面详解

### 认证模块 (auth)

#### Login.vue
- **路径**：`/auth/login`
- **功能**：用户登录，支持学生/教师/管理员三种角色切换登录
- **技术实现**：
  - Vue 3 Composition API + `<script setup>` 语法
  - 响应式数据管理（ref、computed）
  - 路由编程式导航（useRouter + router.push）
  - CSS Scoped 样式隔离
  - 角色切换状态管理
- **UI特性**：
  - **角色分段控制器**：三个可点击角色卡片（学生端/教师端/管理端），带SVG图标
  - **动态账号占位符**：根据角色切换显示不同提示文字（学号/工号/管理员账号）
  - **表单选项**：记住我复选框、忘记密码链接
  - **注册入口**：底部跳转到注册页面链接
- **布局**：AuthLayout 左右分栏结构，左侧品牌展示区，右侧登录表单

#### Register.vue
- **路径**：`/auth/register`
- **功能**：用户注册账号
- **技术实现**：表单校验、路由跳转

### 教师端 (teacher)

#### Workbench.vue
- **路径**：`/teacher/workbench`
- **功能**：教师工作台首页，仪表盘式数据展示
- **技术实现**：
  - Vue 3 Composition API + `<script setup>`
  - 响应式布局（flex、grid）
  - 路由编程式导航
  - 模拟数据驱动展示
- **UI特性**：
  - **Header区域**：问候语 + 今日任务统计卡片（本周活跃学生、待办任务）
  - **快捷入口网格**：三个主操作卡片（创建实训/开启实训/综合评价），带图标和描述
  - **历史实训列表**：状态标签（已就绪/进行中/已结束）、时间信息、参与人数统计
  - **卡片悬浮效果**：hover transform 动画
- **业务场景**：密码学多智能体协同实训管理

#### TrainingCreate.vue
- **路径**：`/teacher/training-create`
- **功能**：可视化实训流程编排器，拖拽式节点工作流设计
- **技术实现**：
  - **Vue Flow**：基于 @vue-flow/core 的可视化工作流编辑器
  - **拖拽API**：HTML5 Drag & Drop API（draggable、ondragstart、ondrop）
  - **坐标转换**：screenToFlowCoordinate 将页面坐标转换为流程图坐标
  - **节点组件**：markRaw 动态组件注册（避免响应式开销）
  - **历史记录系统**：撤回/恢复栈（historyStack、redoStack），最多保存30步
  - **连线校验**：checkValidConnection 防止自环和类型错误连接
  - **状态管理**：Pinia store（useTrainingStore）管理 nodes 和 edges
- **UI特性**：
  - **左侧组件仓库面板**：可折叠侧边栏，分类展示节点（LLM调用/代码执行/条件分支等）
  - **顶部工具栏**：撤回/恢复按钮、草稿暂存、发布实训
  - **Canvas画布**：VueFlow 实例，支持缩放、拖拽、节点连接
  - **Background背景**：点阵图案（pattern-color, gap, size）
  - **Controls控件**：右下角最小化导航控件
  - **成功弹窗**：发布后的模态框 + 倒计时自动跳转
- **布局**：MaterialSidebar + CanvasContainer + ModalOverlay 三栏结构
- **业务场景**：编排 SM4 密钥配置、无人机抗重放攻击等实训流程

#### TrainingManage.vue
- **路径**：`/teacher/training-manage`
- **功能**：实训资源全生命周期管理（模板/待发布/进行中/历史）
- **技术实现**：
  - Vue 3 Composition API + TypeScript 类型断言
  - Tab 状态切换（template/pending/ongoing/history）
  - 分页逻辑（totalPages、handlePageChange）
  - 路由编程跳转
  - Transition 组件实现 Tab 切换动画
- **UI特性**：
  - **Tab导航栏**：四个状态切换标签，当前选中项高亮
  - **数据表格**：Grid 布局表头 + 表体，支持不同操作按钮
  - **状态标签**：根据状态显示不同颜色标签（已就绪/进行中/已结束）
  - **分页控件**：上一页/下一页 + 页码数字按钮 + 共多少项信息
  - **空状态**：无数据时显示友好提示
- **操作流程**：编辑模板 → 创建实训任务 → 开启实训 → 进入实训监控 → 结束实训 → 查看历史

#### TrainingPublish.vue
- **路径**：`/teacher/training-publish`
- **功能**：实训任务发布，选择班级和学生下发
- **技术实现**：表单配置、班级选择组件

#### EvaluationDashboard.vue
- **路径**：`/teacher/evaluation`
- **功能**：实训评价总览，多维数据统计
- **技术实现**：
  - Vue 3 Composition API
  - 计算属性过滤（filteredProjects）
  - 搜索功能（searchQuery）
  - 路由编程跳转（useRouter）
- **UI特性**：
  - **搜索栏**：输入框过滤项目名称或分类
  - **项目卡片网格**：每个项目显示分类标签、日期、平均分、完成率、技能标签
  - **指标展示**：平均分、完成率数字高亮
  - **技能标签**：如 SM4、时间戳验证、Dilithium 等密码学相关标签
  - **操作按钮**：查看详细报告跳转
- **业务场景**：评价学生密码学实训能力，如无人机通信抗重放攻击、国密SM3杂凑分析等

#### EvaluationManage.vue
- **路径**：`/teacher/evaluation-manage`
- **功能**：评价任务管理，创建和管理评价活动
- **技术实现**：表单编辑、状态管理

#### ClassCompetencyProfile.vue
- **路径**：`/teacher/class-competency/:studentId`
- **功能**：班级实训总结分析，能力雷达图
- **技术实现**：
  - 路由参数获取（useRoute.params）
  - 图表可视化（雷达图/柱状图）
  - 数据聚合分析

#### StudentCompetencyProfile.vue
- **路径**：`/teacher/student-competency/:studentId`
- **功能**：学生个人实训能力画像
- **技术实现**：
  - 单学生数据查询
  - 能力维度展示

#### ClassCourseManage.vue
- **路径**：`/teacher/class-course`
- **功能**：班级课程关联管理
- **技术实现**：课程分配、班级数据管理

#### TeacherLiveMonitor.vue
- **路径**：`/teacher/live-monitor`
- **功能**：实训现场实时监控
- **技术实现**：实时数据刷新、WebSocket 连接

#### UserProfile.vue
- **路径**：`/teacher/profile`
- **功能**：教师个人信息管理
- **技术实现**：表单编辑、个人信息展示

### 学生端 (student)

#### Workbench.vue
- **路径**：`/student/workbench`
- **功能**：学生学习工作台，AI助教交互 + 实训任务列表
- **技术实现**：
  - Vue 3 Composition API + `<script setup>` 语法
  - 分页组件集成（Pagination.vue）
  - 计算属性过滤（tabFilteredList、paginatedList）
  - 响应式数据处理
- **UI特性**：
  - **背景效果**：动态光晕背景（bg-glow、radial-gradient）
  - **Hero区域**：品牌Logo + AI对话气泡 + 快捷建议标签（解释抗重放机制/SM4编程第一步/查看我的进度）
  - **数据看板**：三列统计卡片（我的课程/我的作业/实训进度）
  - **Tab切换**：待完成/已完成/全部任务筛选
  - **任务列表**：网格布局卡片，显示封面、标题、描述、进度条、截止时间、操作按钮
  - **空状态**：无任务时显示占位图和提示

#### StudentCabin.vue
- **路径**：`/student/student-cabin/:id`
- **功能**：学生个人学习舱位，沉浸式学习环境
- **技术实现**：
  - 路由参数获取（useRoute.params.id）
  - 动态数据加载
  - 个性化展示

#### TrainingDetail.vue
- **路径**：`/student/training-detail/:id`
- **功能**：实训详情查看
- **技术实现**：
  - 路由参数解析
  - 详情数据获取
  - 开始实训跳转

#### TrainingExecute.vue
- **路径**：`/training/execute/:id`
- **功能**：学生实训执行页面，核心实训交互
- **技术实现**：
  - **Vue Flow**：可视化流程执行
  - **WebSocket**：实时通信获取节点状态
  - **代码编辑器**：Monaco Editor 或 CodeMirror 集成
  - **状态管理**：training.store 管理执行状态
- **UI特性**：
  - **流程可视化**：左侧显示实训流程图，高亮当前节点
  - **工作区**：右侧主内容区显示当前节点任务
  - **控制台**：底部输出执行结果和日志
  - **进度条**：顶部显示整体进度
- **执行流程**：
  ```
  1. 加载实训流程图
  2. 建立 WebSocket 连接
  3. 从起始节点开始
  4. 循环执行节点:
     - 显示节点内容（LLM对话/代码编辑/选择题）
     - 学生完成任务
     - 提交结果
     - 等待后端验证
     - 高亮下一节点
  5. 完成所有节点，生成报告
  ```

#### MyClass.vue
- **路径**：`/student/my-class`
- **功能**：我的班级信息查看
- **技术实现**：班级数据获取、成员列表展示

#### CourseList.vue
- **路径**：`/student/courselist`
- **功能**：课程列表浏览
- **技术实现**：课程数据获取、筛选排序

#### Notifications.vue
- **路径**：`/student/notifications`
- **功能**：消息通知中心
- **技术实现**：通知列表、已读/未读状态

#### MyHomework.vue
- **路径**：`/student/my-homework`
- **功能**：我的作业列表
- **技术实现**：作业数据获取、状态筛选

#### MySubmission.vue
- **路径**：`/student/my-submission`
- **功能**：我的提交记录
- **技术实现**：提交历史、成绩查看

#### MyTraining.vue
- **路径**：`/student/my-training`
- **功能**：我的实训记录
- **技术实现**：实训历史、进度查看

#### UserProfile.vue
- **路径**：`/student/profile`
- **功能**：学生个人信息管理
- **技术实现**：表单编辑、头像上传

### 管理员端 (admin)

#### TeacherManage.vue
- **路径**：`/admin/teacher`
- **功能**：教师账号管理
- **技术实现**：CRUD操作、分页列表

#### StudentManage.vue
- **路径**：`/admin/student`
- **功能**：学生账号管理
- **技术实现**：CRUD操作、批量导入

#### OrgManage.vue
- **路径**：`/admin/org`
- **功能**：机构组织管理
- **技术实现**：树形结构、层级管理

#### CourseManage.vue
- **路径**：`/admin/course`
- **功能**：课程资源管理
- **技术实现**：课程CRUD、分类管理

---

## 🔧 配置说明

### 后端接口配置

前端默认连接后端地址：`http://localhost:8080`

如需修改后端地址，请编辑 `src/services/api.ts`：

```typescript
const http: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // 修改此地址
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})
```

### 跨域配置

后端已配置允许以下前端域名访问：
- http://localhost:5173
- http://localhost:5174
- http://localhost:3000

---

## 📖 开发指南

### 添加新页面

1. 在 `src/views/` 目录创建页面组件
2. 在 `src/router/modules/` 目录添加路由配置
3. 在 `src/services/modules/` 添加对应的 API 服务（如需要）
4. 在 `src/stores/modules/` 添加状态管理（如需要）

### 代码规范

- 使用 Vue 3 Composition API + `<script setup>` 语法
- 使用 TypeScript 进行类型检查
- 使用 Pinia 进行状态管理
- 使用 Axios 进行 HTTP 请求
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

---

## ❓ 常见问题

### 1. 登录时报"网络错误，请稍后重试"

**问题描述**: 输入正确账号密码后提示网络错误

**原因分析**: 通常是跨域问题或后端服务未启动

**解决方案**:
1. 确认后端服务正在运行（端口 8080）
2. 确认前端运行端口已添加到后端 CORS 配置
3. 使用命令检查端口状态：`netstat -ano | findstr 