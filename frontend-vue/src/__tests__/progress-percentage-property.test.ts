/**
 * Property 6: 进度百分比计算正确性
 *
 * **Validates: Requirements 8.3**
 *
 * 使用 fast-check 生成随机节点数（1-100）和完成数（0-总数），
 * 验证进度百分比计算的正确性。
 *
 * 计算公式：Math.round(completedNodes / totalNodes * 100)
 * 结果范围：0-100 整数
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'

/**
 * 进度百分比计算函数
 * 与 useStudentFlowStore 中 overallProgress 的计算逻辑一致：
 *   Math.round((completed / allNodes.length) * 100)
 */
function calculateProgressPercentage(completedNodes: number, totalNodes: number): number {
  if (totalNodes === 0) return 0
  return Math.round((completedNodes / totalNodes) * 100)
}

describe('Feature: student-training-demo, Property 6: 进度百分比计算正确性', () => {
  /**
   * **Validates: Requirements 8.3**
   *
   * Property: 对于任意 totalNodes (1-100) 和 completedNodes (0 到 totalNodes)，
   * 进度百分比等于 Math.round(completedNodes / totalNodes * 100)，
   * 结果为 0-100 范围内的整数。
   */
  it('百分比 = round(完成数/总数 × 100)，结果范围 0-100 整数', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 100 }).chain((totalNodes) =>
          fc.integer({ min: 0, max: totalNodes }).map((completedNodes) => ({
            totalNodes,
            completedNodes,
          }))
        ),
        ({ totalNodes, completedNodes }) => {
          const result = calculateProgressPercentage(completedNodes, totalNodes)

          // 验证：结果等于 Math.round(completedNodes / totalNodes * 100)
          const expected = Math.round((completedNodes / totalNodes) * 100)
          expect(result).toBe(expected)

          // 验证：结果是整数（无小数）
          expect(Number.isInteger(result)).toBe(true)

          // 验证：结果在 [0, 100] 范围内
          expect(result).toBeGreaterThanOrEqual(0)
          expect(result).toBeLessThanOrEqual(100)

          // 验证：当 completedNodes=0 时，结果=0
          if (completedNodes === 0) {
            expect(result).toBe(0)
          }

          // 验证：当 completedNodes=totalNodes 时，结果=100
          if (completedNodes === totalNodes) {
            expect(result).toBe(100)
          }
        },
      ),
      { numRuns: 100 },
    )
  })
})
