# ai-smartconstruct-lab
基于大模型与松耦合架构的智能教学实训系统。支持可视化工作流编排，以及具备真实数学逻辑的多维量化评分引擎。
ai-practicum-sandbox/
├── docs/                       # 架构与 API 契约文档
├── frontend-next/              # Next.js + TypeScript
│   ├── src/
│   │   ├── app/
│   │   │   ├── teacher-console/   # 教师端：DAG 流程编排画布、全班数据看板
│   │   │   ├── student-workspace/ # 学生端：沉浸式实训沙盒 (类似 WebIDE 界面)
│   │   │   └── shared-dashboard/  # 师生共用的交互讨论与量化报告页
│   │   └── features/
│   │       ├── dag-builder/       # React Flow 拓扑图构建器
│   │       └── code-editor/       #  Monaco Editor (或类似) 实训代码编辑器组件
│
├── backend-core/               # Spring Boot 核心总线
│   ├── src/main/java/...
│   │   ├── core/workflow/      # 核心状态机流转控制 (处理师生任务状态跃迁)
│   │   ├── core/sandbox/       # 调度测试用例、接收学生提交记录的接口
│   │   └── core/auth/          # 涉密等级区分 (教师/学生/系统管理员)
│
├── ai-engine/                  # Python + FastAPI 智能体微服务
│   ├── src/
│   │   ├── evaluator/          # 量化评分引擎 (LLM-as-a-judge & 余弦相似度)
│   │   └── socratic_tutor/     # 针对学生报错代码的五轮引导式 Agent 状态机
│
└── infrastructure/             # Docker Compose & 数据库初始化


ai-smartconstruct-lab 核心技术栈与初始化架构盘点为了确保你在后续的交付中具备坚实的架构底座，以下是整个 Monorepo（单体仓库）的技术选型与版本锁定矩阵：目录模块核心技术栈强制版本锁定 (LTS)架构职责frontend-nextNext.js + React FlowNext.js 14.x, React Flow 11.x, TS 5.x承载复杂图形化节点拖拽、学生实训沙盒视图与服务端渲染。backend-coreJava + Spring BootJava 21, Spring Boot 3.2.x 或 3.3.x核心业务总线，负责高密级涉密鉴权、任务流转控制与持久化。ai-enginePython + FastAPIPython 3.12, FastAPI 0.110+, LangGraph 0.0.30+纯算法微服务，专职处理大语言模型判定链与五步苏格拉底智能体。infrastructureDocker Compose引擎版本 24.0+, 语法 v3.8物理隔离环境，统管底层向量数据库与本地私有化大模型基座。