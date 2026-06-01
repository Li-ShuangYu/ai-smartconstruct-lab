import pymysql, json

conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', password='123456', database='ai-smartconstruct-lab', charset='utf8mb4')
cursor = conn.cursor()

TASK_ID = 2061117992095911937

# Fix 1: Start node - flow_overview_json flat array
cursor.execute("SELECT id, config_json FROM wf_node_instance WHERE task_id = %s AND node_type = 'start'", [TASK_ID])
nid, config_str = cursor.fetchone()
config = json.loads(config_str)

if 'flow_overview_json' in config and isinstance(config['flow_overview_json'], dict) and 'phases' in config['flow_overview_json']:
    config['flow_overview_json'] = config['flow_overview_json']['phases']
    cursor.execute('UPDATE wf_node_instance SET config_json = %s WHERE id = %s', [json.dumps(config, ensure_ascii=False), nid])
    print('Fixed start node %s: flow_overview_json now has %d phases' % (nid, len(config['flow_overview_json'])))
else:
    print('Start node %s: already correct or missing' % nid)

# Fix 2: Resource read node - add top-level fields
cursor.execute("SELECT id, config_json FROM wf_node_instance WHERE task_id = %s AND node_type = 'resource_read'", [TASK_ID])
nid, config_str = cursor.fetchone()
config = json.loads(config_str)

if 'resource_list' in config and len(config['resource_list']) > 0:
    first = config['resource_list'][0]
    if 'resource_name' not in config:
        config['resource_name'] = first.get('resource_name', 'teaching resource')
    if 'resource_type' not in config:
        config['resource_type'] = first.get('resource_type', 'PDF')
else:
    config.setdefault('resource_name', 'teaching resource')
    config.setdefault('resource_type', 'PDF')

config.setdefault('ai_summary', '本文档详细介绍了Python核心数据结构与算法的基本概念和实现方法，包括数组、链表、栈、队列等线性数据结构，以及排序、搜索等经典算法的原理与复杂度分析。通过阅读本文档，你将建立起扎实的数据结构理论基础，为后续的编码实训做好充分准备。')
config.setdefault('ai_key_points', [
    '数组采用连续内存存储，支持O(1)随机访问，但插入删除需要移动元素',
    '链表采用节点链式存储，支持O(1)插入删除，但不支持随机访问',
    '栈是LIFO后进先出结构，队列是FIFO先进先出结构，两者都有广泛应用',
    '排序算法分为O(n²)和O(nlogn)两类，应根据数据规模选择合适的算法',
    '理解时间复杂度和空间复杂度的权衡是算法设计的核心思想'
])

cursor.execute('UPDATE wf_node_instance SET config_json = %s WHERE id = %s', [json.dumps(config, ensure_ascii=False), nid])
print('Fixed resource_read node %s: added top-level fields' % nid)

conn.commit()
conn.close()
print('Done')