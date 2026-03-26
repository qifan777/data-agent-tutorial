<script setup lang="ts">
defineProps<{ content: string; status: 'pending' | 'success' }>()
</script>

<template>
  <div class="toy-node">
    <div class="toy-node__title">Toy Hello Node</div>
    <div class="toy-node__content">{{ content || '等待数据...' }}</div>
    <div class="toy-node__status">{{ status }}</div>
  </div>
</template>
