-- 开启向量插件
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建教学任务表
CREATE TABLE IF NOT EXISTS training_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    dag_config JSONB NOT NULL, -- 存储前端导出的 React Flow JSON
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);