src/
├── assets/               # 静态资源，严格分类
│   ├── styles/           # OpenMaic风格全局样式核心
│   │   ├── reset.css     # 浏览器样式重置
│   │   ├── variables.css # 全局CSS变量（配色/间距/字号，完全固化）
│   │   ├── layout.css    # 全局布局规范
│   │   └── components.css # 通用组件样式规范
│   ├── icons/            # SVG图标，统一命名：模块-功能.svg
│   └── images/           # 静态图片，仅允许webp/png格式
├── components/           # 组件严格分级，禁止跨级复用
│   ├── base/             # 原子基础组件（不可再分，OpenMaic最小交互单元）
│   │   ├── Button/       # 统一按钮组件，仅允许预设type/size
│   │   ├── Card/         # 步进卡片核心组件（One-by-One实训基础）
│   │   ├── Input/        # 统一输入框组件，内置校验规范
│   │   ├── Loading/      # 极简加载组件，无多余动画
│   │   └── Typography/   # 文本组件，统一字号/行高
│   ├── business/         # 业务通用组件（跨页面复用）
│   │   ├── CourseCard/   # 实训课程卡片
│   │   ├── StepProgress/ # 步进进度条组件
│   │   ├── MindMap/      # 思维导图组件（封装Vue Flow）
│   │   ├── CodeEditor/   # 代码编辑器基础组件
│   │   └── TelemetryTracker/ # 埋点上报通用组件
│   ├── layout/           # 布局组件，严格插槽规范
│   │   ├── AppLayout/    # 全局应用布局（侧边栏+顶部导航）
│   │   ├── WorkbenchLayout/ # 学生/教师工作台通用布局
│   │   ├── TrainingLayout/ # 沉浸式实训布局（无多余干扰元素）
│   │   └── AuthLayout/   # 登录注册极简布局
│   └── ai-assistant/     # 智能助手专属组件
│       ├── AssistantDrawer/ # 侧边栏助手抽屉
│       ├── AssistantFloatBtn/ # 悬浮助手按钮
│       └── MessageBubble/ # 对话气泡组件
├── router/               # 路由配置，严格分层权限
│   ├── index.ts          # 路由入口
│   ├── guard.ts          # 路由守卫（登录态/权限校验）
│   └── modules/          # 按业务拆分路由模块
│       ├── auth.ts       # 登录注册路由
│       ├── teacher.ts    # 教师工作台路由
│       ├── student.ts    # 学生工作台路由
│       ├── training.ts   # 实训模块路由
│       └── homework.ts   # 作业模块路由
├── services/             # 服务层（唯一网络请求入口，AI禁止在组件/Store直接调用Axios）
│   ├── api.ts            # Axios实例封装（统一拦截器/错误处理）
│   ├── types/            # 前后端契约TS类型定义（请求/响应全量覆盖）
│   │   ├── auth.types.ts
│   │   ├── training.types.ts
│   │   ├── homework.types.ts
│   │   └── user.types.ts
│   └── modules/          # 按业务拆分API服务
│       ├── auth.service.ts
│       ├── training.service.ts
│       ├── homework.service.ts
│       ├── user.service.ts
│       └── assistant.service.ts
├── stores/               # Pinia状态层（严格数据-only，禁止异步请求）
│   ├── index.ts          # store入口
│   └── modules/
│       ├── auth.store.ts # 用户身份/权限状态
│       ├── training.store.ts # 实训全局状态
│       ├── homework.store.ts # 作业全局状态
│       ├── assistant.store.ts # 助手对话状态
│       └── app.store.ts  # 全局应用状态
├── utils/                # 纯函数工具，无副作用
│   ├── format.ts         # 格式化工具
│   ├── validate.ts       # 校验工具
│   ├── telemetry.ts      # 埋点上报工具
│   └── storage.ts        # 本地存储工具
├── hooks/                # 组合式函数，复用业务逻辑
│   ├── useAuth.ts        # 权限相关hooks
│   ├── useTraining.ts    # 实训相关hooks
│   ├── useStepProgress.ts # 步进流程通用hooks
│   ├── useTelemetry.ts   # 埋点上报hooks
│   └── useAssistant.ts   # 智能助手hooks
├── rules/                # AI规则文件（团队与AI必须100%遵守）
│   ├── .cursorrules      # Cursor编辑器AI生成规则
│   ├── SKILL.md          # 代码生成技能规范
│   └── PROMPT.md         # 业务组件生成提示词模板
├── views/                # 页面组件，与路由一一对应
│   ├── auth/             # 登录注册页面
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── teacher/          # 教师工作台页面
│   │   ├── Workbench.vue
│   │   ├── TrainingCreate.vue
│   │   ├── TrainingManage.vue
│   │   ├── AssignmentManage.vue
│   │   └── EvaluationManage.vue
│   ├── student/          # 学生工作台页面
│   │   ├── Workbench.vue
│   │   ├── MyTraining.vue
│   │   ├── MyHomework.vue
│   │   └── MySubmission.vue
│   ├── training/         # 实训核心页面
│   │   ├── TheoryTraining.vue # One-by-One理论实训
│   │   └── CodeTraining.vue # 编码实训舱
│   ├── homework/         # 作业模块页面
│   │   ├── HomeworkDetail.vue
│   │   ├── MindMapPractice.vue
│   │   └── ExamPage.vue
│   └── common/           # 通用页面
│       ├── 404.vue
│       └── 500.vue
├── App.vue               # 根组件，仅做布局入口与全局注册
├── main.ts               # 应用入口，仅做插件注册与实例挂载
└── vite-env.d.ts         # TS环境声明










