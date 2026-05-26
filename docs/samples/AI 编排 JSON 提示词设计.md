# **智能实训系统大模型多智能体编排与提示词工程深度架构报告**

## **核心架构概览与多智能体状态机模式**

在现代企业级智能实训系统中，实训流的编排与发布环节是从前端静态配置（JSON）向动态教学资源与底层持久化数据（Database）转换的核心枢纽。依据所提供的实训大纲 JSON（例如“Java高并发编程实战”）及底层数据库结构，系统在接收到前端发布指令后，面临着高度复杂的“存算分离”挑战。系统需要将结构化配置持久化至 MySQL 数据库，同时异步唤起大语言模型（LLM）进行教学资源的深度预计算与生成。  
传统的单体应用或单一链式大模型调用无法满足生产级的高并发、状态管理与容错需求。系统架构在处理长耗时的 Agent 工作流时，若依赖同步 HTTP 请求，必然面临连接超时、状态丢失以及并发抢占等问题 1。因此，必须引入基于图结构的多智能体状态机（State Machine）框架，例如 LangGraph 2。LangGraph 通过定义图中的节点（Nodes）为不同专长的智能体，通过边（Edges）控制逻辑流转，能够完美实现实训编排发布时的复杂分发、并行处理与容错重试 4。在此架构下，大模型不再是单纯的文本生成器，而是受控的、具备状态记忆与路由能力的计算节点 6。

### **系统状态机与数据库映射基准**

在发布操作的保存阶段，原始的前端编排结果首先作为 raw\_canvas\_json 落库至 wf\_training\_template 表 7。随后，总控智能体（Supervisor Agent）接管流程，解析该 JSON 树，剥离出所有需要 AI 参与的配置项（如 enable\_ai\_summary: true 或 source\_mode: "ai"），并将这些子任务派发给并行的垂直领域智能体 8。  
所有智能体的输出必须受到严格的约束，采用结构化输出（Structured Output）以确保能够反序列化并安全写入下游数据表（如 sys\_resource, biz\_question, biz\_knowledge\_point）9。在 LangGraph 框架中，使用 Pydantic 模型定义工具调用和输出格式，能够有效防止大模型的幻觉，确保输出完全符合 JSON Schema 11。最终，经过 AI 丰富并挂载了真实资源 ID 的新 JSON 被固化为 standard\_payload\_json，此时 wf\_training\_template 的 ai\_status 更新为 2（已就绪），标志着实训课件准备完毕 7。

## **全局调度提示词（Master Supervisor Prompt）设计**

全局调度提示词的目标是将大模型实例化为一个“实训内容解析与调度中枢”。该模型不负责具体的教学资源生成，而是负责阅读复杂的嵌套 JSON，识别所有的 AI 处理触发器，并输出一份符合 Pydantic 规范的并行执行计划 10。  
这一环节是整个异步编排流的起点。当 Java 服务将 JSON 推送至异步消息队列并被 AI 服务（FastAPI \+ LangGraph）消费时，Supervisor Agent 首先被唤醒 1。

XML  
\<system\_role\>  
你是一个部署在企业级智能实训平台的全局调度架构师（Supervisor Agent）。你的核心职责是解析前端传入的实训编排图（JSON 格式），识别其中所有需要 AI 预处理的节点，并为各个下游执行智能体生成标准化的任务派发清单。  
\</system\_role\>

