import pymysql, json, time, datetime

conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='123456', database='ai-smartconstruct-lab', charset='utf8mb4')
cursor = conn.cursor()

TASK_ID = 2061117992095911937
NOW = datetime.datetime.now()

cursor.execute("DELETE FROM wf_student_node_progress WHERE participation_id IN (SELECT id FROM biz_training_participation WHERE task_id = %s)", [TASK_ID])
cursor.execute("DELETE FROM wf_student_activity_state WHERE participation_id IN (SELECT id FROM biz_training_participation WHERE task_id = %s)", [TASK_ID])
cursor.execute("DELETE FROM wf_node_instance WHERE task_id = %s", [TASK_ID])

cursor.execute("UPDATE biz_training_participation SET status = 0, total_score = NULL WHERE task_id = %s", [TASK_ID])
cursor.execute("UPDATE biz_training_task SET status = 0, task_name = 'python算法入门实训任务' WHERE id = %s", [TASK_ID])

EPOCH = 1700000000000
seq = [0]
def next_id():
    seq[0] += 1
    t = int(time.time() * 1000) - EPOCH
    return (t << 22) | (1 << 12) | (seq[0] & 0xFFF)

phases = [{"phase_id": "phase_pre", "phase_name": "课前阶段", "sort_num": 1}, {"phase_id": "phase_mid", "phase_name": "课中阶段", "sort_num": 2}, {"phase_id": "phase_post", "phase_name": "课后阶段", "sort_num": 3}]

phase_nodes = {
    "phase_pre": [("start", "开始实训"), ("resource_read", "资源阅读"), ("video_learn", "视频观看"), ("mindmap_preview", "导图预习"), ("end", "结束实训")],
    "phase_mid": [("start", "开始实训"), ("theory_class", "理论实训"), ("mindmap_draw", "导图绘制"), ("task_distribute", "任务下发"), ("coding_class", "编码实训"), ("plan_upload", "方案上传"), ("end", "结束实训")],
    "phase_post": [("start", "开始实训"), ("homework", "课后作业"), ("end", "结束实训")]
}

cfg_map = {}

cfg_map["resource_read"] = {"resource_name":"Python算法入门指南","resource_type":"PDF","enable_ai_summary":True,"enable_ai_key_points":True,"ai_summary":"本文档介绍了Python核心算法与数据结构。","ai_key_points":["O(n)与O(nlogn)排序算法","递归三要素","动态规划核心"],"knowledge_points":[{"name":"时间复杂度分析"},{"name":"排序算法"},{"name":"递归"},{"name":"动态规划"}],"orchestration_settings":{"is_required":True}}

cfg_map["video_learn"] = {"video_title":"Python数据结构核心概念解析","video_name":"冒泡排序演示视频.mp4","knowledge_points":[{"time":0,"title":"课程导学","desc":"概览"},{"time":60,"title":"冒泡排序原理","desc":"相邻比较交换"},{"time":180,"title":"复杂度分析","desc":"详解"},{"time":360,"title":"代码实现","desc":"逐步实现"},{"time":720,"title":"总结","desc":"回顾"}],"orchestration_settings":{"is_required":True}}

cfg_map["mindmap_preview"] = {"display":{"map_title":"Python算法知识体系","map_data":{"id":"root","label":"Python算法知识体系","children":[{"id":"n1","label":"数据结构基础","children":[{"id":"n1a","label":"数组"},{"id":"n1b","label":"链表"},{"id":"n1c","label":"栈"},{"id":"n1d","label":"队列"}]},{"id":"n2","label":"高级数据结构","children":[{"id":"n2a","label":"二叉树"},{"id":"n2b","label":"堆"},{"id":"n2c","label":"哈希表"}]},{"id":"n3","label":"排序算法","children":[{"id":"n3a","label":"冒泡排序"},{"id":"n3b","label":"快速排序"},{"id":"n3c","label":"归并排序"}]},{"id":"n4","label":"算法思想","children":[{"id":"n4a","label":"递归与分治"},{"id":"n4b","label":"动态规划"},{"id":"n4c","label":"贪心算法"}]}]}},"orchestration_settings":{"is_required":True}}

