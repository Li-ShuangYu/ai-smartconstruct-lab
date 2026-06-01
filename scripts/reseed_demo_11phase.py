"""
Complete reseed of demo task 2061117992095911937 with 11-phase structure
and correct config_json formats for each Vue component.

This script:
1. Updates template phases_json to 11 phases
2. Deletes old node instances and progress
3. Creates 11 new node instances with correct data format
4. Creates progress records for all students
"""

import pymysql
import json
import datetime
import time

# Snowflake-like ID generator
SNOWFLAKE_EPOCH = 1700000000000
_snowflake_seq = 0
_snowflake_last = 0

def next_id():
    global _snowflake_seq, _snowflake_last
    t = int(time.time() * 1000) - SNOWFLAKE_EPOCH
    if t == _snowflake_last:
        _snowflake_seq += 1
    else:
        _snowflake_seq = 0
        _snowflake_last = t
    worker = 1
    return (t << 22) | (worker << 12) | (_snowflake_seq & 0xFFF)

conn = pymysql.connect(
    host='127.0.0.1', port=3306, user='root', password='123456',
    database='ai-smartconstruct-lab', charset='utf8mb4'
)
cursor = conn.cursor()

KEEP_TEMPLATE_ID = 2060290973225353218
KEEP_TASK_ID = 2061117992095911937
NOW = datetime.datetime.now()

print("=" * 60)
print("STEP 1: Update template with 11-phase structure")
print("=" * 60)

# 11-phase structure
phases = [
    {"phase_id": "phase_start", "phase_name": "开始实训", "sort_num": 1},
    {"phase_id": "phase_resource", "phase_name": "资源阅读", "sort_num": 2},
    {"phase_id": "phase_video", "phase_name": "视频观看", "sort_num": 3},
    {"phase_id": "phase_mindmap_preview", "phase_name": "导图预习", "sort_num": 4},
    {"phase_id": "phase_theory", "phase_name": "理论实训", "sort_num": 5},
    {"phase_id": "phase_mindmap_draw", "phase_name": "导图绘制", "sort_num": 6},
    {"phase_id": "phase_task", "phase_name": "任务下发", "sort_num": 7},
    {"phase_id": "phase_coding", "phase_name": "编码实训", "sort_num": 8},
    {"phase_id": "phase_upload", "phase_name": "方案上传", "sort_num": 9},
    {"phase_id": "phase_homework", "phase_name": "课后作业", "sort_num": 10},
    {"phase_id": "phase_end", "phase_name": "结束实训", "sort_num": 11}
]

phases_json_str = json.dumps(phases, ensure_ascii=False)

# ============ Build configs for each node type (CORRECT FORMAT for components) ============

def make_start_config():
    return {
        "welcome_prompt": "同学你好！欢迎进入「Python算法入门」实训。本实训将带你从零开始掌握Python核心算法与数据结构，通过理论学习、编码实践与项目演练，帮助你建立扎实的编程基础。",
        "enable_ai_welcome": True,
        "desc": "本次实训涵盖数组、链表、栈、队列、二叉树、排序算法、搜索算法等核心知识点，共分为11个阶段循序渐进地学习。",
        "student_bg": "建议具备基础编程概念，无需Python经验",
        "flow_overview_json": {
            "phases": [
                {"name": "开始实训", "duration": "5分钟", "order": 1},
                {"name": "资源阅读", "duration": "15分钟", "order": 2},
                {"name": "视频观看", "duration": "20分钟", "order": 3},
                {"name": "导图预习", "duration": "10分钟", "order": 4},
                {"name": "理论实训", "duration": "20分钟", "order": 5},
                {"name": "导图绘制", "duration": "25分钟", "order": 6},
                {"name": "任务下发", "duration": "5分钟", "order": 7},
                {"name": "编码实训", "duration": "40分钟", "order": 8},
                {"name": "方案上传", "duration": "10分钟", "order": 9},
                {"name": "课后作业", "duration": "20分钟", "order": 10},
                {"name": "结束实训", "duration": "5分钟", "order": 11}
            ]
        },
        "orchestration_settings": {"is_required": True}
    }

