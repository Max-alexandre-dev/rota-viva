export type CenterStatus = 'ATIVO' | 'INATIVO'
export type Priority = 'ALTA' | 'MEDIA' | 'BAIXA'
export type TransferStatus = 'PROPOSTA' | 'APROVADA' | 'EM_TRANSITO' | 'RECEBIDA' | 'REJEITADA' | 'CANCELADA'
export type Vehicle = 'BICICLETA' | 'MOTO' | 'CARRO'
export type Center = { id: string; name: string; region: string; address: string; status: CenterStatus }
export type Resource = { id: string; name: string; unit: string; unitWeightKg: number }
export type Balance = { centerId: string; resourceId: string; onHand: number; reserved: number }
export type Need = { centerId: string; resourceId: string; target: number; minimumReserve: number; priority: Priority }
export type Transfer = {
  id: string
  originId: string
  destinationId: string
  resourceId: string
  quantity: number
  reason: string
  status: TransferStatus
  createdAt: string
  volunteerId?: string
}
export type Volunteer = { id: string; name: string; vehicle: Vehicle; capacityKg: number; available: boolean }
export type AuditEvent = { id: string; at: string; kind: string; description: string }
export type DemoData = {
  centers: Center[]
  resources: Resource[]
  balances: Balance[]
  needs: Need[]
  transfers: Transfer[]
  volunteers: Volunteer[]
  events: AuditEvent[]
}
export type Proposal = Omit<Transfer, 'id' | 'createdAt' | 'status' | 'volunteerId'>
export type ActionResult = { ok: boolean; message: string }
