<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '@/utils/api-instance.ts'

const hello = ref('')
const loadHello = async () => {
  const res = await api.helloController.hello()
  console.log(res)
  hello.value = res.message
}
onMounted(() => {
  loadHello()
})
</script>

<template>
  <h1>Hello, {{ hello }}</h1>
</template>

<style scoped></style>