def make_resource_read_config():
    return {
        "resource_list": [
            {
                "resource_name": "Python数据结构与算法入门指南",
                "resource_type": "PDF",
                "file_size": "2.3MB",
                "require_full_read": 100,
                "description": "涵盖数组、链表、栈、队列等基础数据结构的Python实现与时间复杂度分析"
            },
            {
                "resource_name": "算法复杂度分析速查表",
                "resource_type": "PDF",
                "file_size": "856KB",
                "require_full_read": 80,
                "description": "常见算法的时间复杂度与空间复杂度对比表"
            },
            {
                "resource_name": "Python内置数据结构最佳实践",
                "resource_type": "PDF",
                "file_size": "1.5MB",
                "require_full_read": 90,
                "description": "list、dict、set、tuple的底层实现原理与性能对比"
            }
        ],
        "enable_ai_summary": True,
        "enable_ai_key_points": True,
        "enable_ai_quick_nav": True,
        "allow_ai_qa": True,
        "knowledge_points": [
            {"label": "数组", "color": "#6366f1", "description": "顺序存储结构，支持O(1)随机访问"},
            {"label": "链表", "color": "#8b5cf6", "description": "链式存储结构，支持O(1)插入删除"},
            {"label": "栈", "color": "#ec4899", "description": "后进先出(LIFO)结构"},
            {"label": "队列", "color": "#f59e0b", "description": "先进先出(FIFO)结构"},
            {"label": "二叉树", "color": "#10b981", "description": "层次化数据结构"},
            {"label": "哈希表", "color": "#3b82f6", "description": "键值对存储，平均O(1)查找"}
        ],
        "orchestration_settings": {"is_required": True}
    }

def make_video_learn_config():
    return {
        "video_title": "Python数据结构核心概念解析",
        "video_name": "Python数据结构与算法精讲.mp4",
        "duration_seconds": 900,
        "unlockThreshold": 80,
        "speedOptions": [1.0, 1.25, 1.5, 2.0],
        "verificationInterval": 300,
        "knowledge_points": [
            {"time": 0, "title": "课程导学与算法概述", "desc": "数据结构与算法在编程中的重要性"},
            {"time": 60, "title": "数组的底层存储原理", "desc": "连续内存分配与索引时间复杂度分析"},
            {"time": 180, "title": "链表结构与操作详解", "desc": "单向链表、双向链表的实现与常用操作"},
            {"time": 360, "title": "栈与队列的经典应用", "desc": "括号匹配、浏览器前进后退等实际场景"},
            {"time": 540, "title": "二叉树遍历与递归思想", "desc": "前序、中序、后序遍历的递归与迭代实现"},
            {"time": 720, "title": "排序算法对比与选择", "desc": "冒泡、选择、插入、快排、归并的适用场景"},
            {"time": 840, "title": "总结与课后实践指导", "desc": "核心知识回顾与拓展学习建议"}
        ],
        "orchestration_settings": {"is_required": True}
    }

def make_mindmap_preview_config():
    return {
        "display": {
            "map_title": "Python算法知识体系",
            "map_data": {
                "name": "Python算法知识体系",
                "children": [
                    {
                        "name": "数据结构基础",
                        "children": [
                            {"name": "数组(Array)\nO(1)随机访问\n连续内存空间"},
                            {"name": "链表(LinkedList)\nO(1)插入删除\n离散内存分布"},
                            {"name": "栈(Stack)\nLIFO后进先出\n递归函数调用栈"}
                        ]
                    },
                    {
                        "name": "高级数据结构",
                        "children": [
                            {"name": "二叉树(Binary Tree)\n前/中/后序遍历\n二叉搜索树BST"},
                            {"name": "堆(Heap)\n完全二叉树\n优先队列实现"},
                            {"name": "哈希表(Hash Table)\nO(1)平均查找\n哈希冲突解决"}
                        ]
                    },
                    {
                        "name": "排序算法",
                        "children": [
                            {"name": "O(n²)排序\n冒泡·选择·插入"},
                            {"name": "O(nlogn)排序\n快速排序·归并排序"},
                            {"name": "O(n)排序\n计数排序·桶排序"}
                        ]
                    },
                    {
                        "name": "算法思想",
                        "children": [
                            {"name": "递归与分治\n归并排序的分治思想"},
                            {"name": "动态规划\n最优子结构\n状态转移方程"},
                            {"name": "贪心算法\n局部最优解\n区间调度问题"}
                        ]
                    }
                ]
            }
        },
        "orchestration_settings": {"is_required": True}
    }

