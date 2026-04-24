# 编码标准与结构技能规范

当你在为特定的架构层生成代码时，必须严格套用以下结构模板：

## 1. 类型定义层 (`services/types/*.types.ts`)
- 使用 `interface` 关键字定义对象结构，不要使用 `type`。
- 将相关的 Request (请求) 和 Response (响应) 接口按业务分组写在一起。
- 涉及到列表查询的接口，必须包含标准的 Pagination (分页) 结构。

## 2. 服务层 (`services/modules/*.service.ts`)
- 必须导入项目中已封装好的单例 Axios 实例（从 `@/services/api` 导入）。
- 每个方法都必须返回一个带有明确类型的 `Promise`。
```typescript
import http from '@/services/api'
import type { CreateReq, CreateRes } from '../types/training.types'

export const TrainingService = {
  createTraining: (data: CreateReq): Promise<CreateRes> => {
    return http.post('/api/v1/training/create', data)
  }
}