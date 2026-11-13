import type { DemoData } from './types'

export function makeSeed(): DemoData {
  return {
    centers: [
      { id: 'centro-a', name: 'Centro A — Ginásio Municipal', region: 'Zona Norte', address: 'Rio de Janeiro · RJ (fictício)', status: 'ATIVO' },
      { id: 'centro-b', name: 'Centro B — Escola Estadual', region: 'Zona Oeste', address: 'Rio de Janeiro · RJ (fictício)', status: 'ATIVO' },
      { id: 'centro-c', name: 'Centro C — Espaço Comunitário', region: 'Centro', address: 'Rio de Janeiro · RJ (fictício)', status: 'ATIVO' },
    ],
    resources: [
      { id: 'agua', name: 'Água potável', unit: 'garrafas (500 ml)', unitWeightKg: 0.55 },
      { id: 'kit', name: 'Kit de higiene', unit: 'kits', unitWeightKg: 0.9 },
    ],
    balances: [
      { centerId: 'centro-a', resourceId: 'agua', onHand: 500, reserved: 0 },
      { centerId: 'centro-b', resourceId: 'agua', onHand: 20, reserved: 0 },
      { centerId: 'centro-c', resourceId: 'agua', onHand: 50, reserved: 0 },
      { centerId: 'centro-a', resourceId: 'kit', onHand: 120, reserved: 0 },
      { centerId: 'centro-b', resourceId: 'kit', onHand: 30, reserved: 0 },
      { centerId: 'centro-c', resourceId: 'kit', onHand: 18, reserved: 0 },
    ],
    needs: [
      { centerId: 'centro-a', resourceId: 'agua', target: 0, minimumReserve: 200, priority: 'BAIXA' },
      { centerId: 'centro-b', resourceId: 'agua', target: 170, minimumReserve: 0, priority: 'ALTA' },
      { centerId: 'centro-c', resourceId: 'agua', target: 150, minimumReserve: 0, priority: 'MEDIA' },
      { centerId: 'centro-a', resourceId: 'kit', target: 0, minimumReserve: 40, priority: 'BAIXA' },
      { centerId: 'centro-b', resourceId: 'kit', target: 50, minimumReserve: 0, priority: 'ALTA' },
      { centerId: 'centro-c', resourceId: 'kit', target: 65, minimumReserve: 0, priority: 'MEDIA' },
    ],
    transfers: [],
    volunteers: [
      { id: 'vol-1', name: 'Voluntária 01', vehicle: 'CARRO', capacityKg: 120, available: true },
      { id: 'vol-2', name: 'Voluntário 02', vehicle: 'MOTO', capacityKg: 25, available: true },
      { id: 'vol-3', name: 'Voluntária 03', vehicle: 'BICICLETA', capacityKg: 12, available: true },
      { id: 'vol-4', name: 'Voluntário 04', vehicle: 'CARRO', capacityKg: 160, available: true },
    ],
    events: [{ id: 'evt-inicial', at: '2026-11-09T15:00:00-03:00', kind: 'INICIO', description: 'Cenário sintético carregado para demonstração.' }],
  }
}
