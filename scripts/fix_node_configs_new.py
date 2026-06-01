"""
Fix node config_json for demo task 2061117992095911937

The seed_training_data.py script stores flat data structures, but Vue components
expect nested structures. This script transforms each node's config_json to match
the component's expected format.

Component → Expected Config Path:
  MindMapPreview.vue  → display.map_title, display.map_data
  TheoryClassView.vue → slides[] (with proper QuizQuestion format)
  MindMapEditor.vue   → display.topic, display.initial_nodes
  TaskBoard.vue       → display.task_title, display.task_description, display.deadline
                        data_collection.sub_tasks[{id, name, description, assignee, status}]
  StudentCodingLab.vue → coding_task{title, description, code_template, hints, test_cases, language}
  PlanUpload.vue       → display.title, ai_processing.enable_ai_feasibility
  HomeworkEngine.vue   → questions[{id, type, title, options[{label, value}]}]
"""

import pymysql
import json
import copy

conn = pymysql.connect(
    host='127.0.0.1', port=3306, user='root', password='123456',
    database='ai-smartconstruct-lab', charset='utf8mb4'
)
cursor = conn.cursor()

TASK_ID = 2061117992095911937

def transform_mindmap_preview(config):
    """MindMapPreview.vue expects: { display: { map_title?, map_data? } }"""
    tree_data = config.get('tree_data', config.get('display', {}).get('map_data'))
    scenario = config.get('scenario', '')

    # If already in correct format, skip
    if 'display' in config and 'map_data' in config.get('display', {}):
        return config

    return {
        'display': {
            'map_title': scenario or 'Python 数据结构知识图谱',
            'map_data': tree_data
        }
    }

def transform_theory_class(config):
    """TheoryClassView.vue expects: { slides: Slide[] }
    Slide type has:
      - intro/theory: type, title, content, bullet_points[]
      - code: type, title, content, code, output
      - quiz: type, title, content, questions[{question, options, answer}]
      - summary: type, title, content, key_points[]
    """
    slides = config.get('slides', [])
    if not slides:
        return config

    transformed = []
    for slide in slides:
        new_slide = {
            'type': slide['type'],
            'title': slide.get('title', ''),
            'content': slide.get('content', ''),
        }

        if slide['type'] in ('intro', 'theory'):
            # Seed has key_points, component expects bullet_points
            new_slide['bullet_points'] = slide.get('key_points', [])

        elif slide['type'] == 'code':
            new_slide['code'] = slide.get('code', '')
            new_slide['output'] = slide.get('output', '')

        elif slide['type'] == 'quiz':
            # Seed has flat {question, options, correct_index, explanation}
            # Component expects {questions: [{question, options, answer}]}
            seed_question = slide.get('question', '')
            seed_options = slide.get('options', [])
            correct_idx = slide.get('correct_index', 0)

            new_slide['questions'] = [{
                'question': seed_question,
                'options': seed_options,
                'answer': seed_options[correct_idx] if seed_options and correct_idx < len(seed_options) else ''
            }]

        elif slide['type'] == 'summary':
            new_slide['key_points'] = slide.get('key_points', [])

        transformed.append(new_slide)

    return {'slides': transformed}

def transform_mindmap_draw(config):
    """MindMapEditor.vue expects: { display: { topic? }, ai_processing: { enable_ai_structure_eval? } }
    Note: Component has hardcoded fallbacks, this is mostly for data completeness.
    """
    if 'display' in config:
        return config

    return {
        'display': {
            'topic': config.get('topic', config.get('scenario', 'Python数据结构思维导图')),
            'initial_nodes': config.get('initial_nodes', [])
        },
        'ai_processing': {
            'enable_ai_structure_eval': config.get('enable_ai_generate_map', True)
        }
    }

def transform_task_distribute(config):
    """TaskBoard.vue expects:
    { display: { task_title?, task_description?, deadline? },
      data_collection: { sub_tasks?: [{id, name, description, assignee, status}] } }
    """
    if 'display' in config:
        return config

    raw_sub_tasks = config.get('sub_tasks', [])
    sub_tasks = []
    for i, st in enumerate(raw_sub_tasks):
        sub_tasks.append({
            'id': st.get('id') or f't{i+1}',
            'name': st.get('name', ''),
            'description': st.get('desc', st.get('description', '')),
            'assignee': st.get('assignee', ''),
            'status': st.get('status', 'pending')
        })

    return {
        'display': {
            'task_title': config.get('task_title', 'Python算法实践项目'),
            'task_description': config.get('task_desc', config.get('task_description', '')),
            'deadline': config.get('deadline', '2026-07-30')
        },
        'data_collection': {
            'sub_tasks': sub_tasks
        }
    }

def transform_coding_class(config):
    """StudentCodingLab.vue expects: { coding_task: { title, description, code_template, hints, test_cases, language } }
    Seed has coding_task with starter_code instead of code_template.
    """
    coding_task = config.get('coding_task', config)

    # If already has code_template, skip transform
    if coding_task.get('code_template'):
        return config

    test_cases = coding_task.get('test_cases', [])
    tc_transformed = []
    for i, tc in enumerate(test_cases):
        tc_transformed.append({
            'description': tc.get('description', f'测试用例 {i+1}'),
            'input': str(tc.get('input', '')),
            'expected': str(tc.get('expected', ''))
        })

    new_task = {
        'title': coding_task.get('title', '编码实训'),
        'description': coding_task.get('description', coding_task.get('desc', '')),
        'code_template': coding_task.get('starter_code', coding_task.get('code_template', '')),
        'hints': coding_task.get('hints', []),
        'test_cases': tc_transformed,
        'language': coding_task.get('language', 'python')
    }

    # If config was already wrapped in coding_task
    if 'coding_task' in config:
        config['coding_task'] = new_task
        return config

    return {'coding_task': new_task}