\<context\>  
当前正在执行“实训配置落库与资源预计算”的编译期任务。传入的配置是关于实训生命周期的有向无环图（DAG），包含了如“视频学习”、“导图预习”、“课后作业”等节点。  
你需要关注配置字典（config）中所有带有 \`enable\_ai\_\` 前缀的布尔值为 true 的字段，以及特定的资源生成模式（如 \`source\_mode: "ai"\`）。  
\</context\>

\<instructions\>  
1\. 深度遍历传入的 JSON 对象（特别是 \`nodes\` 数组）。  
2\. 提取需要进行 AI 内容生成的节点 ID、节点类型（node\_type）以及相关的配置参数。  
3\. 根据节点类型，将任务分类路由给以下五种下游智能体：  
   \- TEXT\_AGENT：处理欢迎语、资源摘要、重点提炼（如 START, RESOURCE\_READ）。  
   \- STRUCT\_AGENT：处理思维导图生成、任务拆解（如 MINDMAP\_PREVIEW, TASK\_DISTRIBUTE）。  
   \- EXAM\_AGENT：处理课后测验生成、理论卡片生成（如 HOMEWORK, THEORY\_CLASS）。  
   \- CODE\_AGENT：处理编程代码初始化与审查规则生成（如 CODING\_CLASS, PLAN\_UPLOAD）。  
   \- VIDEO\_AGENT：处理音视频字幕提取与章节切片（如 VIDEO\_LEARN）。  
4\. 忽略所有只需纯粹数据库写入而无需 AI 预处理的节点（如单纯的 SEMESTER\_SURVEY 学情调查，除非明确标注了由 AI 生成问卷）。  
\</instructions\>

\<output\_schema\>  
你的输出必须是严格的 JSON 格式，符合以下结构，以便下游系统进行并行任务调度（Parallel Node Execution）：  
{  
  "task\_id": "流程编排的 orchestration\_id",  
  "parallel\_jobs":  
}  
\</output\_schema\>

\<strict\_constraints\>  
\- 绝对禁止包含任何 Markdown 格式的说明文字，仅输出可解析的纯 JSON。  
\- 分析必须穷尽所有 \`nodes\`，不得遗漏任何开启了 AI 辅助开关的节点。  
\</strict\_constraints\>

通过上述调度策略，系统可以利用 LangGraph 的并行执行（Parallel Execution）能力。如果一个节点具有多个独立的生成需求，或者多个节点互不依赖，LangGraph 的图网络会在同一个超级步（Super-step）中并发激活这些目标智能体，大幅度压缩了整体处理时间 14。

## **节点级多智能体提示词与持久化架构**

依据 JSON 编排的具体节点配置，必须为每个微服务智能体定制专属的处理逻辑。每一个节点的处理都需要专属的上下文注入与结构化 Pydantic 数据类输出映射，并严格对接至 MySQL 业务表 7。以下是对 JSON 中 16 个节点的详尽解析。

### **1\. 开始实训 (START) \- node\_start\_01**

**架构意图与数据库联动**： START 节点配置了 enable\_ai\_welcome: true。在发布阶段，AI 服务需根据实训主题和学生背景，预先生成多套或单套动态欢迎语模板。这些数据并不单独建表，而是作为补充配置，回写至 wf\_node\_instance 表（实训节点实例表）的 config\_json 字段中 7。这保证了运行时前端页面加载的高效性，避免了学生进入实训时的同步等待。  
**专属提示词工程**：

XML  
\<system\_role\>资深计算机教育专家、实训课程引导员\</system\_role\>

\<task\>  
基于提供的实训背景信息，为学生编写一段极具启发性和鼓励性的个性化欢迎语。该欢迎语将在学生首次进入实训平台时通过全局悬浮 AI 弹出框展示。  
\</task\>

\<input\_data\>  
\- 实训主题：{desc} （例如：Java高并发编程实战）  
\- 学生背景画像：{student\_bg}  
\- 教师预设模板参考：{welcome\_prompt}  
\</input\_data\>

\<guidelines\>  
1\. 语气必须温暖、专业且充满激励感，拉近系统与学生的心理距离。  
2\. 精准切中学生目前的技能盲区。基于输入画像推断他们可能遇到的难点（如并发安全、死锁），并明确指出本次实训将如何帮助他们跨越这些障碍。  
3\. 强调“从需求分析到编码实现”的工程化完整生命周期，建立全局视野。  
4\. 字数严格控制在 150-250 字之间，可适当使用 Emoji 增强可读性与现代感。  
5\. 必须保留教师预设模板中的核心要求（如“阅读实训说明”）。  
\</guidelines\>

\<output\_format\>  
严格返回包含 \`generated\_welcome\_message\` 字段的 JSON 对象。  
\</output\_format\>

### **2\. 资源阅读 (RESOURCE\_READ) \- node\_resource\_01**

**架构意图与数据库联动**： 本节点挂载了三个文件（res\_doc\_001, res\_doc\_002, res\_pdf\_003），并且开启了 enable\_ai\_summary, enable\_ai\_key\_points, enable\_ai\_quick\_nav 三项高密度处理任务。 在 LangGraph 层，这里将触发 Map-Reduce 模式的并行节点执行（Parallel Node Execution）。针对资源列表中的每一个 resource\_id，系统启动一个独立的文本处理 Agent 子图 16。 生成的元数据将被关联至 sys\_resource（统一资源管理表），在资源的扩展属性中存储。当运行时记录学生进度时，详细的点击率与阅读完成度会记录在 wf\_student\_node\_progress 表的 node\_specific\_data\_json 中 7。  
**专属提示词工程**：

XML  
\<system\_role\>学术内容分析师与知识结构化专家\</system\_role\>

\<task\>  
针对用户上传的长篇教学文档（或经 OCR 提取的文本），进行深度的语义理解与结构化重组，提取符合人类认知规律的学习骨架。  
\</task\>

\<input\_document\>  
{document\_content\_extracted\_from\_resource\_id}  
\</input\_document\>

\<processing\_flags\>  
\- 提炼摘要 (enable\_ai\_summary: {enable\_ai\_summary})  
\- 提炼重点 (enable\_ai\_key\_points: {enable\_ai\_key\_points})  
\- 快捷导航 (enable\_ai\_quick\_nav: {enable\_ai\_quick\_nav})  
\</processing\_flags\>

\<instructions\>  
1\. 若开启“提炼摘要”，需在 300 字以内高度概括全文核心论点、技术原理与业务价值。  
2\. 若开启“提炼重点”，需罗列 5-7 个绝对核心的知识概念（如 Java 内存模型、CAS 原理等），每个概念附带 30-50 字的精准技术释义。  
3\. 若开启“快捷导航”，需提取文本的一级与二级逻辑标题，过滤掉无关紧要的过渡段落，输出具有逻辑递进关系的层级树状结构。  
\</instructions\>

\<json\_schema\>  
{  
  "summary": "全文摘要",  
  "key\_points": \[  
    {"concept": "概念名称", "explanation": "专业技术释义"}  
  \],  
  "navigation\_tree": \[  
    {  
      "level": 1,   
      "title": "大标题",   
      "children": \[{"level": 2, "title": "子标题"}\]  
    }  
  \]  
}  
\</json\_schema\>

### **3\. 视频观看 (VIDEO\_LEARN) \- node\_video\_01**

**架构意图与数据库联动**： 对于挂载的视频 ThreadPoolExecutor源码讲解.mp4，配置要求启用 enable\_ai\_subtitle 和 enable\_ai\_chapter。这是一个多模态集成点，内部工作流需首先调用语音识别服务（如 Whisper）提取文本，再交由大语言模型处理。 切片与字幕数据同样挂载于 sys\_resource。在学习过程中，学生的播放、暂停、回退等行为，作为高频遥测日志写入 ElasticSearch 索引，用于后续分析学生是否在某个并发原理讲解处反复观看（反映难度系数） 7。  
**专属提示词工程**：

XML  
\<system\_role\>音视频内容结构化工程师\</system\_role\>

\<task\>  
基于语音识别引擎输出的原始时间戳文本（VTT/SRT 格式），将其切分为逻辑连贯的知识点章节，并为每个章节生成简短的语义标题。  
\</task\>

\<input\_transcript\>  
{raw\_transcript\_with\_timestamps}  
\</input\_transcript\>

\<instructions\>  
1\. 梳理文本，识别视频讲解的主题切换点（例如从“线程池核心参数”切换到“拒绝策略”的时刻）。  
2\. 将整个视频划分为 4-8 个章节，每个章节长度不低于 2 分钟，不高于 15 分钟。  
3\. 提取每个章节的精确起始与结束时间戳。  
4\. 为每个章节生成一个不多于 15 个字符的精准标题。  
\</instructions\>

\<output\_schema\>  
{  
  "chapters":  
}  
\</output\_schema\>

### **4\. 导图预习 (MINDMAP\_PREVIEW) \- node\_mindmap\_preview\_01**

**架构意图与数据库联动**： 本节点配置 enable\_ai\_generate\_map: true，主题为“Java并发编程知识体系”。这里包含着极高的系统工程价值：AI 生成的导图不仅用于前端 UI 渲染，其每一个子节点都将被解析并注册到 biz\_knowledge\_point（标准知识点字典表）中 7。 这一设计建立了整个实训课程的“知识标准坐标系”。解决了主观发散无法客观聚合的痛点，确保后续的测验、提问和代码审查都能映射到这个坐标系上，从而在最终环节生成准确的全班知识掌握度雷达图。  
**专属提示词工程**：

XML  
\<system\_role\>知识图谱架构师与认知科学专家\</system\_role\>

\<task\>  
根据指定的核心场景与主题，构建一份符合脑科学发散性记忆法则的思维导图拓扑结构。该结构不仅用于视觉展示，还将作为系统底层的本体（Ontology）数据库集。  
\</task\>

\<input\>  
\- 导图核心主题：{scenario}  
\- 最大节点数量限制：{max\_nodes}  
\</input\>

\<instructions\>  
1\. 必须以提供的“核心主题”为 Root 节点。  
2\. 围绕主题向外延伸出 4-6 个核心一级分支（如：线程基础、锁机制、并发容器、线程池、Fork/Join 框架）。  
3\. 继续向二级、三级分支延伸，必须涵盖底层原理（如 AQS、CAS、可见性、指令重排）与实际应用层（如 ConcurrentHashMap、CountDownLatch）。  
4\. 深度控制在 3-4 层。节点总数严格控制在 {max\_nodes} 以内。  
5\. 每个节点的命名要求简练、精准（严格控制在 15 个中文字符以内），避免冗长的解释性短语。  
\</instructions\>

\<output\_schema\>  
必须返回严格符合前端拓扑绘制组件的 JSON 格式：  
{  
  "root": "Java并发编程知识体系",  
  "children":  
    }  
  \]  
}  
\</output\_schema\>

### **5\. 学情调查 (SEMESTER\_SURVEY) \- node\_survey\_01**

**架构意图与数据库联动**： 配置中未开启 AI 生成题目的选项，所有题目为静态配置。因此，在调度阶段，Supervisor Agent 会直接跳过此节点，不对其进行 AI 派发。 该节点配置在运行时将被完整保存在 wf\_node\_instance，而学生的调查作答结果将更新至 wf\_student\_node\_progress 表中的特定 JSON 字段，供教师实时查阅，掌握学生的前置摸底状况 7。

### **6\. 导图绘制 (MINDMAP\_DRAW) \- node\_mindmap\_draw\_01**

**架构意图与数据库联动**： 与节点 4 不同，此节点 enable\_ai\_generate\_map 为 false，提供了一个初始模板 base\_map\_data（电商秒杀系统）。 这里 AI 的主要职责在运行期（评估学生绘制的导图），但在发布期，AI 需要对这个初始骨架建立评估模型基准。学生绘制提交后的拓扑 JSON，会直接写入 biz\_mindmap\_record（思维导图实训产出表）7。直接以 JSON 格式存储图元，极大地提升了数据库读写性能，避免了无限极分类表的递归查询灾难。  
**运行期 AI 评估提示词预设（构建期落库）**：

XML  
\<system\_role\>系统级架构评审员\</system\_role\>

\<task\>  
基于给定的“电商秒杀系统”初始思维导图，建立自动化评分模型，以评估后续学生对其进行的扩展和完善。  
\</task\>

\<input\_base\_map\>  
{base\_map\_data}  
\</input\_base\_map\>

\<instructions\>  
梳理该架构必须包含的扩展知识锚点。例如，在“库存管理”下，除了原有的乐观锁和 Redis 缓存，优秀的扩展还应包含“预扣减逻辑”、“本地缓存结合”、“Lua 脚本原子性”等。请输出用于运行时自动对比评分的核心锚点列表及权重。  
\</instructions\>

### **7\. AI 对练 (AI\_PRACTICE) \- node\_ai\_practice\_01**

**架构意图与数据库联动**： 这是一个典型的多轮对话互动节点。配置了 knowledgePoint（考核知识点）和 pass\_criteria（通关标准）。 在实训进行时，每一次学生的对话都被完整收敛在 biz\_ai\_session（AI 对话会话主表）与 biz\_ai\_message（消息明细表）中。该设计彻底解耦了宏观会话状态与单条消息微观数据，防止了大模型 RAG（检索增强生成）被长文本污染，并实现了精确的 Token 成本核算与历史追溯 7。 在发布期，系统需要通过大语言模型自动生成一套符合该通关标准的 System Prompt，写入节点实例配置中，以便运行时实例化面试官 Agent。  
**专属提示词工程（Prompt for Prompt）**：

XML  
\<system\_role\>智能体人格构造师 (Prompt Framework Generator)\</system\_role\>

\<task\>  
为系统中的“AI 面试官”智能体编写一段严厉、专业且旨在循循善诱的 System Prompt。  
\</task\>

\<input\>  
\- 考核知识点：{knowledgePoint}  
\- 通关最低标准：{pass\_criteria}  
\- 最大对话轮数：{max\_rounds}  
\- 启用难度自适应：{enable\_adaptive\_difficulty}  
\</input\>

\<instructions\>  
你需要生成一段完整的系统指令（包含给 AI 面试官的角色设定、交互规则、终止条件）。  
该指令必须要求面试官在不超过 {max\_rounds} 轮对话内，采用苏格拉底式提问法（Socratic method），步步紧逼地测试学生对知识点的掌握。  
禁止直接告诉学生答案，而是通过反问（如：“如果系统是 IO 密集型，按你的公式线程池会不会耗尽内存？”）来逼迫学生思考。  
如果学生满足了通关标准，必须要求面试官在回复的末尾输出特殊标记 \`\` 以触发系统放行。  
\</instructions\>

\<output\_schema\>  
{  
  "system\_prompt": "生成的完整 Prompt 字符串",  
  "evaluation\_logic": "用于判断学生是否通关的内部逻辑描述"  
}  
\</output\_schema\>

### **8\. 理论学习 (THEORY\_CLASS) \- node\_theory\_01**

**架构意图与数据库联动**： 针对“锁机制精讲”，要求生成 15 张微学习卡片（Flashcards）。微学习强调知识的碎片化与高频交互，避免长篇大论带来的认知过载。 由于原生的业务库并未对“闪卡”独立建表，这种流水线式的轻量结构化数据将被序列化后存入 wf\_node\_instance 的专属 config\_json 字段内。学生在学习过程中的翻卡率、重做次数则上报并更新到高频遥测日志中 7。  
**专属提示词工程**：

XML  
\<system\_role\>认知心理学专家与微学习（Micro-learning）内容设计师\</system\_role\>

\<task\>  
基于复杂的底层源码知识，将其拆解为一组递进式、高度聚焦的交互式学习卡片流水线。  
\</task\>

\<input\>  
\- 主题：{topic}  
\- 覆盖知识点：{knowledge\_points}  
\- 卡片数量要求：严格为 {card\_count} 张  
\</input\>

\<instructions\>  
1\. 卡片流必须遵循人类认知规律：抛出问题 \-\> 启发思考 \-\> 核心原理剖析 \-\> 代码/流程图解 \-\> 记忆口诀。  
2\. 涵盖从偏向锁、轻量级锁到重量级锁的锁膨胀升级全过程。  
3\. 涵盖 AQS 抽象队列同步器的双向链表与 Node 节点状态。  
4\. 正面（front\_content）应具悬念，背面（back\_content）必须透彻，允许使用简明的伪代码或 Markdown 流程图。  
\</instructions\>

\<output\_schema\>  
{  
  "flashcards":  
}  
\</output\_schema\>

### **9\. 任务下发 (TASK\_DISTRIBUTE) \- node\_task\_01**

**架构意图与数据库联动**： 本节点不仅提供了需求文档，还启用了 enable\_ai\_task\_split: true。其目的是在学生查收任务时，AI 能将宏大的业务目标（设计并实现秒杀系统的下单接口）拆解为里程碑式的执行步骤。任务单的基本要素会直接落库到节点实例属性中，而学生的接单响应时间和查阅拆解指引的操作频率，将被作为过程考核数据监控记录在 wf\_student\_node\_progress 表中 7。  
**专属提示词工程**：

XML  
\<system\_role\>敏捷开发 Scrum Master 兼高级技术经理\</system\_role\>

\<task\>  
对学生收到的宏大系统开发任务进行架构级别的逻辑拆解（WBS \- Work Breakdown Structure），为学生提供一条切实可行的研发路径指引。  
\</task\>

\<input\>  
\- 任务标题：{task\_title}  
\- 任务描述：{task\_desc} (含性能要求：200ms RT, 5000 QPS)  
\</input\>

\<instructions\>  
1\. 将需求转化为 5-7 个关键研发里程碑（例如：数据模型与 Redis 结构设计、线程池与异步策略初始化、MQ 发送与消费端设计、接口防刷与限流设计、压测与参数调优）。  
2\. 对于每个步骤，明确指出潜在的技术陷阱（如：Redis 与 DB 的缓存一致性问题，线程池拒绝策略的选择）。  
\</instructions\>

\<output\_schema\>  
{  
  "task\_breakdown":  
}  
\</output\_schema\>

### **10\. 需求上传 (REQ\_UPLOAD) \- node\_req\_upload\_01**

**架构意图与数据库联动**： 学生需要在这一环节提交需求分析报告，涉及 500 字的长度限制和架构图描述。 在后台的持久化设计上，学生提交的内容会作为物料存储在 biz\_training\_upload（实训产出上传物表）中 7。发布期 AI 的任务主要是预先配置好动态文本校验规则和词云过滤机制，并写入该节点。运行时将分析这些数据并展示全班“动态词云图”。

### **11\. 方案上传 (PLAN\_UPLOAD) \- node\_plan\_upload\_01**

**架构意图与数据库联动**： 在这个节点，学生将上传包含系统架构、数据库设计与部署方案在内的多种格式的技术文档。配置中 enable\_ai\_pre\_evaluation: true 意味着需要由 AI 提供初步评分和批改。 这里使用了典型的 EAV（实体-属性-值）评价体系架构。在发布期，AI 首先根据要求生成评价指标字典（Rubrics），并持久化到 dim\_eval\_indicator（评价指标字典表）中 7。当运行时学生提交文档后，触发并行的文档解析流水线，得到的评价结果则作为事实数据落入窄表 fact\_eval\_result（评价事实结果表）中，便于最终环节的雷达图聚合计算。  
**专属提示词工程（构建期生成评价指标）**：

XML  
\<system\_role\>资深系统架构评审委员会主席\</system\_role\>

\<task\>  
根据给定的技术方案提交要求，制定一份严密且可量化的“架构文档 AI 自动化审查评分标准（Rubric）”。  
\</task\>

\<input\>  
\- 上传具体要求：{upload\_requirements} (包含架构设计、选型理由、接口设计等)  
\</input\>

\<instructions\>  
梳理出 4-5 个核心评价维度。为每个维度提供极度精确的、供后续大模型自动打分时使用的判定依据（Calc Prompt）。  
判定依据必须包含：考察重点是什么？什么情况得满分？什么情况扣除对应分值？如果缺失某个核心图表（如时序图），应该产生何种惩罚。  
\</instructions\>

\<output\_schema\>  
{  
  "indicators":  
}  
\</output\_schema\>

### **12\. 课后作业 (HOMEWORK) \- node\_homework\_01**

**架构意图与数据库联动**：  
此节点是数据生成最繁重的一环。配置项表明来源模式为纯 AI 生成（source\_mode: "ai"），且指定了严格的题目类型计数（单选 10、多选 5、填空 5 等）及难度层级（Medium）。  
这里的数据持久化流程必须严密设计：

1. 大模型生成的试题首先通过结构化输出约束，确保选项和题干完全分离。  
2. 生成后，每一道试题独立存入 biz\_question（题目明细表），标注为 AI 录入，并与其相应的 biz\_knowledge\_point 对应 7。  
3. 随后，系统组合这些题目，生成试卷记录 biz\_training\_paper（实训卷），并通过中间表 biz\_training\_paper\_question 将试题快照映射到该试卷 7。  
4. 这种解耦机制（试卷-题库防腐层）保障了在以后题目被意外修改时，历史考试记录的成绩和单题对错明细（biz\_student\_answer\_detail）不会受到破坏。

**专属提示词工程**：

XML  
\<system\_role\>高级命题专家与技术面试官\</system\_role\>

\<task\>  
本次实训已进入考核阶段。请根据预设的题型分布和难度矩阵，自动生成一套具有高度区分度、实战背景浓厚的“高并发与秒杀系统”测验题库。  
\</task\>

\<configuration\>  
\- 难度级别：{difficulty\_level}  
\- 题型约束：单选题 {single\_choice} 道，多选题 {multi\_choice} 道，填空题 {fill\_blank} 道，判断题 {true\_false} 道，简答题 {essay} 道。  
\</configuration\>

\<domain\_scope\>  
知识点必须覆盖：线程池工作机制、JUC 核心类原理、各种锁优化机制、Redis 缓存穿透/击穿/雪崩解决方案、MQ 消息防丢失与削峰策略。  
题目应摒弃简单的死记硬背，大量采用代码片段纠错或生产故障场景复盘的形式。  
\</domain\_scope\>

\<formatting\_rules\>  
必须输出为严格结构化的 JSON 数组，每个对象代表一道题。由于系统将直接映射至数据库 \`biz\_question\` 表，请确保字段格式完全对齐：  
\- \`question\_type\`: 枚举值 1(单选) 2(多选) 3(填空) 4(判断) 5(简答)  
\- \`content\`: 必须包含题干（键名为 "stem"）和选项（键名为 "options"，仅对于选择题）。整个 content 是序列化的字符串。  
\- \`standard\_answer\`: 标准答案纯文本。多选题为选项组合（如 "A,C"），简答题为评分要点。  
\- \`default\_score\`: 基础分值设定。  
\</formatting\_rules\>

\<json\_example\>

\</json\_example\>

### **13\. 编码实训 (CODING\_CLASS) \- node\_coding\_01**

**架构意图与数据库联动**： 节点不仅提供了初始化代码（init\_code），还要求支持代码审查（enable\_code\_review: true）以及分模式的 AI 辅助（guide 模式）。 运行时的代码提交，不论是成功通过编译的还是含有漏洞的，均存入 biz\_training\_upload，物料类型为 2（代码）。相关的 AI 初评分数及漏洞检测评语将记入 ai\_score 与 ai\_comment 中。测试通过率与有效编码时间占比则汇入遥测日志系统 7。  
在发布阶段，AI 智能体需要为这个具体的 SeckillService.createOrder 方法生成定制化的代码漏洞静态分析约束（AST层级的逻辑审查点）。  
**专属提示词工程**：

XML  
\<system\_role\>资深 Java 性能调优专家与代码安全审计员\</system\_role\>

\<task\>  
基于给定的秒杀系统初始代码结构，预配置针对该业务场景的代码逻辑审查规范（Review Checkpoints）。  
\</task\>

\<input\_code\>  
{init\_code}  
\</input\_code\>

\<instructions\>  
考虑到业务场景（高并发秒杀，5000QPS），分析学生在实现 \`createOrder\` 时可能犯的关键错误，输出一组严格的检查规则集。  
规则必须包含：  
1\. 线程池资源滥用（如在方法内部反复创建线程池）。  
2\. 同步锁域过大导致性能瓶颈（锁整个方法而不是库存扣减代码块）。  
3\. 数据库和 Redis 的操作顺序引发的数据不一致（如先删缓存还是后删缓存的区别）。  
4\. 代码的幂等性设计缺失（同一个用户的恶意重复下单未被拦截）。  
\</instructions\>

### **14\. 学生互评 (STUDENT\_PEER\_REVIEW) \- node\_student\_review\_01**

**架构意图与数据库联动**：  
互评环节的痛点在于评分的不客观以及产出物的频繁变动。系统在底层架构通过两项设计解决此痛点：

1. 采用 biz\_peer\_review\_assignment 表记录评审分配。  
2. 记录 target\_snapshot\_json，将被评人的目标代码/方案在评价当下的状态进行哈希快照锁定，实现防腐防篡改，防止被评价人中途修改作业导致“评不对板” 7。 发布阶段无需生成实质内容，但需将设定的四个评价维度（代码规范、并发安全性、性能优化、可读性与扩展性）映射至 dim\_eval\_indicator 中。同时，后台会唤醒一个常驻的 Sentiment Analysis Agent，用于在运行期异步监控极端异常分数或无意义评语。

### **15\. 教师点评 (TEACHER\_COMMENT) \- node\_teacher\_eval\_01 & 16\. 结束实训 (END) \- node\_end\_01**

**架构意图与数据库联动**： 当所有流程流转完毕，最终两个节点的核心是将碎片化的评价数据进行聚合降维。系统查阅 fact\_eval\_result 事实表中的原始分，结合 dim\_eval\_indicator 对应维度权重和学生个人的互评成就指标（如获取特定的徽章记录于 biz\_student\_achievement），由大模型进行深度综合分析。 在 END 节点，班级的宏观全链路耗时、AI 求助总频次等底层汇聚数据将转化为直观雷达图，结束生命周期并归档 7。AI 会基于个人在各个节点的停留时间（从 wf\_student\_node\_progress 表读取）与知识点评分（从 biz\_mindmap\_eval\_detail 读取），出具一份定制化的实训报告 PDF。

## **结论与系统价值重塑**

基于多智能体 LangGraph 工作流与结构化 Pydantic 约束构建的提示词工程架构，彻底改变了实训系统的运作机制。通过将复杂的单一巨型 JSON 转化为一组在底层并行执行的 Agentic 任务簇，极大缩短了前端交互的等待耗时 4。同时，深度耦合底层基于 EAV 范式的防腐数据库架构（如 biz\_training\_paper 与 biz\_peer\_review\_assignment 的快照机制），从根源上杜绝了脏数据污染和状态竞态 7。这一整套技术栈不仅实现了大语言模型生成能力的业务级整合，更为智能教育平台提供了一个具备高扩展性与极强稳健性的标杆实践范式。

#### **引用的著作**

1. Building Production-Ready AI Agent Services: FastAPI \+ LangGraph Template Deep Dive, 访问时间为 五月 26, 2026， [https://ranjankumar.in/building-production-ready-ai-agent-services-fastapi-langgraph-template-deep-dive](https://ranjankumar.in/building-production-ready-ai-agent-services-fastapi-langgraph-template-deep-dive)  
2. Aidemy: Building Multi-Agent Systems with LangGraph, EDA, and Generative AI on Google Cloud, 访问时间为 五月 26, 2026， [https://codelabs.developers.google.com/aidemy-multi-agent/instructions](https://codelabs.developers.google.com/aidemy-multi-agent/instructions)  
3. LangGraph: Multi-Agent Workflows \- LangChain, 访问时间为 五月 26, 2026， [https://www.langchain.com/blog/langgraph-multi-agent-workflows](https://www.langchain.com/blog/langgraph-multi-agent-workflows)  
4. Build multi-agent systems with LangGraph and Amazon Bedrock | Artificial Intelligence, 访问时间为 五月 26, 2026， [https://aws.amazon.com/blogs/machine-learning/build-multi-agent-systems-with-langgraph-and-amazon-bedrock/](https://aws.amazon.com/blogs/machine-learning/build-multi-agent-systems-with-langgraph-and-amazon-bedrock/)  
5. Building Multi-Agent Systems with LangGraph | by Clearwater Analytics Engineering, 访问时间为 五月 26, 2026， [https://medium.com/cwan-engineering/building-multi-agent-systems-with-langgraph-04f90f312b8e](https://medium.com/cwan-engineering/building-multi-agent-systems-with-langgraph-04f90f312b8e)  
6. LangGraph: Agent Orchestration Framework for Reliable AI Agents \- LangChain, 访问时间为 五月 26, 2026， [https://www.langchain.com/langgraph](https://www.langchain.com/langgraph)  
7. 编排节点.xlsx  
8. \[Hitachi Digital Services\] Demo on LangGraph-based Lightweight Multi-Agent System, 访问时间为 五月 26, 2026， [https://github.com/TheQuantScientist/Multi-Agent](https://github.com/TheQuantScientist/Multi-Agent)  
9. Structured output \- Docs by LangChain, 访问时间为 五月 26, 2026， [https://docs.langchain.com/oss/python/langchain/structured-output](https://docs.langchain.com/oss/python/langchain/structured-output)  
10. LangGraph — Structuring LLM Tool Calls with Pydantic and JSON Serialization \- Medium, 访问时间为 五月 26, 2026， [https://medium.com/@shuv.sdr/langgraph-structuring-llm-tool-calls-with-pydantic-and-json-serialization-1715f7a0c2e0](https://medium.com/@shuv.sdr/langgraph-structuring-llm-tool-calls-with-pydantic-and-json-serialization-1715f7a0c2e0)  
11. Built with LangGraph\! \#3: Structured Outputs | by Okan Yenigün | Towards Dev \- Medium, 访问时间为 五月 26, 2026， [https://medium.com/@okanyenigun/built-with-langgraph-3-structured-outputs-4707284be57e](https://medium.com/@okanyenigun/built-with-langgraph-3-structured-outputs-4707284be57e)  
12. Generate Structured Output in AI Agents Using Prebuilt LangGraph APIs \- YouTube, 访问时间为 五月 26, 2026， [https://www.youtube.com/watch?v=3Q31aObRBMo](https://www.youtube.com/watch?v=3Q31aObRBMo)  
13. Clarification on how Pydantic schema descriptions are used in with\_structured\_output, 访问时间为 五月 26, 2026， [https://forum.langchain.com/t/clarification-on-how-pydantic-schema-descriptions-are-used-in-with-structured-output/1612](https://forum.langchain.com/t/clarification-on-how-pydantic-schema-descriptions-are-used-in-with-structured-output/1612)  
14. Graph API overview \- Docs by LangChain, 访问时间为 五月 26, 2026， [https://docs.langchain.com/oss/python/langgraph/graph-api](https://docs.langchain.com/oss/python/langgraph/graph-api)  
15. Building Parallel Workflows with LangGraph: A Practical Guide | by Manu Francis | GoPenAI, 访问时间为 五月 26, 2026， [https://blog.gopenai.com/building-parallel-workflows-with-langgraph-a-practical-guide-3fe38add9c60](https://blog.gopenai.com/building-parallel-workflows-with-langgraph-a-practical-guide-3fe38add9c60)  
16. Parallel Node Execution in LangGraph: A Hands-on Guide with Arithmetic Operations, 访问时间为 五月 26, 2026， [https://sangeethasaravanan.medium.com/parallel-node-execution-in-langgraph-a-hands-on-guide-with-arithmetic-operations-9053865a7473](https://sangeethasaravanan.medium.com/parallel-node-execution-in-langgraph-a-hands-on-guide-with-arithmetic-operations-9053865a7473)  
17. Parallelization of nodes in LangGraph Explained \- YouTube, 访问时间为 五月 26, 2026， [https://www.youtube.com/watch?v=jIPwAHopS3w](https://www.youtube.com/watch?v=jIPwAHopS3w)