import type { Balance, DemoData, Need, Proposal, Priority } from './types'

export const priorityWeight: Record<Priority, number> = { ALTA: 3, MEDIA: 2, BAIXA: 1 }
export const activeCommitments = new Set(['APROVADA', 'EM_TRANSITO'])

export function balanceFor(data: DemoData, centerId: string, resourceId: string): Balance {
  return data.balances.find(b => b.centerId === centerId && b.resourceId === resourceId)
    ?? { centerId, resourceId, onHand: 0, reserved: 0 }
}

export function needFor(data: DemoData, centerId: string, resourceId: string): Need | undefined {
  return data.needs.find(n => n.centerId === centerId && n.resourceId === resourceId)
}

export function offerable(data: DemoData, centerId: string, resourceId: string): number {
  const stock = balanceFor(data, centerId, resourceId)
  return Math.max(0, stock.onHand - stock.reserved - (needFor(data, centerId, resourceId)?.minimumReserve ?? 0))
}

export function physicalDeficit(data: DemoData, centerId: string, resourceId: string): number {
  const stock = balanceFor(data, centerId, resourceId)
  return Math.max(0, (needFor(data, centerId, resourceId)?.target ?? 0) - stock.onHand)
}

export function incomingCommitted(data: DemoData, centerId: string, resourceId: string): number {
  return data.transfers
    .filter(t => t.destinationId === centerId && t.resourceId === resourceId && activeCommitments.has(t.status))
    .reduce((sum, t) => sum + t.quantity, 0)
}

export function plannedDeficit(data: DemoData, centerId: string, resourceId: string): number {
  return Math.max(0, physicalDeficit(data, centerId, resourceId) - incomingCommitted(data, centerId, resourceId))
}

export function reservationConflict(data: DemoData, centerId: string, resourceId: string): boolean {
  const stock = balanceFor(data, centerId, resourceId)
  const minimum = needFor(data, centerId, resourceId)?.minimumReserve ?? 0
  return stock.reserved > 0 && stock.onHand < stock.reserved + minimum
}

// Planejamento determinístico, sem distâncias e sem rota segura.
// Propostas em aberto entram como compromissos de planejamento para impedir gerações repetidas.
export function calculateSuggestions(data: DemoData, resourceId: string): Proposal[] {
  const activeCenters = data.centers.filter(c => c.status === 'ATIVO')
  const supply = new Map(activeCenters.map(c => [c.id, offerable(data, c.id, resourceId)]))
  const demand = new Map(activeCenters.map(c => [c.id, plannedDeficit(data, c.id, resourceId)]))

  for (const t of data.transfers.filter(t => t.resourceId === resourceId && t.status === 'PROPOSTA')) {
    supply.set(t.originId, Math.max(0, (supply.get(t.originId) ?? 0) - t.quantity))
    demand.set(t.destinationId, Math.max(0, (demand.get(t.destinationId) ?? 0) - t.quantity))
  }

  const destinations = activeCenters
    .filter(c => (demand.get(c.id) ?? 0) > 0)
    .sort((a, b) => (priorityWeight[needFor(data, b.id, resourceId)?.priority ?? 'BAIXA'] -
      priorityWeight[needFor(data, a.id, resourceId)?.priority ?? 'BAIXA']) || a.id.localeCompare(b.id))

  const proposals: Proposal[] = []
  for (const destination of destinations) {
    let missing = demand.get(destination.id) ?? 0
    const origins = activeCenters
      .filter(c => c.id !== destination.id && (supply.get(c.id) ?? 0) > 0)
      .sort((a, b) => ((supply.get(b.id) ?? 0) - (supply.get(a.id) ?? 0)) || a.id.localeCompare(b.id))
    for (const origin of origins) {
      if (missing <= 0) break
      const available = supply.get(origin.id) ?? 0
      const quantity = Math.min(missing, available)
      if (quantity <= 0) continue
      proposals.push({
        originId: origin.id,
        destinationId: destination.id,
        resourceId,
        quantity,
        reason: `Origem com ${available} unidades ofertáveis; destino com déficit planejável de ${missing}. Reserva mínima preservada.`,
      })
      missing -= quantity
      supply.set(origin.id, available - quantity)
    }
  }
  return proposals
}
