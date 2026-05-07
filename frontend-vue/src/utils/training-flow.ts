export interface FlowNode { node_id: string; node_type: string; name: string; config: Record<string, unknown> }
export interface FlowEdge { source: string; target: string }

/** 从 edges 中找 source → target 的下一个 node_id */
export function getNextNodeId(currentNodeId: string, edges: FlowEdge[]): string | null {
  const edge = edges.find(e => e.source === currentNodeId)
  return edge?.target ?? null
}

/** 在所有 nodes 中找出 START 节点 */
export function findStartNodeId(nodes: FlowNode[]): string | null {
  const start = nodes.find(n => normalizeType(n.node_type) === 'START')
  return start?.node_id ?? null
}

/** 检查节点是否为 END 类型 */
export function checkIsEndNode(node: FlowNode): boolean {
  return normalizeType(node.node_type || '').includes('END')
}

/** 构建有序序列 (DFS from START following edges) */
export function buildOrderedSequence(nodes: FlowNode[], edges: FlowEdge[]): FlowNode[] {
  if (!nodes || !nodes.length) return []
  const nodeMap = new Map<string, FlowNode>()
  for (const n of nodes) nodeMap.set(n.node_id, n)

  const adj = new Map<string, string[]>()
  for (const e of edges) {
    const targets = adj.get(e.source) || []
    targets.push(e.target)
    adj.set(e.source, targets)
  }

  let startId = findStartNodeId(nodes)
  if (!startId && nodes.length > 0) startId = nodes[0]?.node_id ?? ''
  if (!startId) return []

  const visited = new Set<string>()
  const sequence: FlowNode[] = []
  function dfs(id: string) {
    if (visited.has(id)) return
    visited.add(id)
    const node = nodeMap.get(id)
    if (node) {
      sequence.push(node)
      if (normalizeType(node.node_type) === 'END') return
    }
    for (const t of adj.get(id) || []) dfs(t)
  }
  dfs(startId)
  return sequence
}

export function normalizeType(t: string): string {
  return (t || '').toUpperCase().replace(/[ _-]/g, '_')
}
