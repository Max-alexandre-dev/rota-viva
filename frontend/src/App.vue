<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { LayoutDashboard, Building2, Boxes, ClipboardList, ArrowLeftRight, HandHeart, History, Menu, X, Route, CircleHelp } from 'lucide-vue-next'

const route = useRoute()
const menuOpen = ref(false)
const currentTitle = computed(() => String(route.meta.title || 'Visão geral'))
const links = [
  { to: '/', label: 'Visão geral', icon: LayoutDashboard },
  { to: '/centros', label: 'Centros de apoio', icon: Building2 },
  { to: '/estoques', label: 'Recursos e estoques', icon: Boxes },
  { to: '/necessidades', label: 'Necessidades', icon: ClipboardList },
  { to: '/transferencias', label: 'Transferências', icon: ArrowLeftRight },
  { to: '/voluntarios', label: 'Voluntários', icon: HandHeart },
  { to: '/historico', label: 'Histórico', icon: History },
]
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar" :class="{ 'sidebar-open': menuOpen }">
      <div class="brand"><div class="brand-mark"><Route :size="25" :stroke-width="2.7" /></div><div><strong>Rota Viva</strong><small>Logística humanitária</small></div></div>
      <nav aria-label="Navegação principal">
        <RouterLink v-for="link in links" :key="link.to" :to="link.to" class="nav-link" :class="{ active: route.path === link.to }" @click="menuOpen = false">
          <component :is="link.icon" :size="18"/><span>{{ link.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-bottom"><CircleHelp :size="17"/> Protótipo · Equipe 404</div>
    </aside>
    <div v-if="menuOpen" class="overlay" @click="menuOpen = false" aria-hidden="true" />
    <div class="workspace">
      <header class="topbar">
        <div class="title-line">
          <button class="mobile-menu" aria-label="Abrir menu" @click="menuOpen = !menuOpen"><X v-if="menuOpen" :size="22"/><Menu v-else :size="22"/></button>
          <div><small>OPERAÇÃO DEMONSTRATIVA</small><h1>{{ currentTitle }}</h1></div>
        </div>
        <span class="demo-pill"><span class="demo-dot"/> Dados fictícios</span>
      </header>
      <main><RouterView /></main>
    </div>
  </div>
</template>
