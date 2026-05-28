/**
 * Frontend integration test: Orchestration editor phase CRUD, node drag, config save.
 *
 * Tests the orchestration store's complete workflow:
 * 1. Phase CRUD: add, rename, reorder, delete phases
 * 2. Node operations: add node to phase, update config, remove node
 * 3. Config save: export canvas JSON preserves all data
 * 4. Constraints: max 10 phases, name length 1-20
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import type { OrchestrationNode, NodeConfig6D } from '@/services/types/orchestration'

function makeTestNode(nodeId: string, nodeType: string = 'resource_read'): OrchestrationNode {
  return {
    node_id: nodeId,
    node_type: nodeType,
    node_name: `Node ${nodeId}`,
    config: {
      display: { title: 'Test' },
      db_mapping: {},
      ai_processing: { enable_ai_summary: true },
      orchestration_settings: { is_required: true },
      data_collection: {},
      evaluation: {},
    },
    position: { x: 100, y: 100 },
    is_required: true,
    ai_overrides: {},
  }
}

describe('Orchestration Editor Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('Phase CRUD', () => {
    it('adds phases up to maximum of 10', () => {
      const store = useOrchestrationStore()

      for (let i = 1; i <= 10; i++) {
        const result = store.addPhase(`Phase ${i}`)
        expect(result).toBe(true)
      }
      expect(store.phases.length).toBe(10)

      // 11th phase should be rejected
      const result = store.addPhase('Phase 11')
      expect(result).toBe(false)
      expect(store.phases.length).toBe(10)
    })

    it('rejects phase names outside 1-20 characters', () => {
      const store = useOrchestrationStore()

      // Empty name
      expect(store.addPhase('')).toBe(false)

      // Name too long (21 chars)
      expect(store.addPhase('A'.repeat(21))).toBe(false)

      // Valid names
      expect(store.addPhase('A')).toBe(true)
      expect(store.addPhase('A'.repeat(20))).toBe(true)
    })

    it('renames phase with valid name', () => {
      const store = useOrchestrationStore()
      store.addPhase('Original')
      const phaseId = store.phases[0].phase_id

      expect(store.renamePhase(phaseId, 'Renamed')).toBe(true)
      expect(store.phases[0].phase_name).toBe('Renamed')
    })

    it('rejects rename with invalid name', () => {
      const store = useOrchestrationStore()
      store.addPhase('Original')
      const phaseId = store.phases[0].phase_id

      expect(store.renamePhase(phaseId, '')).toBe(false)
      expect(store.renamePhase(phaseId, 'X'.repeat(21))).toBe(false)
      expect(store.phases[0].phase_name).toBe('Original')
    })

    it('deletes phase and updates active phase', () => {
      const store = useOrchestrationStore()
      store.addPhase('Phase 1')
      store.addPhase('Phase 2')
      const phase1Id = store.phases[0].phase_id
      const phase2Id = store.phases[1].phase_id

      store.setActivePhase(phase1Id)
      expect(store.removePhase(phase1Id)).toBe(true)
      expect(store.phases.length).toBe(1)
      expect(store.activePhaseId).toBe(phase2Id)
    })

    it('prevents deleting the last phase', () => {
      const store = useOrchestrationStore()
      store.addPhase('Only Phase')
      const phaseId = store.phases[0].phase_id

      expect(store.removePhase(phaseId)).toBe(false)
      expect(store.phases.length).toBe(1)
    })

    it('reorders phases correctly', () => {
      const store = useOrchestrationStore()
      store.addPhase('Phase A')
      store.addPhase('Phase B')
      store.addPhase('Phase C')

      const ids = store.phases.map(p => p.phase_id)
      // Reverse order
      store.reorderPhases([ids[2], ids[0], ids[1]])

      expect(store.phases[0].phase_name).toBe('Phase C')
      expect(store.phases[0].sort_num).toBe(1)
      expect(store.phases[1].phase_name).toBe('Phase A')
      expect(store.phases[1].sort_num).toBe(2)
      expect(store.phases[2].phase_name).toBe('Phase B')
      expect(store.phases[2].sort_num).toBe(3)
    })
  })

  describe('Node Operations', () => {
    it('adds node to active phase', () => {
      const store = useOrchestrationStore()
      store.addPhase('Test Phase')
      store.setActivePhase(store.phases[0].phase_id)

      const node = makeTestNode('node_1')
      expect(store.addNode(node)).toBe(true)
      expect(store.activePhaseNodes.length).toBe(1)
      expect(store.activePhaseNodes[0].node_id).toBe('node_1')
    })

    it('removes node and cleans up edges', () => {
      const store = useOrchestrationStore()
      store.addPhase('Test Phase')
      store.setActivePhase(store.phases[0].phase_id)

      store.addNode(makeTestNode('node_1'))
      store.addNode(makeTestNode('node_2'))
      store.addEdge({ source: 'node_1', target: 'node_2' })

      expect(store.activePhaseEdges.length).toBe(1)

      store.removeNode('node_1')
      expect(store.activePhaseNodes.length).toBe(1)
      expect(store.activePhaseEdges.length).toBe(0) // Edge cleaned up
    })

    it('updates node config', () => {
      const store = useOrchestrationStore()
      store.addPhase('Test Phase')
      store.setActivePhase(store.phases[0].phase_id)
      store.addNode(makeTestNode('node_1'))

      const newConfig: NodeConfig6D = {
        display: { title: 'Updated Title' },
        db_mapping: { resource_id: 42 },
        ai_processing: { enable_ai_summary: false },
        orchestration_settings: { is_required: false },
        data_collection: { collect_duration: true },
        evaluation: { weight: 0.5 },
      }

      store.updateNodeConfig('node_1', newConfig)
      store.selectNode('node_1')
      expect(store.selectedNode?.config.display).toEqual({ title: 'Updated Title' })
      expect(store.selectedNode?.config.db_mapping).toEqual({ resource_id: 42 })
    })

    it('updates node position', () => {
      const store = useOrchestrationStore()
      store.addPhase('Test Phase')
      store.setActivePhase(store.phases[0].phase_id)
      store.addNode(makeTestNode('node_1'))

      store.updateNodePosition('node_1', { x: 250, y: 350 })
      store.selectNode('node_1')
      expect(store.selectedNode?.position).toEqual({ x: 250, y: 350 })
    })
  })

  describe('Canvas Export', () => {
    it('exports complete canvas JSON with all phases and nodes', () => {
      const store = useOrchestrationStore()
      store.templateId = 'tmpl_123'
      store.addPhase('Phase 1')
      store.addPhase('Phase 2')

      store.setActivePhase(store.phases[0].phase_id)
      store.addNode(makeTestNode('node_a'))

      store.setActivePhase(store.phases[1].phase_id)
      store.addNode(makeTestNode('node_b'))

      const canvas = store.exportCanvas()

      expect(canvas.orchestration_id).toBe('tmpl_tmpl_123')
      expect(canvas.phases.length).toBe(2)
      expect(canvas.phases[0].nodes.length).toBe(1)
      expect(canvas.phases[0].nodes[0].node_id).toBe('node_a')
      expect(canvas.phases[1].nodes.length).toBe(1)
      expect(canvas.phases[1].nodes[0].node_id).toBe('node_b')
    })

    it('loadCanvas restores state correctly', () => {
      const store = useOrchestrationStore()

      store.loadCanvas({
        orchestration_id: 'test_orch',
        phases: [
          {
            phase_id: 'p1',
            phase_name: 'Loaded Phase',
            sort_num: 1,
            nodes: [makeTestNode('loaded_node')],
            edges: [],
          },
        ],
      })

      expect(store.phases.length).toBe(1)
      expect(store.phases[0].phase_name).toBe('Loaded Phase')
      expect(store.phases[0].nodes.length).toBe(1)
      expect(store.activePhaseId).toBe('p1')
    })
  })

  describe('Node Config Validation', () => {
    it('validates 6-dimension structure', () => {
      const store = useOrchestrationStore()
      const validNode = makeTestNode('node_1')
      const missing = store.validateNodeConfig(validNode)
      expect(missing).toEqual([])
    })

    it('detects missing dimensions', () => {
      const store = useOrchestrationStore()
      const invalidNode: OrchestrationNode = {
        node_id: 'bad_node',
        node_type: 'test',
        node_name: 'Bad Node',
        config: {
          display: { title: 'Test' },
          db_mapping: {},
          ai_processing: {},
          orchestration_settings: {},
          data_collection: {},
          evaluation: null as unknown as Record<string, unknown>,
        },
        position: { x: 0, y: 0 },
        is_required: true,
        ai_overrides: {},
      }
      const missing = store.validateNodeConfig(invalidNode)
      expect(missing).toContain('evaluation')
    })
  })
})
