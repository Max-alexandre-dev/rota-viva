<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, CircleAlert, LoaderCircle, RefreshCw } from 'lucide-vue-next'
import { useTransfersStore } from '../stores/transfers'

const store = useTransfersStore()

const demo = {
  centers: [
    { id: '10000000-0000-4000-8000-000000000001', label: 'Centro de origem' },
    { id: '10000000-0000-4000-8000-000000000002', label: 'Centro de destino' },
    { id: '10000000-0000-4000-8000-000000000003', label: 'Centro com saldo limitado' },
  ],
  resourceId: '20000000-0000-4000-8000-000000000001',
}

const form = reactive({
  originCenterId: demo.centers[0].id,
  destinationCenterId: demo.centers[1].id,
  quantity: 1,
  reason: '',
})

const orderedTransfers = computed(() => [...store.transfers].sort((a, b) => a.id.localeCompare(b.id)))
const canSubmit = computed(() => form.originCenterId !== form.destinationCenterId && form.quantity > 0 && form.reason.trim().length > 0 && !store.submitting)

function centerLabel(id: string): string {
  return demo.centers.find(center => center.id === id)?.label ?? id
}

function statusLabel(status: string): string {
  return ({ PROPOSTA: 'Proposta', APROVADA: 'Aprovada', REJEITADA: 'Rejeitada', CANCELADA: 'Cancelada' } as Record<string, string>)[status] ?? status
}

function errorDetail(field: string): string | undefined {
  return store.error?.details[field]
}

async function createProposal() {
  const created = await store.create({
    originCenterId: form.originCenterId,
    destinationCenterId: form.destinationCenterId,
    resourceId: demo.resourceId,
    quantity: Number(form.quantity),
    reason: form.reason.trim(),
  })
  if (created) {
    form.quantity = 1
    form.reason = ''
  }
}

onMounted(() => store.load())
</script>

<template>
  <div class="intro">
    <div>
      <p class="eyebrow">OPERAÇÕES DE ESTOQUE</p>
      <h2>Propostas de transferência</h2>
      <p>Consulte propostas e registre decisões usando os dados demonstrativos da API.</p>
    </div>
    <span class="tag">{{ store.storageMode ?? 'Conectando à API' }}</span>
  </div>

  <div class="transfers-layout">
    <section class="card card-section proposal-card">
      <header class="section-header">
        <div><h3>Nova proposta</h3><p>Os saldos e as regras são validados pelo backend.</p></div>
        <ArrowLeftRight :size="20" class="transfer-form-icon" />
      </header>

      <form class="transfer-form" @submit.prevent="createProposal">
        <label>Centro de origem
          <select v-model="form.originCenterId" required>
            <option v-for="center in demo.centers" :key="center.id" :value="center.id">{{ center.label }} · {{ center.id }}</option>
          </select>
          <small class="field-error" v-if="errorDetail('originCenterId')">{{ errorDetail('originCenterId') }}</small>
        </label>
        <label>Centro de destino
          <select v-model="form.destinationCenterId" required>
            <option v-for="center in demo.centers" :key="center.id" :value="center.id">{{ center.label }} · {{ center.id }}</option>
          </select>
          <small class="field-error" v-if="errorDetail('destinationCenterId')">{{ errorDetail('destinationCenterId') }}</small>
        </label>
        <p v-if="form.originCenterId === form.destinationCenterId" class="field-error form-hint">Origem e destino precisam ser diferentes.</p>
        <label>Recurso
          <input :value="`Recurso demonstrativo · ${demo.resourceId}`" readonly />
        </label>
        <label>Quantidade
          <input v-model.number="form.quantity" type="number" min="1" step="1" required />
          <small class="field-error" v-if="errorDetail('quantity')">{{ errorDetail('quantity') }}</small>
        </label>
        <label class="form-wide">Justificativa
          <textarea v-model="form.reason" rows="3" required placeholder="Descreva o motivo da proposta" />
          <small class="field-error" v-if="errorDetail('reason')">{{ errorDetail('reason') }}</small>
        </label>
        <button class="button-primary form-wide" type="submit" :disabled="!canSubmit">
          <LoaderCircle v-if="store.submitting" :size="17" class="spin" />
          <ArrowUpRight v-else :size="17" />
          Criar proposta
        </button>
      </form>
      <p class="demo-note"><CircleAlert :size="15" /> Ambiente demonstrativo em memória. As alterações não são persistidas.</p>
    </section>

    <section class="card card-section transfer-list-card">
      <header class="section-header">
        <div><h3>Transferências</h3><p>{{ store.transfers.length }} registro(s) retornado(s) pela API</p></div>
        <button class="refresh-button" type="button" aria-label="Atualizar transferências" :disabled="store.loading" @click="store.load()">
          <LoaderCircle v-if="store.loading" :size="17" class="spin" />
          <RefreshCw v-else :size="17" />
        </button>
      </header>

      <div v-if="store.error" class="api-error" :class="{ conflict: store.error.isConflict }" role="alert">
        <CircleAlert :size="18" />
        <div><strong>{{ store.error.isConflict ? 'Conflito na operação' : 'Falha na comunicação' }}</strong><p>{{ store.error.message }}</p>
          <small v-if="store.error.code">{{ store.error.code }}<template v-if="store.error.status"> · HTTP {{ store.error.status }}</template></small>
        </div>
      </div>

      <div v-if="store.loading && !store.transfers.length" class="list-state"><LoaderCircle :size="22" class="spin" /> Carregando transferências…</div>
      <div v-else-if="store.listUnavailable && !orderedTransfers.length" class="list-state">A listagem está indisponível. Verifique a conexão com a API e tente atualizar.</div>
      <div v-else-if="!orderedTransfers.length" class="list-state">Nenhuma transferência encontrada. Crie uma proposta para começar.</div>
      <div v-else class="transfer-list">
        <article v-for="transfer in orderedTransfers" :key="transfer.id" class="transfer-item">
          <div class="transfer-item-top">
            <span class="status-pill" :class="`status-${transfer.status.toLowerCase()}`">{{ statusLabel(transfer.status) }}</span>
            <small>{{ transfer.storageMode }}</small>
          </div>
          <div class="route-line">
            <span><ArrowUpRight :size="15" /> {{ centerLabel(transfer.originCenterId) }}</span>
            <span class="route-arrow">→</span>
            <span><ArrowDownLeft :size="15" /> {{ centerLabel(transfer.destinationCenterId) }}</span>
          </div>
          <dl class="transfer-details">
            <div><dt>ID</dt><dd>{{ transfer.id }}</dd></div>
            <div><dt>Recurso</dt><dd>{{ transfer.resourceId }}</dd></div>
            <div><dt>Quantidade</dt><dd>{{ transfer.quantity }}</dd></div>
            <div class="reason-row"><dt>Justificativa</dt><dd>{{ transfer.reason }}</dd></div>
          </dl>
          <div v-if="transfer.status === 'PROPOSTA'" class="transfer-actions">
            <button type="button" class="approve-button" :disabled="store.submitting" @click="store.approve(transfer.id)">Aprovar</button>
            <button type="button" class="reject-button" :disabled="store.submitting" @click="store.reject(transfer.id)">Rejeitar</button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
