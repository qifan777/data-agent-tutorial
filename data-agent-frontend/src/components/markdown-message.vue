<script setup lang="ts">
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

defineProps<{
  message: string
  id?: string
}>()
</script>

<template>
  <md-preview :modelValue="message" :id="id" class="custom-md-preview" />
</template>

<style scoped>
.custom-md-preview {
  background: transparent !important;
  --md-bk-color: transparent !important;
  --md-color: inherit !important;
}

.custom-md-preview :deep(.md-editor-preview-wrapper) {
  padding: 0 !important;
}

.custom-md-preview :deep(.md-editor-preview) {
  color: inherit !important;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}

.custom-md-preview :deep(.md-editor-preview p) {
  margin-bottom: 8px;
  color: inherit;
}

.custom-md-preview :deep(.md-editor-preview p:last-child) {
  margin-bottom: 0;
}

.custom-md-preview :deep(.md-editor-preview ul),
.custom-md-preview :deep(.md-editor-preview ol) {
  padding-left: 20px;
  margin-bottom: 8px;
  color: inherit;
}

.custom-md-preview :deep(.md-editor-preview code) {
  background-color: rgba(127, 127, 127, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  color: inherit;
}

.custom-md-preview :deep(.md-editor-preview pre) {
  background-color: #282c34 !important;
  border-radius: 8px;
  padding: 12px;
  margin: 8px 0;
}

.custom-md-preview :deep(.md-editor-preview pre code) {
  background-color: transparent;
  color: #abb2bf !important;
  padding: 0;
}
</style>
