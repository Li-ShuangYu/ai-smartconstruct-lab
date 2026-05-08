/**
 * 工作流节点接口定义
 */
export interface FlowNode { 
  node_id: string; 
  node_type: string; 
  name: string; 
  config: Record<string, unknown> 
}

/**
 * 工作流连接线接口定义
 */
export interface FlowEdge { 
  source: string; 
  target: string 
}

/**
 * 根据当前节点ID获取下一个节点ID
 * 
 * 从连接线数组中查找以当前节点为源的连接，返回目标节点ID。
 * 
 * @param currentNodeId 当前节点ID
 * @param edges 连接线数组
 * @returns 下一节点ID，如果找不到则返回null
 */
export function getNextNodeId(currentNodeId: string, edges: FlowEdge[]): string | null {
  const edge = edges.find(e => e.source === currentNodeId)
  return edge?.target ?? null
}

/**
 * 在节点数组中查找起始节点(START)
 * 
 * @param nodes 节点数组
 * @returns 起始节点ID，如果找不到则返回null
 */
export function findStartNodeId(nodes: FlowNode[]): string | null {
  const start = nodes.find(n => normalizeType(n.node_type) === 'START')
  return start?.node_id ?? null
}

/**
 * 检查节点是否为结束节点(END)
 * 
 * @param node 节点对象
 * @returns true表示是结束节点，false表示不是
 */
export function checkIsEndNode(node: FlowNode): boolean {
  return normalizeType(node.node_type || '').includes('END')
}

/**
 * 构建有序节点序列
 * 
 * 使用深度优先搜索(DFS)从START节点开始，按照连接线的指向顺序遍历所有节点，
 * 返回一个按执行顺序排列的节点序列。
 * 
 * @param nodes 节点数组
 * @param edges 连接线数组
 * @returns 按执行顺序排列的节点数组
 */
export function buildOrderedSequence(nodes: FlowNode[], edges: FlowEdge[]): FlowNode[] {
  if (!nodes || !nodes.length) return []
  
  // 创建节点映射表，方便快速查找
  const nodeMap = new Map<string, FlowNode>()
  for (const n of nodes) nodeMap.set(n.node_id, n)

  // 构建邻接表，存储每个节点的后继节点列表
  const adj = new Map<string, string[]>()
  for (const e of edges) {
    const targets = adj.get(e.source) || []
    targets.push(e.target)
    adj.set(e.source, targets)
  }

  // 找到起始节点，若无则取第一个节点作为默认起始
  let startId = findStartNodeId(nodes)
  if (!startId && nodes.length > 0) startId = nodes[0]?.node_id ?? ''
  if (!startId) return []

  // DFS遍历构建有序序列
  const visited = new Set<string>()
  const sequence: FlowNode[] = []
  
  function dfs(id: string) {
    if (visited.has(id)) return
    visited.add(id)
    const node = nodeMap.get(id)
    if (node) {
      sequence.push(node)
      // 遇到END节点停止遍历
      if (normalizeType(node.node_type) === 'END') return
    }
    // 递归遍历所有后继节点
    for (const t of adj.get(id) || []) dfs(t)
  }
  
  dfs(startId)
  return sequence
}

/**
 * 标准化节点类型名称
 * 
 * 将节点类型转换为大写，并将空格、下划线、连字符统一替换为下划线。
 * 
 * @param t 原始类型字符串
 * @returns 标准化后的类型字符串
 */
export function normalizeType(t: string): string {
  return (t || '').toUpperCase().replace(/[ _-]/g, '_')
}