def make_theory_class_config():
    return {
        "slides": [
            {
                "type": "intro",
                "title": "Python数据结构总览",
                "content": "数据结构是计算机存储、组织数据的方式。选择合适的数据结构可以显著提升算法效率。Python内置了丰富的数据结构：list（动态数组）、tuple（不可变序列）、dict（哈希表）、set（集合），同时也支持通过class自定义复杂数据结构。",
                "bullet_points": [
                    "数据结构 = 数据的组织方式 + 定义在其上的操作",
                    "选择合适的数据结构是算法设计的关键一步",
                    "Python的内置数据结构已经过高度优化"
                ]
            },
            {
                "type": "theory",
                "title": "数组与链表对比分析",
                "content": "数组（Array）是最基础的数据结构，在内存中占用连续空间，支持通过索引O(1)随机访问。Python的list就是动态数组实现，当容量不足时自动扩容。\n\n链表（LinkedList）由节点组成，每个节点包含数据和指向下一节点的指针。链表的优势在于插入和删除操作只需O(1)时间（已知位置），但查找需要O(n)时间。",
                "bullet_points": [
                    "数组：随机访问快O(1)，插入删除慢O(n)",
                    "链表：插入删除快O(1)，随机访问慢O(n)",
                    "Python的list是动态数组，不是链表"
                ]
            },
            {
                "type": "code",
                "title": "链表节点实现",
                "code": "class ListNode:\n    def __init__(self, val=0, next=None):\n        self.val = val\n        self.next = next\n\n# 创建链表: 1 -> 2 -> 3\nhead = ListNode(1)\nhead.next = ListNode(2)\nhead.next.next = ListNode(3)\n\n# 遍历链表\ndef traverse(head):\n    while head:\n        print(head.val, end=' -> ')\n        head = head.next\n    print('None')\n\ntraverse(head)  # 1 -> 2 -> 3 -> None",
                "output": "1 -> 2 -> 3 -> None"
            },
            {
                "type": "quiz",
                "title": "知识测验",
                "content": "",
                "questions": [{
                    "question": "Python中list的pop(0)操作的时间复杂度是多少？",
                    "options": ["O(1)", "O(logn)", "O(n)", "O(n²)"],
                    "answer": "O(n)"
                }]
            },
            {
                "type": "summary",
                "title": "本节要点总结",
                "content": "本节课我们学习了Python中核心数据结构的基本原理和应用场景。数组和链表是最基础的两种存储结构，栈和队列是两种重要的抽象数据类型。",
                "key_points": [
                    "数组O(1)随机访问vs链表O(1)插入删除",
                    "栈LIFO特性在括号匹配中的经典应用",
                    "队列FIFO特性在BFS中的核心地位"
                ]
            }
        ]
    }

def make_mindmap_draw_config():
    return {
        "display": {
            "topic": "请绘制一张以「Python数据结构」为主题的思维导图，涵盖以下内容：\n1. 线性结构（数组、链表、栈、队列）\n2. 树形结构（二叉树、堆）\n3. 散列结构（哈希表）\n4. 每种结构的特点、时间复杂度、适用场景",
            "initial_nodes": [
                {"id": "root", "label": "Python数据结构", "x": 400, "y": 50, "parent": None},
                {"id": "n1", "label": "线性结构", "x": 120, "y": 180, "parent": "root"},
                {"id": "n2", "label": "树形结构", "x": 400, "y": 180, "parent": "root"},
                {"id": "n3", "label": "散列结构", "x": 680, "y": 180, "parent": "root"},
                {"id": "n4", "label": "数组/列表\nO(1)随机访问", "x": 30, "y": 320, "parent": "n1"},
                {"id": "n5", "label": "栈/队列\nLIFO/FIFO", "x": 210, "y": 320, "parent": "n1"},
                {"id": "n6", "label": "二叉树\n遍历与搜索", "x": 320, "y": 320, "parent": "n2"},
                {"id": "n7", "label": "堆\n优先队列", "x": 480, "y": 320, "parent": "n2"},
                {"id": "n8", "label": "哈希表\nO(1)查找", "x": 680, "y": 320, "parent": "n3"}
            ]
        },
        "ai_processing": {
            "enable_ai_structure_eval": True
        },
        "orchestration_settings": {"is_required": True}
    }

