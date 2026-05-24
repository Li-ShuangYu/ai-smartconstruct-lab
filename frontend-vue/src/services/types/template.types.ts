export interface OrchestrationNode {
  node_id: string
  node_type: string
  name: string
  config: Record<string, unknown>
}

export interface OrchestrationEdge {
  source: string
  target: string
}

export interface CanvasData {
  orchestration_id: string
  nodes: OrchestrationNode[]
  edges: OrchestrationEdge[]
}

export interface TrainingTemplate {
  id?: string
  templateName: string
  templateDesc?: string
  rawCanvasJson?: unknown
  standardPayloadJson?: unknown
  aiStatus: number
  errorReason?: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateTemplateRequest {
  templateName: string
  canvasData: CanvasData
}

export interface CreateTemplateResponse {
  id: number
  aiStatus: number
}
