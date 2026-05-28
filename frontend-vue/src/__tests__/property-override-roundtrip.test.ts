/**
 * Property 6: Teacher override and restore is a round-trip.
 *
 * **Validates: Requirements 3.3, 3.4**
 *
 * For any node config field in the AI-generated zone, overriding the field value
 * and then restoring it SHALL produce a state identical to the original (override
 * flag cleared, value reverted to AI-generated value). Conversely, overriding a
 * field SHALL mark it as overridden and the AI pipeline SHALL skip that field.
 */
import { describe, it, expect, beforeEach } from 'vitest'
import * as fc from 'fast-check'
import { setActivePinia, createPinia } from 'pinia'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import type { OrchestrationNode, Phase, NodeConfig6D } from '@/services/types/orchestration'

// --- Helpers ---

const RESERVED_KEYS = new Set(['constructor', '__proto__', 'prototype', 'toString', 'valueOf', 'hasOwnProperty', 'isPrototypeOf', 'propertyIsEnumerable', 'toLocaleString', '__defineGetter__', '__defineSetter__', '__lookupGetter__', '__lookupSetter__'])

/** Generate valid field names that won't conflict with JS prototype properties */
const fieldNameArb = fc.string({ minLength: 1, maxLength: 30 })
  .filter(s => /^[a-z][a-z_]*$/.test(s) && !RESERVED_KEYS.has(s))

function makeNode(nodeId: string, overrides: Record<string, boolean> = {}): OrchestrationNode {
  return {
    node_id: nodeId,
    node_type: 'resource_read',
    node_name: 'Test Node',
    config: {
      display: { title: 'Test' },
      db_mapping: { resource_id: 1 },
      ai_processing: { enable_ai_summary: true },
      orchestration_settings: { is_required: true },
      data_collection: { collect_reading_duration: true },
      evaluation: {},
    },
    position: { x: 0, y: 0 },
    is_required: true,
    ai_overrides: { ...overrides },
  }
}

function makePhase(nodes: OrchestrationNode[]): Phase {
  return {
    phase_id: 'phase_1',
    phase_name: 'Test Phase',
    sort_num: 1,
    nodes,
    edges: [],
  }
}

// --- Property Tests ---

describe('Property 6: Override round-trip', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('overriding then restoring produces original state', () => {
    fc.assert(
      fc.property(
        fieldNameArb,
        (fieldName) => {
          const store = useOrchestrationStore()
          const node = makeNode('node_1')
          store.loadCanvas({
            orchestration_id: 'test',
            phases: [makePhase([node])],
          })

          store.selectNode('node_1')

          // Verify initial state: no override
          const initialState = store.selectedNode?.ai_overrides[fieldName]
          if (initialState !== undefined) return false // Should be undefined initially

          // Override the field
          store.overrideAiField('node_1', fieldName)
          const overriddenState = store.selectedNode?.ai_overrides[fieldName]
          if (overriddenState !== true) return false // Should be true after override

          // Restore the field
          store.restoreAiField('node_1', fieldName)
          const restoredState = store.selectedNode?.ai_overrides[fieldName]
          return restoredState === undefined // Should be undefined after restore
        }
      ),
      { numRuns: 100 }
    )
  })

  it('override marks field as overridden', () => {
    fc.assert(
      fc.property(
        fieldNameArb,
        (fieldName) => {
          const store = useOrchestrationStore()
          const node = makeNode('node_1')
          store.loadCanvas({
            orchestration_id: 'test',
            phases: [makePhase([node])],
          })
          store.selectNode('node_1')

          // Override
          store.overrideAiField('node_1', fieldName)

          // Verify it's marked
          return store.selectedNode?.ai_overrides[fieldName] === true
        }
      ),
      { numRuns: 100 }
    )
  })

  it('multiple overrides and restores are independent', () => {
    fc.assert(
      fc.property(
        fc.uniqueArray(
          fieldNameArb,
          { minLength: 2, maxLength: 5 }
        ),
        (fieldNames) => {
          const store = useOrchestrationStore()
          const node = makeNode('node_1')
          store.loadCanvas({
            orchestration_id: 'test',
            phases: [makePhase([node])],
          })
          store.selectNode('node_1')

          // Override all fields
          for (const field of fieldNames) {
            store.overrideAiField('node_1', field)
          }

          // Verify all are overridden
          for (const field of fieldNames) {
            if (store.selectedNode?.ai_overrides[field] !== true) return false
          }

          // Restore only the first field
          store.restoreAiField('node_1', fieldNames[0])
          if (store.selectedNode?.ai_overrides[fieldNames[0]] !== undefined) return false

          // Others remain overridden
          for (let i = 1; i < fieldNames.length; i++) {
            if (store.selectedNode?.ai_overrides[fieldNames[i]] !== true) return false
          }

          return true
        }
      ),
      { numRuns: 100 }
    )
  })

  it('restoring a non-overridden field is a no-op', () => {
    fc.assert(
      fc.property(
        fieldNameArb,
        (fieldName) => {
          const store = useOrchestrationStore()
          const node = makeNode('node_1')
          store.loadCanvas({
            orchestration_id: 'test',
            phases: [makePhase([node])],
          })
          store.selectNode('node_1')

          // Verify field is not in overrides initially
          const before = store.selectedNode?.ai_overrides[fieldName]

          // Restore without prior override — should be safe (no-op)
          store.restoreAiField('node_1', fieldName)

          // Field should still not be in overrides
          const after = store.selectedNode?.ai_overrides[fieldName]
          return before === undefined && after === undefined
        }
      ),
      { numRuns: 100 }
    )
  })
})