def make_task_distribute_config():
    return {
        "display": {
            "task_title": "Python算法实践项目",
            "task_description": "本次实训项目要求你使用Python实现一个「学生成绩管理系统」，运用所学数据结构和算法知识，完成以下功能模块的开发。",
            "deadline": "2026-07-30"
        },
        "data_collection": {
            "sub_tasks": [
                {"id": "t1", "name": "学生信息存储模块", "status": "completed", "assignee": "本人", "description": "使用字典存储学生基本信息（学号、姓名、成绩），支持增删改查操作"},
                {"id": "t2", "name": "成绩排序功能", "status": "in_progress", "assignee": "本人", "description": "实现至少两种排序算法（如快速排序和归并排序）对成绩进行排序"},
                {"id": "t3", "name": "成绩统计分析", "status": "in_progress", "assignee": "本人", "description": "计算平均分、最高分、最低分、方差等统计指标"},
                {"id": "t4", "name": "数据持久化存储", "status": "pending", "assignee": "本人", "description": "将学生数据保存到JSON文件，支持程序重启后加载"},
                {"id": "t5", "name": "命令行交互界面", "status": "pending", "assignee": "本人", "description": "实现菜单驱动的命令行交互界面，方便用户操作"},
                {"id": "t6", "name": "扩展功能：成绩分布可视化", "status": "pending", "assignee": "本人", "description": "使用matplotlib生成成绩分布柱状图（选做）"}
            ]
        },
        "orchestration_settings": {"is_required": True}
    }

def make_coding_class_config():
    return {
        "coding_task": {
            "title": "实现快速排序算法",
            "description": "快速排序（Quicksort）是一种高效的排序算法，采用分治策略。请根据以下要求实现快速排序：\n\n1. 实现quick_sort(arr)函数，接受整数列表并返回排序后的列表\n2. 实现partition分区函数，选择最后一个元素作为pivot\n3. 在main函数中测试你的实现\n4. 分析算法的时间复杂度",
            "language": "python",
            "hints": [
                "选择pivot后，将小于pivot的元素放在左边，大于的放在右边",
                "递归地对左右两个子数组进行排序",
                "当子数组长度≤1时，递归终止"
            ],
            "code_template": "def quick_sort(arr):\n    # 在这里实现快速排序\n    pass\n\ndef partition(arr, low, high):\n    # 实现分区函数\n    pass\n\n# 测试\nif __name__ == '__main__':\n    test_arr = [3, 6, 8, 10, 1, 2, 1]\n    print(f'排序前: {test_arr}')\n    sorted_arr = quick_sort(test_arr)\n    print(f'排序后: {sorted_arr}')",
            "test_cases": [
                {"description": "常规测试", "input": "[3, 6, 8, 10, 1, 2, 1]", "expected": "[1, 1, 2, 3, 6, 8, 10]"},
                {"description": "单元素", "input": "[1]", "expected": "[1]"},
                {"description": "空数组", "input": "[]", "expected": "[]"},
                {"description": "逆序情况", "input": "[5, 4, 3, 2, 1]", "expected": "[1, 2, 3, 4, 5]"}
            ]
        },
        "orchestration_settings": {"is_required": True}
    }

def make_plan_upload_config():
    return {
        "display": {
            "title": "方案文档上传"
        },
        "data_collection": {
            "upload_requirements": "请上传你的学生成绩管理系统的设计方案，包含以下内容：\n1. 系统架构设计（模块划分与数据流）\n2. 数据结构选择理由\n3. 核心算法设计说明\n4. 测试方案",
            "allowed_formats": ["pdf", "txt", "word", "ppt", "md"],
            "max_size_mb": 100
        },
        "ai_processing": {
            "enable_ai_feasibility": True,
            "evaluation_dimensions": [
                {"name": "方案完整性", "description": "设计方案是否覆盖所有功能模块"},
                {"name": "数据结构合理性", "description": "所选数据结构是否符合性能要求"},
                {"name": "算法正确性", "description": "核心算法逻辑是否正确无误"},
                {"name": "文档规范性", "description": "文档格式是否规范、清晰"}
            ]
        },
        "orchestration_settings": {"is_required": True}
    }