def transform_plan_upload(config):
    """PlanUpload.vue expects: { display: { title? }, ai_processing: { enable_ai_feasibility? } }
    Most data comes via Pinia store API calls.
    """
    if 'display' in config:
        return config

    return {
        'display': {
            'title': config.get('title', '方案文档上传')
        },
        'data_collection': {
            'upload_requirements': config.get('upload_requirements', ''),
            'allowed_formats': config.get('allowed_formats', ['pdf']),
            'max_size_mb': config.get('max_size_mb', 100)
        },
        'ai_processing': {
            'enable_ai_feasibility': config.get('enable_ai_pre_evaluation', True),
            'evaluation_dimensions': config.get('evaluation_dimensions', [])
        }
    }

def transform_homework(config):
    """HomeworkEngine.vue expects:
    { questions?: [{ id, type, title, options: [{label, value}] }], ai_grading_enabled?: boolean }

    Seed has: questions[{ id, type, title, question, options: string[], correct }]
    """
    if 'questions' not in config:
        return config

    raw_questions = config['questions']

    # Check if already in correct format
    if raw_questions and isinstance(raw_questions[0].get('options', []), list):
        if raw_questions[0]['options'] and isinstance(raw_questions[0]['options'][0], dict):
            if 'label' in raw_questions[0]['options'][0]:
                return config

    transformed = []
    for q in raw_questions:
        new_q = {
            'id': int(hash(str(q.get('id', ''))) % 10000) if isinstance(q.get('id'), str) else (q.get('id') if q.get('id') else 0),
            'type': q.get('type', 'single'),
            'title': q.get('question', q.get('title', '')),  # Use question field as title
        }

        # Transform options from string[] to {label, value}[]
        raw_options = q.get('options', [])
        if q['type'] == 'judge' and not raw_options:
            raw_options = ['正确', '错误']

        if raw_options:
            labels = 'ABCDEFGH'
            new_q['options'] = [
                {'label': f'{labels[i] if i < len(labels) else i}. {opt}', 'value': opt}
                for i, opt in enumerate(raw_options)
            ]

        transformed.append(new_q)

    return {
        'questions': transformed,
        'ai_grading_enabled': config.get('ai_grading_enabled', True),
        'title': config.get('title', '课后作业'),
        'source_mode': config.get('source_mode', 'ai'),
        'time_limit_mins': config.get('time_limit_mins', 30),
        'difficulty_level': config.get('difficulty_level', 'medium')
    }

# Mapping of node_type to transform function
TRANSFORMS = {
    'mindmap_preview': transform_mindmap_preview,
    'theory_class': transform_theory_class,
    'mindmap_draw': transform_mindmap_draw,
    'task_distribute': transform_task_distribute,
    'coding_class': transform_coding_class,
    'plan_upload': transform_plan_upload,
    'homework': transform_homework,
}

# Fetch all node instances for the task
cursor.execute(
    "SELECT id, node_type, node_name, config_json FROM wf_node_instance WHERE task_id = %s ORDER BY sort_num",
    [TASK_ID]
)
nodes = cursor.fetchall()

print(f"Found {len(nodes)} node instances for task {TASK_ID}")

for node_id, node_type, node_name, config_json in nodes:
    if not config_json:
        print(f"  SKIP {node_type} ({node_name}): config_json is empty")
        continue

    # Parse config
    if isinstance(config_json, str):
        config = json.loads(config_json)
    else:
        config = config_json

    print(f"\n  Processing {node_type} ({node_name}) - ID={node_id}")

    # Check if it's a phase-start/phase-end node that doesn't need transform
    # Transform only nodes that have a registered transform function
    transform_fn = TRANSFORMS.get(node_type)
    if not transform_fn:
        print(f"    No transform needed for type '{node_type}'")
        continue

    try:
        old_config_str = json.dumps(config, ensure_ascii=False)
        new_config = transform_fn(config)

        # Check if anything actually changed
        new_config_str = json.dumps(new_config, ensure_ascii=False)
        if old_config_str != new_config_str:
            # Update the database
            update_json = json.dumps(new_config, ensure_ascii=False)
            cursor.execute(
                "UPDATE wf_node_instance SET config_json = %s, updated_at = NOW() WHERE id = %s",
                [update_json, node_id]
            )
            print(f"    UPDATED: config_json transformed")
        else:
            print(f"    No change needed")

    except Exception as e:
        print(f"    ERROR: {e}")
        import traceback
        traceback.print_exc()

conn.commit()

# Verify
print("\n" + "=" * 60)
print("VERIFICATION")
print("=" * 60)

cursor.execute(
    "SELECT id, node_type, node_name, config_json FROM wf_node_instance WHERE task_id = %s ORDER BY sort_num",
    [TASK_ID]
)
for node_id, node_type, node_name, config_json in nodes:
    if isinstance(config_json, str):
        config = json.loads(config_json)
    else:
        config = config_json
    summary = json.dumps(config, ensure_ascii=False)
    print(f"  {node_type} ({node_name}): {len(summary)} chars")

conn.close()
print("\nAll done!")
