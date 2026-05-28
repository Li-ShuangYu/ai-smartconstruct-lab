<template>
  <div class="node-type-settings">
    <template v-if="fields.length > 0">
      <div
        v-for="fieldDef in fields"
        :key="fieldDef.field"
        class="field-row"
        :class="{
          'has-error': hasError(fieldDef.field),
          'is-overridden': isOverridden(fieldDef.field)
        }"
      >
        <!-- Field Label -->
        <div class="field-label-row">
          <label class="field-label">
            <!-- Step 2: AI icon indicator for AI-zone fields -->
            <span v-if="zone === 'ai'" class="ai-indicator" title="发布后由AI自动生成">🤖</span>
            {{ fieldDef.label }}
            <span v-if="fieldDef.required" class="required-mark">*</span>
          </label>

          <!-- Step 3: Override badge for AI fields -->
          <span v-if="zone === 'ai' && isOverridden(fieldDef.field)" class="override-badge">
            已覆盖
          </span>
        </div>

        <!-- Step 2: AI tooltip hint -->
        <p v-if="zone === 'ai' && !isOverridden(fieldDef.field)" class="ai-hint">
          发布后由AI自动生成
        </p>

        <!-- Form Control -->
        <div class="field-control">
          <!-- Input -->
          <input
            v-if="fieldDef.type === 'input'"
            type="text"
            class="form-input"
            :class="{ 'input-error': hasError(fieldDef.field) }"
            :placeholder="fieldDef.placeholder"
            :value="getFieldValue(fieldDef)"
            @input="onFieldInput(fieldDef, ($event.target as HTMLInputElement).value)"
          />

          <!-- Textarea -->
          <textarea
            v-else-if="fieldDef.type === 'textarea'"
            class="form-textarea"
            :class="{ 'input-error': hasError(fieldDef.field) }"
            :placeholder="fieldDef.placeholder"
            :value="getFieldValue(fieldDef)"
            rows="3"
            @input="onFieldInput(fieldDef, ($event.target as HTMLTextAreaElement).value)"
          ></textarea>

          <!-- Number -->
          <input
            v-else-if="fieldDef.type === 'number'"
            type="number"
            class="form-input form-input--number"
            :class="{ 'input-error': hasError(fieldDef.field) }"
            :min="fieldDef.min"
            :max="fieldDef.max"
            :value="getFieldValue(fieldDef)"
            @input="onFieldInput(fieldDef, Number(($event.target as HTMLInputElement).value))"
          />

          <!-- Switch -->
          <label v-else-if="fieldDef.type === 'switch'" class="switch-control">
            <input
              type="checkbox"
              :checked="!!getFieldValue(fieldDef)"
              @change="onFieldInput(fieldDef, ($event.target as HTMLInputElement).checked)"
            />
            <span class="switch-slider"></span>
          </label>

          <!-- Radio -->
          <div v-else-if="fieldDef.type === 'radio'" class="radio-group">
            <label
              v-for="opt in fieldDef.options"
              :key="String(opt.value)"
              class="radio-option"
            >
              <input
                type="radio"
                :name="`${node.node_id}_${fieldDef.field}`"
                :value="opt.value"
                :checked="getFieldValue(fieldDef) === opt.value"
                @change="onFieldInput(fieldDef, opt.value)"
              />
              <span class="radio-label">{{ opt.label }}</span>
            </label>
          </div>

          <!-- Select -->
          <select
            v-else-if="fieldDef.type === 'select'"
            class="form-select"
            :class="{ 'input-error': hasError(fieldDef.field) }"
            :value="getFieldValue(fieldDef)"
            @change="onFieldInput(fieldDef, ($event.target as HTMLSelectElement).value)"
          >
            <option value="" disabled>{{ fieldDef.placeholder ?? '请选择' }}</option>
            <option
              v-for="opt in fieldDef.options"
              :key="String(opt.value)"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- Validation error message -->
        <p v-if="hasError(fieldDef.field)" class="field-error">
          <span class="error-icon">⚠️</span>
          {{ getErrorMessage(fieldDef.field) }}
        </p>

        <!-- Step 4: Restore AI button for overridden fields -->
        <button
          v-if="zone === 'ai' && isOverridden(fieldDef.field)"
          class="restore-btn"
          @click="emit('restore-field', fieldDef.field)"
        >
          恢复AI生成
        </button>
      </div>
    </template>

    <!-- Fallback for unknown node types -->
    <div v-else class="no-fields-placeholder">
      <p class="placeholder-text">
        {{ zone === 'manual' ? '该节点类型暂无手动设置项' : '该节点类型暂无AI处理字段' }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { OrchestrationNode } from '@/services/types/orchestration'
import type { ValidationError } from './useNodeValidation'
import type { FieldDefinition } from './nodeFieldDefinitions'
import { getManualFields, getAiFields } from './nodeFieldDefinitions'

const props = defineProps<{
  node: OrchestrationNode
  zone: 'manual' | 'ai'
  aiFlags: string[]
  validationErrors: ValidationError[]
}>()

const emit = defineEmits<{
  'update-config': [dimension: string, field: string, value: unknown]
  'override-field': [fieldName: string]
  'restore-field': [fieldName: string]
}>()

/** Get fields for the current zone */
const fields = computed<FieldDefinition[]>(() => {
  if (props.zone === 'manual') {
    return getManualFields(props.node.node_type, props.aiFlags)
  }
  return getAiFields(props.node.node_type, props.aiFlags)
})

/** Get current field value from node config */
function getFieldValue(fieldDef: FieldDefinition): unknown {
  const dimension = props.node.config[fieldDef.dimension as keyof typeof props.node.config] as Record<string, unknown> | undefined
  if (!dimension) return undefined
  return dimension[fieldDef.field]
}

/** Handle field input — update config and mark override if in AI zone */
function onFieldInput(fieldDef: FieldDefinition, value: unknown) {
  emit('update-config', fieldDef.dimension, fieldDef.field, value)

  // Step 3: If editing an AI-zone field, mark as overridden
  if (props.zone === 'ai' && fieldDef.type !== 'switch') {
    emit('override-field', fieldDef.field)
  }
}

/** Check if field is overridden by teacher */
function isOverridden(fieldName: string): boolean {
  return !!props.node.ai_overrides[fieldName]
}

/** Check if field has validation error */
function hasError(fieldName: string): boolean {
  return props.validationErrors.some(e => e.field === fieldName)
}

/** Get error message for a field */
function getErrorMessage(fieldName: string): string {
  const error = props.validationErrors.find(e => e.field === fieldName)
  return error?.message ?? ''
}
</script>

<style scoped>
.node-type-settings {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-row.has-error .form-input,
.field-row.has-error .form-textarea,
.field-row.has-error .form-select {
  border-color: #ef4444;
}

.field-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text, #2c3e50);
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-indicator {
  font-size: 12px;
  cursor: help;
}

.required-mark {
  color: #ef4444;
  font-weight: 700;
}

.override-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  background: #fef3c7;
  color: #92400e;
  font-weight: 600;
}

.ai-hint {
  font-size: 11px;
  color: #059669;
  margin: 0;
  font-style: italic;
}

.field-control {
  margin-top: 2px;
}

/* Form Controls */
.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text, #2c3e50);
  background: var(--vt-c-white, #ffffff);
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}

.form-input--number {
  width: 120px;
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.input-error {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.1);
}

/* Switch */
.switch-control {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.switch-control input {
  display: none;
}

.switch-slider {
  width: 36px;
  height: 20px;
  background: var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 10px;
  position: relative;
  transition: background 0.2s;
}

.switch-slider::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  background: var(--vt-c-white, #ffffff);
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.switch-control input:checked + .switch-slider {
  background: #4f46e5;
}

.switch-control input:checked + .switch-slider::after {
  transform: translateX(16px);
}

/* Radio */
.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 12px;
  color: var(--color-text, #2c3e50);
}

.radio-option input[type="radio"] {
  width: 14px;
  height: 14px;
  accent-color: #4f46e5;
}

.radio-label {
  font-weight: 500;
}

/* Error */
.field-error {
  font-size: 11px;
  color: #ef4444;
  margin: 2px 0 0 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-icon {
  font-size: 11px;
}

/* Restore button */
.restore-btn {
  margin-top: 4px;
  padding: 4px 10px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: var(--vt-c-white, #ffffff);
  font-size: 11px;
  color: #4f46e5;
  cursor: pointer;
  transition: all 0.15s;
  align-self: flex-start;
}

.restore-btn:hover {
  background: #eef2ff;
  border-color: #4f46e5;
}

/* Placeholder */
.no-fields-placeholder {
  padding: 12px 0;
}

.placeholder-text {
  font-size: 12px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  margin: 0;
  font-style: italic;
}
</style>