def make_homework_config():
    return {
        "title": "课后作业：数据结构综合练习",
        "source_mode": "ai",
        "time_limit_mins": 30,
        "difficulty_level": "medium",
        "ai_grading_enabled": True,
        "questions": [
            {
                "id": 1, "type": "single",
                "title": "以下哪种数据结构最适合实现浏览器的前进/后退功能？",
                "options": [
                    {"label": "A. 数组", "value": "A"},
                    {"label": "B. 链表", "value": "B"},
                    {"label": "C. 栈", "value": "C"},
                    {"label": "D. 队列", "value": "D"}
                ]
            },
            {
                "id": 2, "type": "multi",
                "title": "下列关于哈希表的说法中，正确的有？",
                "options": [
                    {"label": "A. 平均查找时间复杂度为O(1)", "value": "A"},
                    {"label": "B. 最坏查找时间复杂度为O(n)", "value": "B"},
                    {"label": "C. 必须使用数组作为底层存储", "value": "C"},
                    {"label": "D. 负载因子越大性能越好", "value": "D"}
                ]
            },
            {
                "id": 3, "type": "judge",
                "title": "快速排序的平均时间复杂度为O(n²)。",
                "options": [
                    {"label": "A. 正确", "value": "T"},
                    {"label": "B. 错误", "value": "F"}
                ]
            },
            {
                "id": 4, "type": "fill",
                "title": "在一个长度为n的数组中，查找一个元素的时间复杂度为 ____ 。",
                "options": []
            },
            {
                "id": 5, "type": "essay",
                "title": "请比较数组和链表的优缺点，并说明在实际开发中如何选择。",
                "options": []
            }
        ]
    }

def make_end_config():
    return {
        "end_desc": "恭喜你完成了「Python算法入门」的全部实训内容！通过本次实训，你应该已经掌握了Python核心数据结构与算法的基础知识，具备了解决实际编程问题的能力。希望你继续保持学习的热情！",
        "summary_stats": {
            "total_phases": 11,
            "total_duration": "约3小时",
            "coding_exercises": 1,
            "quiz_questions": 5
        },
        "orchestration_settings": {"is_required": False}
    }


# Node type to phase mapping and config
phase_node_map = [
    ("phase_start", "start", "开始实训", make_start_config),
    ("phase_resource", "resource_read", "资源阅读", make_resource_read_config),
    ("phase_video", "video_learn", "视频观看", make_video_learn_config),
    ("phase_mindmap_preview", "mindmap_preview", "导图预习", make_mindmap_preview_config),
    ("phase_theory", "theory_class", "理论实训", make_theory_class_config),
    ("phase_mindmap_draw", "mindmap_draw", "导图绘制", make_mindmap_draw_config),
    ("phase_task", "task_distribute", "任务下发", make_task_distribute_config),
    ("phase_coding", "coding_class", "编码实训", make_coding_class_config),
    ("phase_upload", "plan_upload", "方案上传", make_plan_upload_config),
    ("phase_homework", "homework", "课后作业", make_homework_config),
    ("phase_end", "end", "结束实训", make_end_config),
]

# Build standard_payload_json for the template
std_phases = []
for phase_id, node_type, phase_name, config_fn in phase_node_map:
    std_phases.append({
        "phase_id": phase_id,
        "phase_name": phase_name,
        "sort_num": len(std_phases) + 1,
        "nodes": [{
            "node_name": phase_name,
            "node_type": node_type,
            "sort_num": 1,
            "config": config_fn(),
            "orchestration_settings": {"is_required": True}
        }]
    })

standard_payload = {"phases": std_phases}
standard_payload_json = json.dumps(standard_payload, ensure_ascii=False)

# Update template
cursor.execute(
    "UPDATE wf_training_template SET phases_json = %s, standard_payload_json = %s, updated_at = %s WHERE id = %s",
    [phases_json_str, standard_payload_json, NOW, KEEP_TEMPLATE_ID]
)
print(f"Updated template {KEEP_TEMPLATE_ID}: phases_json={len(phases_json_str)} chars, standard_payload_json={len(standard_payload_json)} chars")