# 角色与任务定义
你是一位顶尖的 Vue 3 前端架构师。现在的任务是为 "ai-smartconstruct-lab" 项目一次性生成一个完整的业务模块代码。
你必须严格基于我提供的业务需求，并100%遵守项目的底层架构铁律。

# 📂 架构规则文件索引（必须读取并遵守）
在生成代码前，请你务必查阅并严格遵守以下项目本地的规则文件（如果你的环境无法读取，请默认遵守标准的 Vue3 严格三层解耦架构）：
1. `@/rules/.cursorrules` -> 核心高压红线（禁止 View 层发请求、强制使用全局 CSS 变量等）。
2. `@/rules/SKILL.md` -> 代码排列骨架与拼装顺序规范。

# 🚨 本次生成的最高约束（违反任何一条视为彻底失败）
1. **严格的四文件隔离：** 必须为你生成的代码分块，一次性输出这 4 个文件：`*.types.ts` -> `*.service.ts` -> `*.store.ts` -> `*.vue`。
2. **职责绝对单一：**
   - View (`.vue`)：只能有 UI 渲染、表单校验、埋点调用 (`useTelemetry`) 和 Store 的 action 调用。**绝对禁止出现 axios。**
   - Store (`.store.ts`)：只能有 Pinia Setup 写法，管理 loading 状态和响应式数据，调用 Service。**绝对禁止直接引入 axios。**
   - Service (`.service.ts`)：只能引入 `@/services/api`，返回强类型 Promise。
3. **UI 规范：** 只能使用 Naive UI 组件。不允许写硬编码的颜色或尺寸，必须使用 `var(--spacing-xxx)` 等 CSS 变量。

---

# 📝 本次业务需求输入

- **模块名称：** [例如：TrainingCreate / 实训创建]
- **所属路由目录：** [例如：teacher]
- **核心数据字段 (Types)：**
  [在这里详细列出表单或页面的字段。例如：
  - title: 字符串，必填
  - difficulty: 枚举 ('EASY' | 'MEDIUM' | 'HARD')，必填
  - description: 字符串，选填
  - tags: 字符串数组
  ]
- **UI 视图要求 (View)：**
  [在这里描述页面长什么样。例如：
  1. 外层使用 `<WorkbenchLayout>` 包裹。
  2. 页面居中是一个 Naive UI 的 Card，包含一个表单。
  3. 表单需要带基础规则校验。
  4. 底部一个 primary 按钮“创建实训”。点击时要有 loading 状态。
  ]

---

# 🛠️ 要求的输出格式

请按照以下顺序，依次输出 4 个代码块（请在每个代码块前标明完整的文件路径）：

1. **`src/services/types/[模块名].types.ts`** (包含前后端交互的接口定义)
2. **`src/services/modules/[模块名].service.ts`** (封装网络请求)
3. **`src/stores/modules/[模块名].store.ts`** (Pinia 状态管理)
4. **`src/views/[所属路由目录]/[模块名].vue`** (最终的 UI 组件代码)

请开始生成代码，不要输出多余的解释，直接给出代码块。