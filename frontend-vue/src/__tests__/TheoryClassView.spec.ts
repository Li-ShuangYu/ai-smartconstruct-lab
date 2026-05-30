/**
 * TheoryClassView 组件单元测试
 *
 * **Validates: Requirements 6.2**
 *
 * 测试 TheoryClassView 幻灯片组件的渲染和交互逻辑：
 * - 不同 slide type 的正确渲染（intro/theory/code/quiz/summary）
 * - 导航逻辑（上一页/下一页/边界条件）
 * - quiz 提交前不能前进
 * - complete 事件触发
 *
 * @vitest-environment happy-dom
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TheoryClassView from '@/views/training/studentTraining/TheoryClassView.vue'
import type { Slide } from '@/services/types/studentTraining'

// ─── Test Fixtures ────────────────────────────────────────────────────────────

const introSlide: Slide = {
  type: 'intro',
  title: 'Python列表基础',
  content: '列表是Python中最常用的数据结构之一',
  bullet_points: ['有序集合', '可变长度', '支持多种数据类型'],
}

const theorySlide: Slide = {
  type: 'theory',
  title: '列表的创建方式',
  content: 'Python提供了多种创建列表的方式',
  bullet_points: ['字面量语法 []', 'list() 构造函数', '列表推导式'],
}

const codeSlide: Slide = {
  type: 'code',
  title: '列表操作示例',
  content: '以下代码演示了列表的基本操作',
  code: 'numbers = [3, 1, 4, 1, 5]\nnumbers.sort()\nprint(numbers)',
  output: '[1, 1, 3, 4, 5]',
}

const quizSlide: Slide = {
  type: 'quiz',
  title: '知识检测',
  content: '请回答以下问题',
  questions: [
    {
      question: '以下哪个方法会修改原列表？',
      options: ['sorted()', 'list.sort()', 'list[::-1]', 'reversed()'],
      answer: 'list.sort()',
    },
  ],
}

const summarySlide: Slide = {
  type: 'summary',
  title: '本节小结',
  content: '本节学习了列表的基础操作',
  key_points: ['列表是可变序列', 'sort()原地排序', '切片创建新列表'],
}

function createWrapper(slides: Slide[]) {
  return mount(TheoryClassView, {
    props: {
      nodeInstanceId: 1,
      nodeConfig: { slides },
    },
  })
}

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('TheoryClassView', () => {
  describe('渲染 intro/theory slide', () => {
    it('renders intro slide with title, content, and bullet_points', () => {
      const wrapper = createWrapper([introSlide])

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('Python列表基础')
      expect(wrapper.find('.theory-class__slide-text').text()).toBe(
        '列表是Python中最常用的数据结构之一',
      )

      const bullets = wrapper.findAll('.theory-class__bullet-item')
      expect(bullets).toHaveLength(3)
      expect(bullets[0].text()).toBe('有序集合')
      expect(bullets[1].text()).toBe('可变长度')
      expect(bullets[2].text()).toBe('支持多种数据类型')
    })

    it('renders theory slide with title, content, and bullet_points', () => {
      const wrapper = createWrapper([theorySlide])

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('列表的创建方式')
      expect(wrapper.find('.theory-class__slide-text').text()).toBe(
        'Python提供了多种创建列表的方式',
      )

      const bullets = wrapper.findAll('.theory-class__bullet-item')
      expect(bullets).toHaveLength(3)
      expect(bullets[0].text()).toBe('字面量语法 []')
    })
  })

  describe('渲染 code slide', () => {
    it('renders code slide with code block and output', () => {
      const wrapper = createWrapper([codeSlide])

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('列表操作示例')
      expect(wrapper.find('.theory-class__slide-text').text()).toBe(
        '以下代码演示了列表的基本操作',
      )
      expect(wrapper.find('.theory-class__code-content').text()).toContain('numbers.sort()')
      expect(wrapper.find('.theory-class__output-content').text()).toBe('[1, 1, 3, 4, 5]')
    })
  })

  describe('渲染 quiz slide', () => {
    it('renders quiz slide with questions and options', () => {
      const wrapper = createWrapper([quizSlide])

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('知识检测')
      expect(wrapper.find('.theory-class__quiz-stem').text()).toContain(
        '以下哪个方法会修改原列表？',
      )

      const options = wrapper.findAll('.theory-class__quiz-option')
      expect(options).toHaveLength(4)
      expect(options[0].text()).toBe('sorted()')
      expect(options[1].text()).toBe('list.sort()')
      expect(options[2].text()).toBe('list[::-1]')
      expect(options[3].text()).toBe('reversed()')
    })
  })

  describe('渲染 summary slide', () => {
    it('renders summary slide with key_points', () => {
      const wrapper = createWrapper([summarySlide])

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('本节小结')
      expect(wrapper.find('.theory-class__slide-text').text()).toBe(
        '本节学习了列表的基础操作',
      )
      expect(wrapper.find('.theory-class__key-points-title').text()).toBe('关键知识点')

      const keyPoints = wrapper.findAll('.theory-class__key-point-item')
      expect(keyPoints).toHaveLength(3)
      expect(keyPoints[0].text()).toBe('列表是可变序列')
      expect(keyPoints[1].text()).toBe('sort()原地排序')
      expect(keyPoints[2].text()).toBe('切片创建新列表')
    })
  })

  describe('导航逻辑', () => {
    it('prev button is disabled on first slide', () => {
      const wrapper = createWrapper([introSlide, theorySlide, summarySlide])

      const prevBtn = wrapper.find('.theory-class__nav-btn--prev')
      expect(prevBtn.attributes('disabled')).toBeDefined()
    })

    it('next button advances to next slide', async () => {
      const wrapper = createWrapper([introSlide, theorySlide, summarySlide])

      // Initially on first slide
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('Python列表基础')
      expect(wrapper.find('.theory-class__progress').text()).toBe('1 / 3')

      // Click next
      await wrapper.find('.theory-class__nav-btn--next').trigger('click')

      expect(wrapper.find('.theory-class__slide-title').text()).toBe('列表的创建方式')
      expect(wrapper.find('.theory-class__progress').text()).toBe('2 / 3')
    })

    it('prev button navigates back to previous slide', async () => {
      const wrapper = createWrapper([introSlide, theorySlide, summarySlide])

      // Go to second slide
      await wrapper.find('.theory-class__nav-btn--next').trigger('click')
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('列表的创建方式')

      // Go back
      await wrapper.find('.theory-class__nav-btn--prev').trigger('click')
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('Python列表基础')
    })

    it('cannot advance past quiz slide without submitting', async () => {
      const wrapper = createWrapper([introSlide, quizSlide, summarySlide])

      // Navigate to quiz slide
      await wrapper.find('.theory-class__nav-btn--next').trigger('click')
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('知识检测')

      // Next button should be disabled (quiz not submitted)
      const nextBtn = wrapper.find('.theory-class__nav-btn--next')
      expect(nextBtn.attributes('disabled')).toBeDefined()
    })

    it('can advance past quiz slide after submitting', async () => {
      const wrapper = createWrapper([introSlide, quizSlide, summarySlide])

      // Navigate to quiz slide
      await wrapper.find('.theory-class__nav-btn--next').trigger('click')

      // Select an answer
      const options = wrapper.findAll('.theory-class__quiz-option')
      await options[1].trigger('click') // select 'list.sort()'

      // Submit quiz
      await wrapper.find('.theory-class__quiz-submit').trigger('click')

      // Now next button should be enabled
      const nextBtn = wrapper.find('.theory-class__nav-btn--next')
      expect(nextBtn.attributes('disabled')).toBeUndefined()

      // Can advance
      await nextBtn.trigger('click')
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('本节小结')
    })
  })

  describe('complete 事件', () => {
    it('clicking "完成" on last slide emits complete event', async () => {
      const wrapper = createWrapper([introSlide, theorySlide])

      // Navigate to last slide
      await wrapper.find('.theory-class__nav-btn--next').trigger('click')
      expect(wrapper.find('.theory-class__slide-title').text()).toBe('列表的创建方式')

      // Should show complete button instead of next
      const completeBtn = wrapper.find('.theory-class__nav-btn--complete')
      expect(completeBtn.exists()).toBe(true)
      expect(completeBtn.text()).toContain('完成')

      // Click complete
      await completeBtn.trigger('click')

      // Should emit 'complete' event
      expect(wrapper.emitted('complete')).toBeTruthy()
      expect(wrapper.emitted('complete')).toHaveLength(1)
    })

    it('does not show complete button on non-last slides', () => {
      const wrapper = createWrapper([introSlide, theorySlide, summarySlide])

      // On first slide, should show next button, not complete
      expect(wrapper.find('.theory-class__nav-btn--next').exists()).toBe(true)
      expect(wrapper.find('.theory-class__nav-btn--complete').exists()).toBe(false)
    })
  })
})
