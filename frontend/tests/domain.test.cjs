const { test } = require('node:test')
const assert = require('node:assert/strict')
const { makeSeed } = require('./.compiled/seed.js')
const { calculateSuggestions, physicalDeficit, plannedDeficit, offerable, reservationConflict } = require('./.compiled/logic.js')

test('cenário A/B/C gera A→B 150 e A→C 100', () => {
  const data = makeSeed()
  const proposals = calculateSuggestions(data, 'agua')
  assert.deepEqual(proposals.map(p => [p.originId, p.destinationId, p.quantity]), [
    ['centro-a', 'centro-b', 150], ['centro-a', 'centro-c', 100],
  ])
})

test('reservas reduzem o ofertável sem alterar estoque físico', () => {
  const data = makeSeed()
  data.balances[0].reserved = 250
  assert.equal(offerable(data, 'centro-a', 'agua'), 50)
  assert.equal(data.balances[0].onHand, 500)
})

test('transferência em trânsito reduz déficit planejável, não o físico', () => {
  const data = makeSeed()
  data.transfers.push({ id: 't1', originId: 'centro-a', destinationId: 'centro-b', resourceId: 'agua', quantity: 150, reason: 'teste', status: 'EM_TRANSITO', createdAt: '2026-11-09T00:00:00.000Z' })
  assert.equal(physicalDeficit(data, 'centro-b', 'agua'), 150)
  assert.equal(plannedDeficit(data, 'centro-b', 'agua'), 0)
})

test('propostas já geradas não são geradas em duplicidade', () => {
  const data = makeSeed()
  data.transfers.push({ id: 'p1', originId: 'centro-a', destinationId: 'centro-b', resourceId: 'agua', quantity: 150, reason: 'teste', status: 'PROPOSTA', createdAt: '2026-11-09T00:00:00.000Z' })
  assert.deepEqual(calculateSuggestions(data, 'agua').map(p => p.quantity), [100])
})

test('divergência física inferior à reserva é detectada', () => {
  const data = makeSeed()
  data.balances[0].reserved = 250
  data.balances[0].onHand = 200
  assert.equal(reservationConflict(data, 'centro-a', 'agua'), true)
})