# Verify update
cursor.execute("SELECT phases_json FROM wf_training_template WHERE id = %s", [KEEP_TEMPLATE_ID])
row = cursor.fetchone()
if row:
    pj = row[0]
    if isinstance(pj, str):
        updated_phases = json.loads(pj)
    else:
        updated_phases = json.loads(json.dumps(pj))
    print(f"Template now has {len(updated_phases)} phases: {[p['phase_name'] for p in updated_phases]}")

print()
print("=" * 60)
print("STEP 2: Clean existing data for kept task")
print("=" * 60)

# Get existing participations for kept task
cursor.execute("SELECT id, student_id FROM biz_training_participation WHERE task_id = %s", [KEEP_TASK_ID])
keep_participation_records = cursor.fetchall()
keep_participation_ids = [r[0] for r in keep_participation_records]
print(f"Found {len(keep_participation_ids)} participations for kept task")

# Delete old node instances
cursor.execute("DELETE FROM wf_node_instance WHERE task_id = %s", [KEEP_TASK_ID])
print("Deleted old node instances")

# Delete progress and state records
if keep_participation_ids:
    ph = ','.join(['%s'] * len(keep_participation_ids))
    cursor.execute(f"DELETE FROM wf_student_node_progress WHERE participation_id IN ({ph})", keep_participation_ids)
    cursor.execute(f"DELETE FROM wf_student_activity_state WHERE participation_id IN ({ph})", keep_participation_ids)
    cursor.execute(f"DELETE FROM biz_mindmap_record WHERE participation_id IN ({ph})", keep_participation_ids)
    cursor.execute(f"DELETE FROM biz_training_upload WHERE participation_id IN ({ph})", keep_participation_ids)
    # Reset participations to not started
    cursor.execute(f"UPDATE biz_training_participation SET status = 0, total_score = NULL WHERE id IN ({ph})", keep_participation_ids)
    print(f"Cleaned progress/state for {len(keep_participation_ids)} participations")

print()
print("=" * 60)
print("STEP 3: Create 11 node instances with correct data format")
print("=" * 60)

created_node_ids = []

for idx, (phase_id, node_type, node_name, config_fn) in enumerate(phase_node_map):
    nid = next_id()
    sort_num = idx + 1
    config = config_fn()

    cursor.execute(
        "INSERT INTO wf_node_instance (id, task_id, phase_id, node_def_id, node_type, node_name, sort_num, config_json, status, created_at, updated_at) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        [nid, KEEP_TASK_ID, phase_id, None, node_type, node_name, sort_num, json.dumps(config, ensure_ascii=False), 0, NOW, NOW]
    )
    created_node_ids.append((nid, sort_num, phase_id, node_type, node_name))
    print(f"  Created: ID={nid}, type={node_type:20s}, name={node_name}, phase={phase_id}")

print(f"  Total: {len(created_node_ids)} node instances")

print()
print("=" * 60)
print("STEP 4: Create progress records for all students")
print("=" * 60)

progress_count = 0
for pid, student_id in keep_participation_records:
    for node_id, sort_num, phase_id, ntype, name in created_node_ids:
        prog_id = next_id()
        cursor.execute(
            "INSERT INTO wf_student_node_progress (id, participation_id, node_instance_id, status, is_forced_complete, created_at, updated_at) VALUES (%s, %s, %s, 0, 0, %s, %s)",
            [prog_id, pid, node_id, NOW, NOW]
        )
        progress_count += 1

print(f"Created {progress_count} progress records ({len(keep_participation_ids)} students × {len(created_node_ids)} nodes)")

print()
print("=" * 60)
print("VERIFICATION")
print("=" * 60)

# Show node instances
cursor.execute("SELECT id, node_type, node_name, phase_id, sort_num FROM wf_node_instance WHERE task_id = %s ORDER BY sort_num", [KEEP_TASK_ID])
print("\nNode instances:")
for r in cursor.fetchall():
    print(f"  ID={r[0]}, type={r[1]:20s}, name={r[2]}, phase={r[3]:30s}, sort={r[4]}")

# Show total counts
cursor.execute("SELECT COUNT(*) FROM wf_node_instance WHERE task_id = %s", [KEEP_TASK_ID])
print(f"\nTotal node instances: {cursor.fetchone()[0]}")

cursor.execute("SELECT COUNT(*) FROM wf_student_node_progress")
print(f"Total progress records: {cursor.fetchone()[0]}")

conn.commit()
conn.close()
print("\nAll done!")
