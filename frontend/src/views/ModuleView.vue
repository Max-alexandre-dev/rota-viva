<script setup lang="ts">
import { ArrowLeft, Construction } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
defineProps<{ title: string; description: string }>()
</script>

<template>
  <section class="module-state card">
    <div class="module-icon"><Construction :size="28"/></div>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <RouterLink to="/" class="button-primary"><ArrowLeft :size="17"/> Voltar à visão geral</RouterLink>
  </section>
</template>
