export interface FlowNode { node_id: string; node_type: string; name: string; config: Record<string, unknown> }
export interface FlowEdge { source: string; target: string }

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

  let startId = nodes.find(n => normalizeType(n.node_type) === 'START')?.node_id
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
    const targets = adj.get(id) || []
    for (const t of targets) dfs(t)
  }

  dfs(startId)
  return sequence
}

export function getNextNodeId(currentNodeId: string, nodes: FlowNode[], edges: FlowEdge[]): string | null {
  const ordered = buildOrderedSequence(nodes, edges)
  const idx = ordered.findIndex(n => n.node_id === currentNodeId)
  if (idx < 0 || idx >= ordered.length - 1) return null
  return ordered[idx + 1]?.node_id ?? null
}

export function normalizeType(t: string): string {
  return (t || '').toUpperCase().replace(/[ _-]/g, '_')
}

export function checkIsEndNode(node: FlowNode): boolean {
  return normalizeType(node.node_type).includes('END')
}

export function checkIsStartNode(node: FlowNode): boolean {
  return normalizeType(node.node_type).includes('START')
}
