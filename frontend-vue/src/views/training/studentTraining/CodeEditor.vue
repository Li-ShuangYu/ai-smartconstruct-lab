<template>
  <div class="code-editor-wrapper">
    <div ref="editorContainer" class="code-editor-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter } from '@codemirror/view'
import { EditorState } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { syntaxHighlighting, defaultHighlightStyle, bracketMatching, foldGutter } from '@codemirror/language'
import { autocompletion, closeBrackets } from '@codemirror/autocomplete'
import { python } from '@codemirror/lang-python'
import { javascript } from '@codemirror/lang-javascript'
import { java } from '@codemirror/lang-java'
import { cpp } from '@codemirror/lang-cpp'

const props = withDefaults(defineProps<{
  modelValue?: string
  language?: string
  readonly?: boolean
  placeholder?: string
}>(), {
  modelValue: '',
  language: 'python',
  readonly: false,
  placeholder: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorContainer = ref<HTMLDivElement | null>(null)
let editorView: EditorView | null = null

function getLangExtension(lang: string) {
  switch (lang) {
    case 'javascript': return javascript()
    case 'java': return java()
    case 'cpp': return cpp()
    default: return python()
  }
}

function buildEditorState(doc: string, lang: string): EditorState {
  const extensions: any[] = [
    lineNumbers(),
    highlightActiveLine(),
    highlightActiveLineGutter(),
    history(),
    foldGutter(),
    bracketMatching(),
    closeBrackets(),
    autocompletion(),
    syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
    getLangExtension(lang),
    keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]),
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        emit('update:modelValue', update.state.doc.toString())
      }
    }),
    EditorView.theme({
      '&': { height: '100%', fontSize: '13.5px', fontFamily: "'Cascadia Code','Consolas','Monaco',monospace", background: '#ffffff' },
      '.cm-scroller': { overflow: 'auto', lineHeight: '1.6' },
      '.cm-content': { padding: '12px 0' },
      '.cm-gutters': { background: '#f8fafc', borderRight: '1px solid #e2e8f0', color: '#94a3b8', minWidth: '40px' },
      '.cm-lineNumbers .cm-gutterElement': { padding: '0 8px 0 4px', minWidth: '32px', textAlign: 'right' },
      '.cm-activeLine': { background: '#f0f9ff' },
      '.cm-activeLineGutter': { background: '#e0f2fe' },
      '.cm-cursor': { borderLeftColor: '#6366f1' },
      '.cm-selectionBackground, &.cm-focused .cm-selectionBackground': { background: '#c7d2fe' },
      '.cm-matchingBracket': { background: '#ddd6fe', outline: '1px solid #818cf8' },
    }),
    EditorView.lineWrapping,
  ]

  if (props.readonly) {
    extensions.push(EditorView.editable.of(false))
  }

  return EditorState.create({ doc, extensions })
}

function initEditor(doc = '', lang = 'python') {
  if (!editorContainer.value) return
  editorView?.destroy()
  editorView = new EditorView({
    state: buildEditorState(doc, lang),
    parent: editorContainer.value,
  })
}

function getCode(): string {
  return editorView?.state.doc.toString() ?? ''
}

function setCode(code: string) {
  if (!editorView) return
  editorView.dispatch({ changes: { from: 0, to: editorView.state.doc.length, insert: code } })
}

watch(() => props.language, (newLang) => {
  const current = getCode()
  initEditor(current, newLang)
})

watch(() => props.modelValue, (newVal) => {
  if (editorView && getCode() !== newVal) {
    setCode(newVal)
  }
})

defineExpose({ getCode, setCode, initEditor })

onMounted(() => {
  initEditor(props.modelValue, props.language)
})

onBeforeUnmount(() => {
  editorView?.destroy()
})
</script>

<style scoped>
.code-editor-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #ffffff;
}
.code-editor-container {
  width: 100%;
  height: 100%;
}
</style>
