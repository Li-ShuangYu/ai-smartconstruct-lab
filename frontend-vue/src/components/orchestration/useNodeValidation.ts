/**
 * 节点配置实时校验 composable
 *
 * Step 5: 500ms debounce 校验选中节点的必填字段，
 * 缺失字段以红色边框和警告图标标识。
 */
import { ref, watch, type Ref, type ComputedRef } from 'vue'
import type { OrchestrationNode } from '@/services/types/orchestration'
import { getRequiredFieldsForNodeType } from './nodeFieldDefinitions'

export interface ValidationError {
  field: string
  message: string
}

/**
 * 实时校验节点配置，返回校验错误列表
 * @param node - 当前选中的节点（响应式）
 */
export function useNodeValidation(node: ComputedRef<OrchestrationNode | null>) {
  const validationErrors = ref<ValidationError[]>([])
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  watch(
    () => node.value ? JSON.stringify(node.value.config) : null,
    () => {
      if (debounceTimer) {
        clearTimeout(debounceTimer)
      }
      debounceTimer = setTimeout(() => {
        validateNode()
      }, 500)
    },
    { immediate: true }
  )

  function validateNode() {
    if (!node.value) {
      validationErrors.value = []
      return
    }

    const errors: ValidationError[] = []
    const requiredFields = getRequiredFieldsForNodeType(node.value.node_type)
    const config = node.value.config

    for (const fieldDef of requiredFields) {
      const dimension = config[fieldDef.dimension as keyof typeof config] as Record<string, unknown> | undefined
      if (!dimension) {
        errors.push({ field: fieldDef.field, message: `${fieldDef.label}不能为空` })
        continue
      }

      const value = dimension[fieldDef.field]
      if (value === undefined || value === null || value === '') {
        errors.push({ field: fieldDef.field, message: `${fieldDef.label}不能为空` })
      }
    }

    validationErrors.value = errors
  }

  return { validationErrors }
}
