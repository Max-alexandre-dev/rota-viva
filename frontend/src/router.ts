import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from './views/DashboardView.vue'
import ModuleView from './views/ModuleView.vue'

const routes = [
  { path: '/', component: DashboardView, meta: { title: 'Visão geral' } },
  { path: '/centros', component: ModuleView, props: { title: 'Centros de apoio', description: 'Cadastro e acompanhamento dos centros da rede. A implementação de escrita depende do contrato REST.' } },
  { path: '/estoques', component: ModuleView, props: { title: 'Recursos e estoques', description: 'Controle de saldos físicos, reservas e movimentações. Em desenvolvimento.' } },
  { path: '/necessidades', component: ModuleView, props: { title: 'Necessidades', description: 'Metas e prioridades por centro. Em desenvolvimento.' } },
  { path: '/transferencias', component: ModuleView, props: { title: 'Transferências', description: 'Avaliação e aprovação de propostas. Em desenvolvimento; o protótipo não reserva estoque.' } },
  { path: '/voluntarios', component: ModuleView, props: { title: 'Voluntários', description: 'Cadastro e missões de transporte. Etapa seguinte do MVP.' } },
  { path: '/historico', component: ModuleView, props: { title: 'Histórico', description: 'Rastreabilidade das operações. Em desenvolvimento.' } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

export default createRouter({ history: createWebHistory(), routes })
