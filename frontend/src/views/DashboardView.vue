<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowUpRight, ArrowLeftRight, Building2, CircleAlert, Boxes, PackageCheck, ChevronRight } from 'lucide-vue-next'
import { makeSeed } from '../domain/seed'
import { balanceFor, calculateSuggestions, physicalDeficit, offerable, priorityWeight } from '../domain/logic'

const data = makeSeed()
const water = data.resources.find(r => r.id === 'agua')!
const needs = computed(() => data.needs
  .map(n => ({ ...n, center: data.centers.find(c => c.id === n.centerId)!, resource: data.resources.find(r => r.id === n.resourceId)!, deficit: physicalDeficit(data,n.centerId,n.resourceId) }))
  .filter(n => n.deficit > 0)
  .sort((a,b) => priorityWeight[b.priority] - priorityWeight[a.priority] || b.deficit - a.deficit))
const suggestions = computed(() => data.resources.flatMap(resource => calculateSuggestions(data,resource.id).map(p => ({...p, resource, origin:data.centers.find(c=>c.id===p.originId)!, destination:data.centers.find(c=>c.id===p.destinationId)!, available:offerable(data,p.originId,p.resourceId)}))))
const totalStock = computed(() => data.balances.reduce((s,b)=>s+b.onHand,0))
const openNeeds = computed(() => needs.value.length)
const waterA = computed(() => balanceFor(data,'centro-a','agua'))
</script>

<template>
  <div class="intro"><div><p class="eyebrow">CENTRAL DE OPERAÇÕES</p><h2>Coordene recursos onde eles fazem falta.</h2><p>Acompanhe a rede de centros e avalie sugestões de redistribuição com regras transparentes.</p></div><span class="tag">Cenário sintético</span></div>
  <div class="kpi-grid">
    <div class="card kpi"><span class="kpi-icon"><Building2 :size="20"/></span><div><span class="kpi-label">Centros ativos</span><strong>{{ data.centers.filter(c => c.status === 'ATIVO').length }}</strong><small>Rede simulada</small></div></div>
    <div class="card kpi"><span class="kpi-icon blue"><Boxes :size="20"/></span><div><span class="kpi-label">Unidades em estoque</span><strong>{{ totalStock.toLocaleString('pt-BR') }}</strong><small>Todos os recursos</small></div></div>
    <div class="card kpi"><span class="kpi-icon amber"><CircleAlert :size="20"/></span><div><span class="kpi-label">Déficits identificados</span><strong>{{ openNeeds }}</strong><small>Centro e recurso</small></div></div>
    <div class="card kpi"><span class="kpi-icon"><ArrowLeftRight :size="20"/></span><div><span class="kpi-label">Propostas disponíveis</span><strong>{{ suggestions.length }}</strong><small>Não aprovadas</small></div></div>
  </div>

  <div class="dashboard-grid">
    <section class="card card-section">
      <header class="section-header"><div><h3>Necessidades prioritárias</h3><p>Déficit físico por centro e recurso</p></div><RouterLink to="/necessidades" class="link">Ver módulo <ArrowUpRight :size="15"/></RouterLink></header>
      <div v-if="needs.length" class="table-wrap"><table><thead><tr><th>Centro</th><th>Recurso</th><th>Prioridade</th><th class="num">Déficit</th></tr></thead><tbody><tr v-for="item in needs" :key="item.centerId+item.resourceId"><td><strong>{{ item.center.name }}</strong></td><td>{{ item.resource.name }}</td><td><span class="priority" :class="item.priority.toLowerCase()">{{ item.priority }}</span></td><td class="num"><strong>{{ item.deficit }} {{ item.resource.unit }}</strong></td></tr></tbody></table></div>
      <p v-else class="empty">Nenhuma necessidade pendente neste cenário.</p>
    </section>
    <section class="card card-section">
      <header class="section-header"><div><h3>Redistribuição sugerida</h3><p>Propostas calculadas automaticamente</p></div><span class="neutral-badge">Não confirmadas</span></header>
      <div class="suggestion" v-for="(s,index) in suggestions.slice(0,4)" :key="index">
        <span class="transfer-icon"><ArrowLeftRight :size="19"/></span><div class="suggestion-main"><strong>{{ s.origin.name }} → {{ s.destination.name }}</strong><small>{{ s.resource.name }} · {{ s.quantity }} {{ s.resource.unit }}</small><p>Origem com {{ s.available }} unidades ofertáveis; preserva a reserva mínima.</p></div><span class="status-pill">Sugerida</span>
      </div>
      <div v-if="!suggestions.length" class="empty">Nenhuma proposta disponível.</div>
      <RouterLink to="/transferencias" class="section-cta">Analisar transferências <ChevronRight :size="17"/></RouterLink>
    </section>
  </div>

  <section class="card notice" aria-label="Limitações do protótipo"><PackageCheck :size="19"/><div><strong>Protótipo de consulta</strong><p>Dados e cálculos demonstrativos executados no navegador. Sem reservas ou movimentações reais. O Centro A tem {{ waterA.onHand }} {{ water.unit }} físicas e deve preservar 200 unidades.</p></div></section>
</template>