cfg_map["theory_class"] = {"slides":[{"type":"intro","title":"Python数据结构总览","content":"数据结构是计算机存储、组织数据的方式。","key_points":["数据=组织+操作"]},{"type":"theory","title":"数组与链表对比","content":"数组连续内存O(1)随机访问。链表链式存储O(1)插入删除。","key_points":["数组随机访问快","链表插入删除快"]},{"type":"code","title":"链表实现","code":"class ListNode:\n    def __init__(self, val=0, next=None):\n        self.val = val\n        self.next = next","output":"1 -> 2 -> 3 -> None"},{"type":"quiz","title":"测验","question":"list.pop(0)时间复杂度？","options":["O(1)","O(logn)","O(n)","O(n^2)"],"correct_index":2,"explanation":"需移动元素"},{"type":"summary","title":"总结","content":"学习核心数据结构","key_points":["O(1)随机访问 vs O(1)插入删除","栈LIFO应用","队列FIFO应用"]}],"orchestration_settings":{"is_required":True}}

cfg_map["mindmap_draw"] = {"topic":"绘制Python数据结构思维导图","initial_nodes":[{"id":"root","label":"Python数据结构","x":400,"y":50,"parent":None},{"id":"n1","label":"线性结构","x":120,"y":180,"parent":"root"},{"id":"n2","label":"树形结构","x":400,"y":180,"parent":"root"},{"id":"n3","label":"散列结构","x":680,"y":180,"parent":"root"},{"id":"n4","label":"数组","x":30,"y":320,"parent":"n1"},{"id":"n5","label":"栈/队列","x":210,"y":320,"parent":"n1"},{"id":"n6","label":"二叉树","x":320,"y":320,"parent":"n2"},{"id":"n7","label":"堆","x":480,"y":320,"parent":"n2"},{"id":"n8","label":"哈希表","x":680,"y":320,"parent":"n3"}],"orchestration_settings":{"is_required":True}}

cfg_map["task_distribute"] = {"display":{"task_title":"Python算法实践项目","task_description":"实现学生成绩管理系统","deadline":"2026-07-30"},"data_collection":{"sub_tasks":[{"id":"t1","name":"学生信息存储模块","description":"字典存储","assignee":"本人","status":"completed"},{"id":"t2","name":"成绩排序","description":"快速+归并排序","assignee":"本人","status":"in_progress"},{"id":"t3","name":"成绩统计","description":"平均值方差","assignee":"本人","status":"in_progress"},{"id":"t4","name":"数据持久化","description":"JSON文件","assignee":"本人","status":"pending"},{"id":"t5","name":"交互界面","description":"菜单驱动","assignee":"本人","status":"pending"},{"id":"t6","name":"可视化","description":"matplotlib","assignee":"本人","status":"pending"}]},"orchestration_settings":{"is_required":True}}

cfg_map["coding_class"] = {"coding_task":{"title":"实现快速排序","description":"快速排序采用分治策略。","language":"python","hints":["选择pivot","小于放左大于放右","递归"],"starter_code":"def quick_sort(arr):\n    pass\n\nif __name__ == '__main__':\n    test = [3,6,8,10,1,2,1]\n    print(quick_sort(test))","test_cases":[{"input":[3,6,8,10,1,2,1],"expected":[1,1,2,3,6,8,10]}]},"orchestration_settings":{"is_required":True}}

cfg_map["plan_upload"] = {"upload_requirements":"上传学生成绩管理系统设计方案","allowed_formats":["pdf","txt","word","ppt","md"],"max_size_mb":100,"enable_ai_pre_evaluation":True,"orchestration_settings":{"is_required":True}}

cfg_map["homework"] = {"title":"课后作业：数据结构综合练习","questions":[{"id":"q1","type":"single","title":"第1题","question":"最适合浏览器前进后退功能？","options":["数组","链表","栈","队列"],"correct":2},{"id":"q2","type":"multi","title":"第2题","question":"关于哈希表正确的有？","options":["平均O(1)查找","最坏O(n)查找","必须用数组存储","负载因子越大越好"],"correct":[0,1,2]},{"id":"q3","type":"judge","title":"第3题","question":"快排平均复杂度O(n)","correct":False},{"id":"q4","type":"fill","title":"第4题","question":"数组查找时间复杂度____","answer":"O(1)"},{"id":"q5","type":"essay","title":"第5题","question":"比较数组和链表优缺点"}],"orchestration_settings":{"is_required":True}}

all_nodes = []
sort_num = 0
for phase in phases:
    pid = phase["phase_id"]
    for ntype, nname in phase_nodes[pid]:
        sort_num += 1
        nid = next_id()
        if ntype == "start":
            if sort_num == 1:
                cfg = {"ai_welcome_text":"同学你好！欢迎进入Python算法入门实训。共三个阶段：课前(阅读/视频/导图预习)、课中(理论/导图/任务/编码/方案)、课后(作业)。","flow_overview_json":[{"phase_id":"phase_pre","phase_name":"课前阶段","sort_num":1,"nodes":[{"node_id":"n1","node_name":"开始实训","node_type":"start","estimated_duration_minutes":5},{"node_id":"n2","node_name":"资源阅读","node_type":"resource_read","estimated_duration_minutes":15},{"node_id":"n3","node_name":"视频观看","node_type":"video_learn","estimated_duration_minutes":20},{"node_id":"n4","node_name":"导图预习","node_type":"mindmap_preview","estimated_duration_minutes":10}]},{"phase_id":"phase_mid","phase_name":"课中阶段","sort_num":2,"nodes":[{"node_id":"n5","node_name":"理论实训","node_type":"theory_class","estimated_duration_minutes":20},{"node_id":"n6","node_name":"导图绘制","node_type":"mindmap_draw","estimated_duration_minutes":25},{"node_id":"n7","node_name":"任务下发","node_type":"task_distribute","estimated_duration_minutes":5},{"node_id":"n8","node_name":"编码实训","node_type":"coding_class","estimated_duration_minutes":40},{"node_id":"n9","node_name":"方案上传","node_type":"plan_upload","estimated_duration_minutes":10}]},{"phase_id":"phase_post","phase_name":"课后阶段","sort_num":3,"nodes":[{"node_id":"n10","node_name":"课后作业","node_type":"homework","estimated_duration_minutes":20}]}],"orchestration_settings":{"is_required":False}}
            else:
                cfg = {"ai_welcome_text":"请按顺序完成本阶段环节。","orchestration_settings":{"is_required":False}}
        elif ntype == "end":
            if pid == "phase_post":
                cfg = {"end_desc":"恭喜完成全部实训！希望本次实训帮助你掌握了Python数据结构和算法基础。","orchestration_settings":{"is_required":False}}
            else:
                cfg = {"end_desc":"本阶段实训完成，请进入下一阶段。","orchestration_settings":{"is_required":False}}
        else:
            cfg = cfg_map.get(ntype, {})

        cursor.execute("INSERT INTO wf_node_instance (id,task_id,phase_id,node_type,node_name,sort_num,config_json,status,created_at,updated_at) VALUES (%s,%s,%s,%s,%s,%s,%s,0,%s,%s)", [nid,TASK_ID,pid,ntype,nname,sort_num,json.dumps(cfg,ensure_ascii=False),NOW,NOW])
        all_nodes.append(nid)
        print("sort=%d type=%s name=%s id=%d" % (sort_num, ntype, nname, nid & ((1<<63)-1)))

cursor.execute("SELECT id FROM biz_training_participation WHERE task_id = %s", [TASK_ID])
pids = [r[0] for r in cursor.fetchall()]
cnt = 0
for pid in pids:
    for nid in all_nodes:
        cnt += 1
        prog_id = next_id()
        cursor.execute("INSERT INTO wf_student_node_progress (id,participation_id,node_instance_id,status,is_forced_complete,created_at,updated_at) VALUES (%s,%s,%s,0,0,%s,%s)", [prog_id,pid,nid,NOW,NOW])

cursor.execute("UPDATE wf_training_template SET phases_json = %s WHERE id = 2060290973225353218", [json.dumps(phases, ensure_ascii=False)])

conn.commit()
conn.close()
print("\nDone! %d nodes, %d progress records" % (len(all_nodes), cnt))
